/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.mtd;

import ec.satoolkit.x11.AsymmetricEndPoints;
import ec.satoolkit.x11.SeasonalFilterFactory;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.IBuilder;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author palatej
 */
public class MovingTradingDaysEstimator {

    public static class Builder implements IBuilder<MovingTradingDaysEstimator> {

        private int windowLength = 11;
        private SymmetricFilter sfilter = SeasonalFilterFactory.S3X3;
        private AsymmetricEndPoints endPoints = SeasonalFilterFactory.endPoints(2);
        private boolean reestimate = true;
        
        private Builder(){}

        public Builder reEstimateModel(boolean reestimate) {
            this.reestimate = reestimate;
            return this;
        }

        public Builder windowLength(int len) {
            if (len % 2 != 1) {
                throw new IllegalArgumentException("Should be odd");
            }
            this.windowLength = len;
            return this;
        }

        public Builder smoother(SymmetricFilter sfilter) {
            this.sfilter = sfilter;
            this.endPoints = null;
            return this;
        }

        public Builder smoother(SymmetricFilter sfilter, AsymmetricEndPoints endPoints) {
            if (endPoints.getEndPointsCount() != sfilter.getLength() / 2) {
                throw new IllegalArgumentException();
            }
            this.sfilter = sfilter;
            this.endPoints = endPoints;
            return this;
        }

        @Override
        public MovingTradingDaysEstimator build() {
            return new MovingTradingDaysEstimator(this);
        }
    }
    
    public static Builder builder(){
        return new Builder();
    }

    private PreprocessingModel model;
    private Matrix td, tdCoefficients, C;
    private double[] startTdCoefficients;
    private double[] startTdStde;
    private TsData partialLinearizedSeries, tdEffect;
    private int ny, cbeg, cend;

    private final SymmetricFilter sfilter;
    private final AsymmetricEndPoints endPoints;
    private final int windowLength;
    private final boolean reestimate;

    public MovingTradingDaysEstimator(Builder builder) {
        this.windowLength = builder.windowLength;
        this.reestimate = builder.reestimate;
        this.sfilter = builder.sfilter;
        this.endPoints = builder.endPoints;
    }

    public boolean process(PreprocessingModel model) {
        this.model = model;
        try {
            processFullModel();
            computeRawCoefficients();
            smoothCoefficients();
            computeTdEffects();
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    private RegArimaModel<SarimaModel> regarima(double[] data, Matrix x, boolean mean, SarimaModel arima, int start, int end) {
        RegArimaModel<SarimaModel> regarima = new RegArimaModel<>();
        regarima.setArima(arima);
        regarima.setMeanCorrection(mean);
        regarima.setY(new DataBlock(data, start, end, 1));
        SubMatrix px = x.subMatrix(start, end, 0, x.getColumnsCount());
        for (int i = 0; i < x.getColumnsCount(); ++i) {
            regarima.addX(px.column(i));
        }
        return regarima;
    }

    private void processFullModel() {
        // search for TD variables
        TsVariableSelection sel = model.description.buildRegressionVariables(var -> var.isCompatible(ITradingDaysVariable.class));
        if (sel.isEmpty()) {
            return;
        }

        ConcentratedLikelihood ll = model.estimation.getLikelihood();
        int ntd = sel.getVariablesCount();
        startTdCoefficients = new double[ntd];
        startTdStde = new double[ntd];
        int n0 = model.description.getRegressionVariablesStartingPosition();
        double[] b = ll.getB();
        for (int j = 0; j < ntd; ++j) {
            startTdCoefficients[j] = b[n0 + j];
            startTdStde[j] = ll.getBSer(n0 + j, false, 0);
        }
        TsDomain domain = model.description.getSeriesDomain();
        TsData tdeffect = model.deterministicEffect(domain, v -> TsVariableList.getRoot(v) instanceof ITradingDaysVariable);
        partialLinearizedSeries = TsData.add(model.linearizedSeries(true), tdeffect);
        td = sel.matrix(domain);
    }

    private void computeRawCoefficients() {
        TsDomain domain = model.description.getSeriesDomain();
        int ntd = getTd().getColumnsCount();
        // moving window
        int freq = domain.getFrequency().intValue();
        int wlen = windowLength * freq;
        cbeg = domain.getStart().getPosition();
        if (cbeg != 0) {
            cbeg = freq - cbeg - 1;
        }
        cend = domain.getEnd().getPosition();
        boolean mean = model.description.isMean();

        ny = (domain.getLength() - cbeg - cend) / freq;
        int ne = ny - windowLength + 1;
        C = new Matrix(ne, ntd);
        for (int i = 0, i0 = 0, i1 = cbeg + wlen; i < ne;) {
            RegArimaModel<SarimaModel> reg = regarima(getPartialLinearizedSeries().internalStorage(), getTd(), mean, model.estimation.getArima(), i0, i1);
            if (reestimate) {
                GlsSarimaMonitor monitor = new GlsSarimaMonitor();
                RegArimaEstimation<SarimaModel> estimation = monitor.optimize(reg);
                double[] pb = estimation.likelihood.getB();
                getC().row(i).copyFrom(pb, mean ? 1 : 0);
            } else {
                double[] pb = reg.computeLikelihood().getB();
                getC().row(i).copyFrom(pb, mean ? 1 : 0);
            }
            i0 += freq;
            i1 += freq;
            if (++i == ne - 1) {
                i1 += cend;
            }

        }
    }

    private void smoothCoefficients() {
        // extends the matrix of coefficients
        int r0 = windowLength / 2, r1 = r0;
        if (cbeg > 0) {
            ++r0;
        }
        if (cend > 0) {
            ++r1;
        }
        int lr = r0 + getC().getRowsCount();
        Matrix x = new Matrix(lr + r1, getC().getColumnsCount());
        x.subMatrix(r0, r0 + getC().getRowsCount(), 0, getC().getColumnsCount()).copy(getC().all());
        DataBlock row0 = x.row(r0), row1 = x.row(lr - 1);
        for (int i = 0; i < r0; ++i) {
            x.row(i).copy(row0);
        }
        for (int i = lr; i < x.getRowsCount(); ++i) {
            x.row(i).copy(row1);
        }

        tdCoefficients = new Matrix(x.getRowsCount(), x.getColumnsCount());
        // apply the smoother on each columns

        int n = sfilter.getLength() / 2;
        for (int i = 0; i < x.getColumnsCount(); ++i) {
            DataBlock icol = x.column(i), ocol = tdCoefficients.column(i);
            sfilter.filter(icol, ocol.drop(n, n));
            if (endPoints != null) {
                endPoints.process(icol, ocol);
            } else {
                ocol.range(0, n).set(ocol.get(n + 1));
                int m = ocol.getLength(), l = m - n - 1;
                ocol.range(m - n, m).set(ocol.get(l));
            }
        }

    }

    private void computeTdEffects() {
        TsDomain domain = model.description.getSeriesDomain();
        tdEffect = new TsData(domain);
        // incomplete y0
        int row = 0, i = 0;
        if (cbeg > 0) {
            DataBlock crow = tdCoefficients.row(row++);
            for (; i < cbeg; ++i) {
                tdEffect.set(i, td.row(i).dot(crow));
            }
        }
        int freq = domain.getFrequency().intValue();
        for (; row < tdCoefficients.getRowsCount(); ++row) {
            DataBlock crow = tdCoefficients.row(row);
            int cur = 0;
            while (cur++ < freq && i < domain.getLength()) {
                tdEffect.set(i, td.row(i++).dot(crow));
            }
        }
    }

    /**
     * @return the model
     */
    public PreprocessingModel getModel() {
        return model;
    }

    /**
     * @return the tdCoefficients
     */
    public Matrix getTdCoefficients() {
        return tdCoefficients;
    }

    /**
     * @return the tdCoefficients
     */
    public Matrix getRawCoefficients() {
        return getC();
    }

    /**
     * @return the startTdCoefficients
     */
    public double[] getStartTdCoefficients() {
        return startTdCoefficients;
    }

    /**
     * @return the startTdStde
     */
    public double[] getStartTdStde() {
        return startTdStde;
    }

    /**
     * @return the td
     */
    public Matrix getTd() {
        return td;
    }

    /**
     * @return the C
     */
    public Matrix getC() {
        return C;
    }

    /**
     * @return the partialLinearizedSeries
     */
    public TsData getPartialLinearizedSeries() {
        return partialLinearizedSeries;
    }

    /**
     * @return the tdEffect
     */
    public TsData getTdEffect() {
        return tdEffect;
    }

}
