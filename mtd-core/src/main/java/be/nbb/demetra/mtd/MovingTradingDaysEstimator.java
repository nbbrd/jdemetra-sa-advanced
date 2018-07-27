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
import ec.tstoolkit.timeseries.regression.ILengthOfPeriodVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author palatej
 */
public class MovingTradingDaysEstimator {

    private PreprocessingModel model;
    private Matrix td, tdCoefficients, rawTdCoefficients;
    private double[] startTdCoefficients;
    private double[] startTdStde;
    private TsData partialLinearizedSeries, tdEffect;
    private int ny, cbeg, cend;
    private TsVariableSelection variables;

    private final SymmetricFilter sfilter;
    private final AsymmetricEndPoints endPoints;
    private final int windowLength;
    private final boolean reestimate;

    public MovingTradingDaysEstimator(MovingTradingDaysSpecification spec) {
        this.windowLength = spec.getWindowLength();
        this.reestimate = spec.isReestimate();
        this.sfilter = spec.getSmoother();
        this.endPoints = spec.getEndPoints();
    }

    public boolean process(PreprocessingModel model) {
        this.model = model;
        try {
            // initial 
            if (!processFullModel()) {
                return false;
            }
            if (!computeRawCoefficients())
                return false;
            if (!smoothCoefficients())
                return false;
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

    private boolean processFullModel() {
        // search for TD variables
        variables = model.description.buildRegressionVariables(var -> var.getVariable() instanceof ITradingDaysVariable);
        if (variables.isEmpty()) {
            return false;
        }

        ConcentratedLikelihood ll = model.estimation.getLikelihood();
        int ntd = variables.getVariablesCount();
        startTdCoefficients = new double[ntd];
        startTdStde = new double[ntd];
        int n0 = model.description.getRegressionVariablesStartingPosition()+variables.get(0).position;
        double[] b = ll.getB();
        for (int j = 0; j < ntd; ++j) {
            startTdCoefficients[j] = b[n0 + j];
            startTdStde[j] = ll.getBSer(n0 + j, false, 0);
        }
        TsDomain domain = model.description.getSeriesDomain();
        TsData tdeffect = model.deterministicEffect(domain, v -> v instanceof ITradingDaysVariable);
        partialLinearizedSeries = TsData.add(model.linearizedSeries(true), tdeffect);
        td = variables.matrix(domain);
        return true;
    }

    private boolean computeRawCoefficients() {
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
        if (ne <= 1)
            return false;
        rawTdCoefficients = new Matrix(ne, ntd);
        for (int i = 0, i0 = 0, i1 = cbeg + wlen; i < ne;) {
            RegArimaModel<SarimaModel> reg = regarima(partialLinearizedSeries.internalStorage(), getTd(), mean, model.estimation.getArima(), i0, i1);
            if (reestimate) {
                GlsSarimaMonitor monitor = new GlsSarimaMonitor();
                RegArimaEstimation<SarimaModel> estimation = monitor.optimize(reg);
                double[] pb = estimation.likelihood.getB();
                rawTdCoefficients.row(i).copyFrom(pb, mean ? 1 : 0);
            } else {
                double[] pb = reg.computeLikelihood().getB();
                rawTdCoefficients.row(i).copyFrom(pb, mean ? 1 : 0);
            }
            i0 += freq;
            i1 += freq;
            if (++i == ne - 1) {
                i1 += cend;
            }
        }
        return true;
    }

    private boolean smoothCoefficients() {
        // extends the matrix of coefficients
        int r0 = windowLength / 2, r1 = r0;
        if (cbeg > 0) {
            ++r0;
        }
        if (cend > 0) {
            ++r1;
        }
        int lr = r0 + rawTdCoefficients.getRowsCount();
        Matrix x = new Matrix(lr + r1, rawTdCoefficients.getColumnsCount());
        x.subMatrix(r0, r0 + rawTdCoefficients.getRowsCount(), 0, rawTdCoefficients.getColumnsCount()).copy(rawTdCoefficients.all());
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
        if (tdCoefficients.getRowsCount()<= sfilter.getLength())
            return false;
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
        return true;

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
        return rawTdCoefficients;
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

    public TsData tdEffect(TsDomain domain) {
        TsData all = tdEffect.fittoDomain(domain);
        int nbeg = tdEffect.getStart().minus(domain.getStart());
        if (nbeg > 0) {
            TsDomain bdomain = new TsDomain(domain.getStart(), nbeg);
            Matrix m = variables.matrix(bdomain);
            DataBlock c = rawTdCoefficients.row(0);
            for (int i = 0; i < nbeg; ++i) {
                all.set(i, c.dot(m.row(i)));
            }
        }
        int nend = domain.getEnd().minus(tdEffect.getEnd());
        if (nend > 0) {
            TsDomain edomain = new TsDomain(tdEffect.getEnd(), nend);
            Matrix m = variables.matrix(edomain);
            DataBlock c = rawTdCoefficients.row(rawTdCoefficients.getRowsCount() - 1);
            for (int i = 0, j = edomain.getStart().minus(domain.getStart()); i < nend; ++i, ++j) {
                all.set(j, c.dot(m.row(i)));
            }
        }
        return all;
    }

    public TsData fullTdEffect(TsDomain domain) {
        TsData lp = model.deterministicEffect(domain, v->v instanceof ILengthOfPeriodVariable);
        TsData all=TsData.add(lp, tdEffect(domain));
        return all;
    }
}
