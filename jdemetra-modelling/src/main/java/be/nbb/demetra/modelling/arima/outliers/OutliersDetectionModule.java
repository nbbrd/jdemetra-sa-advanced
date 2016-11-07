/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package be.nbb.demetra.modelling.arima.outliers;

import be.nbb.demetra.modelling.outliers.AdditiveOutlier;
import be.nbb.demetra.modelling.outliers.IOutlierFactory;
import be.nbb.demetra.modelling.outliers.IOutlierVariable;
import be.nbb.demetra.modelling.outliers.LevelShift;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.GlsArimaMonitor;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.modelling.arima.IOutliersDetectionModule.ICriticalValueComputer;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 *
 * @author Jean Palate
 */
public class OutliersDetectionModule<T extends IArimaModel> {

    /**
     * @return the estimation
     */
    public Estimation getEstimation() {
        return estimation;
    }

    /**
     * @param estimation the estimation to set
     */
    public void setEstimation(Estimation estimation) {
        this.estimation = estimation;
    }

    /**
     * @return the method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * @return the monitor
     */
    public GlsArimaMonitor<T> getMonitor() {
        return monitor;
    }

    /**
     * @param monitor the monitor to set
     */
    public void setMonitor(GlsArimaMonitor<T> monitor) {
        this.monitor = monitor;
    }

    /**
     * @return the regarima_
     */
    public RegArimaModel<T> getRegarima() {
        return regarima_;
    }

    /**
     * @return the outliers_
     */
    public IOutlierVariable[] getOutliers() {
        return outliers_.toArray(new IOutlierVariable[outliers_.size()]);
    }

    /**
     * @return the addHook
     */
    public Consumer<IOutlierVariable> getAddHook() {
        return addHook;
    }

    /**
     * @param addHook the addHook to set
     */
    public void setAddHook(Consumer<IOutlierVariable> addHook) {
        this.addHook = addHook;
    }

    /**
     * @return the removeHook
     */
    public Consumer<IOutlierVariable> getRemoveHook() {
        return removeHook;
    }

    /**
     * @param removeHook the removeHook to set
     */
    public void setRemoveHook(Consumer<IOutlierVariable> removeHook) {
        this.removeHook = removeHook;
    }

    public static enum Method {

        Tramo,
        X13,
        Demetra1
    }

    public static enum Estimation {

        Approximate,
        ExactIterative,
        ExactFixed
    }

    private static int MAXROUND = 100;
    private static int MAXOUTLIERS = 50;
    protected RegArimaModel<T> regarima_;
    protected final ArrayList<IOutlierVariable> outliers_ = new ArrayList<>();
    private final AbstractSingleOutlierDetector sod;
    private double[] tstats_;
    private int nhp_;
    private int round_;
    // festim = true if the model has to be re-estimated
    private boolean exit_;
    private IOutlierVariable lastremoved_;
    protected GlsArimaMonitor<T> monitor;

    private Estimation estimation = Estimation.ExactIterative;
    private Method method = Method.Tramo;
    private int selectivity_;
    private double cv_, curcv_;
    private double pc_ = 0.12;
    public static final double MINCV = 2.0;
    protected Consumer<IOutlierVariable> addHook;
    protected Consumer<IOutlierVariable> removeHook;

    public OutliersDetectionModule() {
        sod = new FastOutlierDetector();
    }

    public OutliersDetectionModule(AbstractSingleOutlierDetector sod) {
        this.sod = sod;
    }

    public boolean process(RegArimaModel<T> initialModel) {
        clear();
        int n = initialModel.getY().getLength();
        sod.prepare(n);
        sod.setBounds(0, n);
        regarima_ = initialModel.clone();
        if (curcv_ == 0) {
            curcv_ = calcCv();
        }
        nhp_ = 0;
        if (!estimateModel(true)) {
            return false;
        }
        try {
            switch (method) {
                case Tramo:
                    calcTramo();
                    break;
                case X13:
                    calcX13();
                    break;
                case Demetra1:
                    calcDemetra1();
                    break;
            }
            return true;
        } catch (RuntimeException err) {
            return false;
        }
    }

    private void calcTramo() {
        double max;
        exit_ = false;
        do {
            if (!sod.process(regarima_)) {
                break;
            }
            round_++;
            max = sod.getMaxTStat();
            if (Math.abs(max) < curcv_) {
                break;
            }
            IOutlierVariable o = sod.getMaxOutlier();
            boolean bok = true;
            for (int i = 0; i < outliers_.size(); ++i) {
                if (o.getPosition() == outliers_.get(i).getPosition()) {
                    bok = false;
                    break;
                }
            }
            if (bok) {
                addOutlier(o);
                estimateModel(false);

                while (!verifyModel()) {
                    if (exit_) {
                        break;
                    }
//                    estimateModel(false);
                    updateLikelihood(regarima_.computeLikelihood());
                }
                if (exit_ || outliers_.size() == MAXOUTLIERS) {
                    break;
                }
            } else {
                break;
            }
        } while (round_ < MAXROUND);

        while (!verifyModel()) {
//                updateLikelihood(regarima_.computeLikelihood());
            estimateModel(false);
        }
    }

    private void calcX13() {
//        double max;
//        do {
//            if (!sod.process(regarima_)) {
//                break;
//            }
//            round_++;
//            max = sod.getMaxTStat();
//            if (Math.abs(max) < curcv_) {
//                break;
//            }
//            IOutlierVariable o = sod.getMaxOutlier();
//            addOutlier(o);
//            addOutlierInfo(context, o, max);
//            estimateModel(false);
//
//            if (outliers_.size() == MAXOUTLIERS) {
//                break;
//            }
//        } while (round_ < MAXROUND);
//
//        while (!verifyModel(context)) {
//            estimateModel(false);
//        }
    }

    private void calcDemetra1() {
//        double max;
//        do {
//            if (!sod.process(regarima_)) {
//                break;
//            }
//            round_++;
//            max = sod.getMaxTStat();
//            if (Math.abs(max) < curcv_) {
//                break;
//            }
//            IOutlierVariable o = sod.getMaxOutlier();
//            addOutlier(o);
//            addOutlierInfo(context, o, max);
//            if (outliers_.size() == MAXOUTLIERS) {
//                break;
//            }
//        } while (round_ < MAXROUND);
//        estimateModel(true);
//        while (!verifyModel(context)) {
//            estimateModel(false);
//        }
    }

    private boolean estimateModel(boolean full) {
        RegArimaEstimation<T> est = full ? monitor.process(regarima_) : monitor.optimize(regarima_);
        regarima_ = est.model;
        updateLikelihood(est.likelihood);
        return true;
    }

    private void updateLikelihood(ConcentratedLikelihood likelihood) {
        tstats_ = likelihood.getTStats(true, nhp_);
    }

    private void clear() {
        nhp_ = 0;
        outliers_.clear();
        round_ = 0;
        lastremoved_ = null;
        tstats_ = null;
        curcv_ = 0;
        // festim = true if the model has to be re-estimated
    }

    /**
     * Backward procedure (without re-estimation of the model)
     *
     * @param exit
     * @return True means that the model was not modified
     */
    private boolean verifyModel() {
        if (outliers_.isEmpty()) {
            return true;
        }
        /*double[] t = m_model.computeLikelihood().getTStats(true,
         m_model.getArma().getParametersCount());*/
        int nx0 = regarima_.getVarsCount() - outliers_.size();
        int imin = 0;
        for (int i = 1; i < outliers_.size(); ++i) {
            if (Math.abs(tstats_[i + nx0]) < Math.abs(tstats_[imin + nx0])) {
                imin = i;
            }
        }

        if (Math.abs(tstats_[nx0 + imin]) >= curcv_) {
            return true;
        }
        IOutlierVariable toremove = outliers_.get(imin);
        sod.allow(toremove);
        removeOutlier(imin);
        if (removeHook != null)
            removeHook.accept(toremove);
        if (lastremoved_ != null) {
            if (toremove.getPosition() == lastremoved_.getPosition()
                    && toremove.getCode().equals(lastremoved_.getCode())) {
                exit_ = true;
            }
        }
        lastremoved_ = toremove;
        return false;
    }

    private void addOutlier(IOutlierVariable o) {
        outliers_.add(o);
        double[] xo = new double[regarima_.getObsCount()];
        DataBlock XO = new DataBlock(xo);
        o.data(sod.getLBound(), XO);
        regarima_.addX(XO);
        sod.exclude(o);
        if (addHook != null)
            addHook.accept(o);
    }

    /**
     *
     * @param model
     * @return
     */
    private void removeOutlier(int idx) {
        //
        int opos = regarima_.getXCount() - outliers_.size() + idx;
        regarima_.removeX(opos);
        outliers_.remove(idx);
    }

    /**
     *
     * @param o
     */
    public void addOutlierFactory(IOutlierFactory o) {
        sod.addOutlierFactory(o);
    }

    /**
     *
     */
    public void clearOutlierFactories() {
        sod.clearOutlierFactories();
    }

    /**
     *
     */
    public void setAll() {
        clear();
        clearOutlierFactories();
        addOutlierFactory(new AdditiveOutlier.Factory());
        LevelShift.Factory lfac = new LevelShift.Factory();
        lfac.setZeroEnded(true);
        addOutlierFactory(lfac);
//        addOutlierFactory(new TransitoryChangeFactory());
//        SeasonalOutlierFactory sfac = new SeasonalOutlierFactory();
//        sfac.setZeroEnded(true);
//        addOutlierFactory(sfac);
    }

    /**
     *
     * @return
     */
    public int getOutlierFactoriesCount() {
        return sod.getOutlierFactoriesCount();
    }

    /**
     *
     */
    public void setDefault() {
        clear();
        clearOutlierFactories();
        addOutlierFactory(new AdditiveOutlier.Factory());
        addOutlierFactory(new LevelShift.Factory());
        //      addOutlierFactory(new TransitoryChangeFactory());
        curcv_ = 0;
    }

    public void setCriticalValue(double value) {
        cv_ = value;
    }

    public double getCritivalValue() {
        return cv_;
    }

    public double getPc() {
        return pc_;
    }

    public void setPc(double pc) {
        pc_ = pc;
    }

    private double calcCv() {
        double cv = cv_;
        if (cv == 0) {
            cv = ICriticalValueComputer.defaultComputer().compute(this.regarima_.getObsCount());
        }
        for (int i = 0; i < -selectivity_; ++i) {
            cv *= (1 - pc_);
        }
        return Math.max(cv, MINCV);
    }

    public boolean reduceSelectivity() {
        if (curcv_ == 0) {
            return false;
        }
        --selectivity_;
        if (curcv_ == MINCV) {
            return false;
        }
        curcv_ = Math.max(MINCV, curcv_ * (1 - pc_));

        return true;
    }

    public void setSelectivity(int level) {
        if (selectivity_ != level) {
            selectivity_ = level;
            curcv_ = 0;
        }
    }

    public int getSelectivity() {
        return selectivity_;
    }

}
