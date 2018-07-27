/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.tvtd;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.estimation.SarimaMapping;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.RegSsf;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfAlgorithm;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.SsfFunction;
import ec.tstoolkit.ssf.SsfFunctionInstance;
import ec.tstoolkit.ssf.SsfModel;
import ec.tstoolkit.ssf.TimeVaryingRegSsf;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.timeseries.DayClustering;
import ec.tstoolkit.timeseries.regression.ILengthOfPeriodVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author palatej
 */
public class TimeVaryingTradingDaysEstimator {

    private final boolean onContrasts, airline;
    private PreprocessingModel model;
    private Matrix td, tdCoefficients, stdeTdCoefficients;
    private TsData partialLinearizedSeries, tdEffect;
    private TsVariableSelection variables;
    private double[] fixedTdCoefficients;
    private double[] fixedTdStde;
    private SarimaModel arima0, arima;
    private double aic, aic0;

    /**
     * *
     *
     * @param onContrasts
     * @param airline
     */
    public TimeVaryingTradingDaysEstimator(final boolean onContrasts, final boolean airline) {
        this.onContrasts = onContrasts;
        this.airline = airline;
    }

    public boolean process(PreprocessingModel model) {
        this.model = model;
        try {
            if (!processFullModel()) {
                return false;
            }
            return compute();
        } catch (Exception err) {
            return false;
        }
    }

    private Matrix generateVar(int ntd) {
        DayClustering dc = ntd == 6 ? DayClustering.TD7 : DayClustering.TD2;
        int groupsCount = dc.getGroupsCount();
        Matrix full = Matrix.square(7);
        if (!onContrasts) {
            full.set(-1.0 / 7.0);
        }
        full.diagonal().add(1);
        Matrix Q = new Matrix(groupsCount - 1, 7);
        int[] gdef = dc.getGroupsDefinition();
        for (int i = 1; i < groupsCount; ++i) {
            for (int j = 0; j < 7; ++j) {
                if (gdef[j] == i) {
                    Q.set(i - 1, j, 1);
                }
            }
        }
        return SymmetricMatrix.quadraticFormT(full, Q);
    }

    private boolean compute() {
        try {
            int ntd = variables.getVariablesCount();
            Matrix nvar = generateVar(ntd);
            SsfData data = new SsfData(getPartialLinearizedSeries(), null);
            // step 0 fixed model
            TDvarMapping mapping0 = new TDvarMapping(getPartialLinearizedSeries().getFrequency().intValue(), td, null);
            ReadDataBlock p0 = new ReadDataBlock(new double[]{-.6, -.6});
            // Create the function
            SsfModel<ISsf> model = new SsfModel<>(mapping0.map(p0), new SsfData(getPartialLinearizedSeries(), null), null, null);
            SsfFunction<ISsf> fn0 = new SsfFunction<>(model, mapping0, new SsfAlgorithm());
            SarimaSpecification spec = new SarimaSpecification(getPartialLinearizedSeries().getFrequency().intValue());
            spec.airline();

            LevenbergMarquardtMethod min = new LevenbergMarquardtMethod();
            min.setConvergenceCriterion(1e-12);
            min.minimize(fn0, p0);
            SsfFunctionInstance<ISsf> rfn0 = (SsfFunctionInstance<ISsf>) min.getResult();
            DiffuseConcentratedLikelihood ll0 = rfn0.getLikelihood();
            IReadDataBlock ep0 = rfn0.getParameters();
            arima0 = new SarimaModel(spec);
            getArima0().setTheta(1, ep0.get(0));
            getArima0().setBTheta(1, ep0.get(1));

            TDvarMapping mapping = new TDvarMapping(getPartialLinearizedSeries().getFrequency().intValue(), td, nvar);
            ReadDataBlock p = new ReadDataBlock(new double[]{ep0.get(0), ep0.get(1), 0.0001});
            // Create the function
            model = new SsfModel<>(mapping.map(p), new SsfData(getPartialLinearizedSeries(), null), null, null);
            SsfFunction<ISsf> fn = new SsfFunction<>(model, mapping, new SsfAlgorithm());
            min.minimize(fn, p);
            SsfFunctionInstance<ISsf> rfn = (SsfFunctionInstance<ISsf>) min.getResult();
            DiffuseConcentratedLikelihood ll = rfn.getLikelihood();
            IReadDataBlock ep = rfn.getParameters();
            SarimaModel arima = new SarimaModel(spec);
            arima.setTheta(1, ep.get(0));
            arima.setBTheta(1, ep.get(1));
            double tdvar = ep.get(2);

            ISsf ssf;
            aic0 = ll0.AIC(2);
            aic = ll.AIC(3);
//            if (aic + diffAic < aic0) {
            ssf = rfn.ssf;
//            } else {
//                ssf = rfn0.ssf;
//            }

            Smoother smoother = new Smoother();
            smoother.setSsf(ssf);
            smoother.setCalcVar(true);
            SmoothingResults fs = new SmoothingResults(true, true);
            smoother.process(data, fs);
            Matrix c = new Matrix(td.getRowsCount(), td.getColumnsCount());
            Matrix ec = new Matrix(td.getRowsCount(), td.getColumnsCount());

            int del = partialLinearizedSeries.getFrequency().intValue() + 2;
            for (int i = 0; i < td.getColumnsCount(); ++i) {
                c.column(i).copyFrom(fs.component(del + i), 0);
                ec.column(i).copyFrom(fs.componentVar(del + i), 0);
            }

            double[] sec = ec.internalStorage();
            for (int i = 0; i < sec.length; ++i) {
                sec[i] = sec[i] <= 0 ? 0 : Math.sqrt(sec[i]);
            }
            tdCoefficients = c;
            stdeTdCoefficients = ec;

            tdEffect = new TsData(this.model.description.getSeriesDomain());
            for (int i = 0; i < c.getRowsCount(); ++i) {
                tdEffect.set(i, c.row(i).dot(td.row(i)));
            }

            return true;
        } catch (Exception err) {
            return false;
        }
    }

    private boolean processFullModel() {
        // search for TD variables
        variables = model.description.buildRegressionVariables(var -> var.getVariable() instanceof ITradingDaysVariable);
        if (variables.isEmpty()) {
            return false;
        }

        ConcentratedLikelihood ll = model.estimation.getLikelihood();
        int ntd = variables.getVariablesCount();
        fixedTdCoefficients = new double[ntd];
        fixedTdStde = new double[ntd];
        int n0 = model.description.getRegressionVariablesStartingPosition() + variables.get(0).position;
        double[] b = ll.getB();
        for (int j = 0; j < ntd; ++j) {
            fixedTdCoefficients[j] = b[n0 + j];
            fixedTdStde[j] = ll.getBSer(n0 + j, false, 0);
        }
        TsDomain domain = model.description.getSeriesDomain();
        TsData tdeffect = model.deterministicEffect(domain, v -> v instanceof ITradingDaysVariable);
        partialLinearizedSeries = TsData.add(model.linearizedSeries(true), tdeffect);
        td = variables.matrix(domain);
        return true;
    }

    public TsData tdEffect(TsDomain domain) {
        TsData all = getTdEffect().fittoDomain(domain);
        int nbeg = getTdEffect().getStart().minus(domain.getStart());
        if (nbeg > 0) {
            TsDomain bdomain = new TsDomain(domain.getStart(), nbeg);
            Matrix m = variables.matrix(bdomain);
            DataBlock c = getTdCoefficients().row(0);
            for (int i = 0; i < nbeg; ++i) {
                all.set(i, c.dot(m.row(i)));
            }
        }
        int nend = domain.getEnd().minus(getTdEffect().getEnd());
        if (nend > 0) {
            TsDomain edomain = new TsDomain(getTdEffect().getEnd(), nend);
            Matrix m = variables.matrix(edomain);
            DataBlock c = getTdCoefficients().row(getTdCoefficients().getRowsCount() - 1);
            for (int i = 0, j = edomain.getStart().minus(domain.getStart()); i < nend; ++i, ++j) {
                all.set(j, c.dot(m.row(i)));
            }
        }
        return all;
    }

    public TsData fullTdEffect(TsDomain domain) {
        TsData lp = model.deterministicEffect(domain, v -> v instanceof ILengthOfPeriodVariable);
        TsData all = TsData.add(lp, tdEffect(domain));
        return all;
    }

    /**
     * @return the tdCoefficients
     */
    public Matrix getTdCoefficients() {
        return tdCoefficients;
    }

    /**
     * @param tdCoefficients the tdCoefficients to set
     */
    public void setTdCoefficients(Matrix tdCoefficients) {
        this.tdCoefficients = tdCoefficients;
    }

    /**
     * @return the stdeTdCoefficients
     */
    public Matrix getStdeTdCoefficients() {
        return stdeTdCoefficients;
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

    /**
     * @return the tdEffect
     */
    public TsData getFullTdEffect() {
        return fullTdEffect(tdEffect.getDomain());
    }
    /**
     * @return the fixedTdCoefficients
     */
    public double[] getFixedTdCoefficients() {
        return fixedTdCoefficients;
    }

    /**
     * @return the fixedTdStde
     */
    public double[] getFixedTdStde() {
        return fixedTdStde;
    }

    /**
     * @return the arima0
     */
    public SarimaModel getArima0() {
        return arima0;
    }

    /**
     * @return the arima
     */
    public SarimaModel getArima() {
        return arima;
    }

    /**
     * @return the aic
     */
    public double getAic() {
        return aic;
    }

    /**
     * @return the aic0
     */
    public double getAic0() {
        return aic0;
    }

    PreprocessingModel getModel() {
        return model;
    }

    private static class TDvarMapping implements IParametricMapping<ISsf> {

        private final int frequency;
        private final Matrix td; // regression variable
        private final Matrix nvar; // unscaled covariance matrix for var coefficients
        private static final SarimaMapping AIRLINEMAPPING;

        static {
            SarimaSpecification spec = new SarimaSpecification(12);
            spec.airline();
            AIRLINEMAPPING = new SarimaMapping(spec, true);
        }

        TDvarMapping(int freq, Matrix td, Matrix nvar) {
            this.frequency = freq;
            this.td = td;
            this.nvar = nvar;
        }

        @Override
        public ISsf map(IReadDataBlock p) {
            SarimaSpecification spec = new SarimaSpecification(frequency);
            spec.airline();
            SarimaModel arima = new SarimaModel(spec);
            arima.setTheta(1, p.get(0));
            arima.setBTheta(1, p.get(1));
            SsfArima ssf = new SsfArima(arima);
            if (nvar != null) {
                double nv = p.get(2);
                Matrix v = nvar.clone();
                v.mul(nv);
                return new TimeVaryingRegSsf(ssf, td.all(), v);
            } else {
                return new RegSsf(ssf, td.all());
            }
        }

        @Override
        public IReadDataBlock map(ISsf t) {
            if (t instanceof TimeVaryingRegSsf) {
                TimeVaryingRegSsf ssf = (TimeVaryingRegSsf) t;
                SsfArima ssfarima = (SsfArima) ssf.getCoreSsf();
                SarimaModel arima = (SarimaModel) ssfarima.getModel();
                Matrix fnv = ssf.getFullNoiseVar();
                double[] p = new double[]{arima.theta(1), arima.btheta(1), fnv.diagonal().sum() / nvar.diagonal().sum()};
                return new ReadDataBlock(p);
            } else {
                RegSsf ssf = (RegSsf) t;
                SsfArima ssfarima = (SsfArima) ssf.getCoreSsf();
                SarimaModel arima = (SarimaModel) ssfarima.getModel();
                double[] p = new double[]{arima.theta(1), arima.btheta(1)};
                return new ReadDataBlock(p);

            }
        }

        @Override
        public boolean checkBoundaries(IReadDataBlock inparams) {
            if (nvar != null) {
                return inparams.get(2) >= 0 && inparams.get(2) < 10 && AIRLINEMAPPING.checkBoundaries(inparams.rextract(0, 2));
            } else {
                return AIRLINEMAPPING.checkBoundaries(inparams.rextract(0, 2));
            }
        }

        @Override
        public double epsilon(IReadDataBlock inparams, int idx) {
            if (idx < 2) {
                return AIRLINEMAPPING.epsilon(inparams, idx);
            }
            return Math.max(inparams.get(2) * .001, 1e-9);
        }

        @Override
        public int getDim() {
            return nvar == null ? 2 : 3;
        }

        @Override
        public double lbound(int idx) {
            if (idx < 2) {
                return AIRLINEMAPPING.lbound(idx);
            } else {
                return 0;
            }
        }

        @Override
        public double ubound(int idx) {
            if (idx < 2) {
                return AIRLINEMAPPING.ubound(idx);
            } else {
                return 10;
            }
        }

        @Override
        public ParamValidation validate(IDataBlock ioparams) {
            ParamValidation pv = ParamValidation.Valid;
            if (nvar != null && ioparams.get(2) < 0) {
                pv = ParamValidation.Changed;
                ioparams.set(2, Math.min(10, -ioparams.get(2)));
            }
            if (nvar != null && ioparams.get(2) > 10) {
                pv = ParamValidation.Changed;
                ioparams.set(2, 10);
            }
            ParamValidation pv2 = AIRLINEMAPPING.validate(ioparams.extract(0, 2));
            if (pv == ParamValidation.Valid && pv2 == ParamValidation.Valid) {
                return ParamValidation.Valid;
            }
            if (pv == ParamValidation.Invalid || pv2 == ParamValidation.Invalid) {
                return ParamValidation.Invalid;
            }
            return ParamValidation.Changed;
        }

        @Override
        public String getDescription(int idx) {
            if (idx < 2) {
                return AIRLINEMAPPING.getDescription(idx);
            } else {
                return "noise stdev";
            }
        }
    }
}
