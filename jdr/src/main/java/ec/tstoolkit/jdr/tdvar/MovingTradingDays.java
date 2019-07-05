/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.tdvar;

import be.nbb.demetra.mtd.MovingTradingDaysEstimator;
import be.nbb.demetra.mtd.MovingTradingDaysSaProcessor;
import be.nbb.demetra.mtd.MovingTradingDaysSaResults;
import be.nbb.demetra.mtd.MovingTradingDaysSpecification;
import be.nbb.demetra.tvtd.TimeVaryingTradingDaysSaProcessor;
import be.nbb.demetra.tvtd.TimeVaryingTradingDaysSaResults;
import be.nbb.demetra.tvtd.TimeVaryingTradingDaysSpecification;
import demetra.algorithm.IProcResults;
import demetra.information.InformationMapping;
import ec.satoolkit.x11.SeasonalFilterFactory;
import ec.satoolkit.x11.X11Specification;
import ec.tstoolkit.jdr.mapping.SarimaInfo;
import ec.tstoolkit.jdr.regarima.RegArimaInfo;
import ec.tstoolkit.jdr.sa.SaDecompositionInfo;
import ec.tstoolkit.jdr.x11.MstatisticsInfo;
import ec.tstoolkit.jdr.x11.X11DecompositionInfo;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class MovingTradingDays {

    @lombok.Value
    public static class Results implements IProcResults {

        private static final String REGARIMA = "regarima", TD = "mtd.tdenolp", TDE = "mtd.tde", LIN = "mtd.partiallinearizedseries",
                RAWCOEF = "mtd.rawcoefficients", SMOOTHCOEF = "mtd.smoothedcoefficients";

        private MovingTradingDaysSaResults core;

        private static final InformationMapping<Results> MAPPING = new InformationMapping<>(Results.class);

        static {
            MAPPING.delegate(REGARIMA, RegArimaInfo.getMapping(), r -> r.core.getPreprocessing());
            MAPPING.set(TD, TsData.class, r
                    -> {
                TsData s = r.core.getMovingTradingDaysCorrection().getTdEffect().clone();
                r.core.getPreprocessing().backTransform(s, false, false);
                return s;
            });
            MAPPING.set(TDE, TsData.class, r
                    -> {
                TsData s = r.core.getMovingTradingDaysCorrection().getFullTdEffect().clone();
                r.core.getPreprocessing().backTransform(s, false, false);
                return s;
            });
            MAPPING.set(LIN, TsData.class, r->r.core.getMovingTradingDaysCorrection().getPartialLinearizedSeries());
            MAPPING.set(RAWCOEF, Matrix.class, r -> r.core.getMovingTradingDaysCorrection().getRawCoefficients());
            MAPPING.set(SMOOTHCOEF, Matrix.class, r -> r.core.getMovingTradingDaysCorrection().getSmoothedCoefficients());
            MAPPING.delegate("decomposition", X11DecompositionInfo.getMapping(), r -> r.core.getDecomposition());
            MAPPING.delegate("mstatistics", MstatisticsInfo.getMapping(), r -> r.core.getMstatistics());
            MAPPING.delegate(null, SaDecompositionInfo.getMapping(), r -> r.core.getFinalDecomposition());
        }

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }
    }

    @lombok.Value
    public static class Results2 implements IProcResults {

        private static final String REGARIMA = "regarima", TD = "tvtd.tdenolp", TDE = "tvtd.tde", LIN = "tvtd.partiallinearizedseries",
                COEF = "tvtd.coefficients", ECOEF = "tvtd.stdecoefficients";

        private TimeVaryingTradingDaysSaResults core;

        private static final InformationMapping<Results2> MAPPING = new InformationMapping<>(Results2.class);

        static {
            MAPPING.delegate(REGARIMA, RegArimaInfo.getMapping(), r -> r.core.getPreprocessing());
            MAPPING.set(TD, TsData.class, r
                    -> {
                TsData s = r.core.getTimeVaryingTradingDaysCorrection().getTdEffect().clone();
                r.core.getPreprocessing().backTransform(s, false, false);
                return s;
            });
            MAPPING.set(TDE, TsData.class, r
                    -> {
                TsData s = r.core.getTimeVaryingTradingDaysCorrection().getFullTdEffect().clone();
                r.core.getPreprocessing().backTransform(s, false, false);
                return s;
            });
            MAPPING.set(LIN, TsData.class, r->r.core.getTimeVaryingTradingDaysCorrection().getPartialLinearizedSeries());
            MAPPING.set(COEF, Matrix.class, r -> r.core.getTimeVaryingTradingDaysCorrection().getTdCoefficients());
            MAPPING.set(ECOEF, Matrix.class, r -> r.core.getTimeVaryingTradingDaysCorrection().getStdeTdCoefficients());
            MAPPING.set("tvtd.aic", Double.class, r->r.core.getTimeVaryingTradingDaysCorrection().getAic());
            MAPPING.set("tvtd.aic0", Double.class, r->r.core.getTimeVaryingTradingDaysCorrection().getAic0());
            MAPPING.delegate("tvtd.arima", SarimaInfo.getMapping(), r->r.core.getTimeVaryingTradingDaysCorrection().getArima());
            MAPPING.delegate("tvtd.arima0", SarimaInfo.getMapping(), r->r.core.getTimeVaryingTradingDaysCorrection().getArima0());
            MAPPING.delegate("decomposition", X11DecompositionInfo.getMapping(), r -> r.core.getDecomposition());
            MAPPING.delegate("mstatistics", MstatisticsInfo.getMapping(), r -> r.core.getMstatistics());
            MAPPING.delegate(null, SaDecompositionInfo.getMapping(), r -> r.core.getFinalDecomposition());
        }

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }

        public static final InformationMapping<Results2> getMapping() {
            return MAPPING;
        }
    }

    public Results movingWindow(TsData s, int windowLength, int smoothingLength, String preprocessing, String option, boolean reestimate) {
        MovingTradingDaysSpecification mtdSpec = mtdSpec(windowLength, smoothingLength, reestimate);
        X11Specification xspec = new X11Specification();
        boolean air = option.endsWith("a");
        if (air) {
            option = option.substring(0, option.length() - 1);
        }

        if (preprocessing.equalsIgnoreCase("TRAMO")) {
            TramoSpecification spec = TramoSpecification.fromString(option).clone();
            if (spec == null) {
                return null;
            }
            if (air) {
                spec.setUsingAutoModel(false);
                spec.getArima().airline();
            }
            return new Results(MovingTradingDaysSaProcessor.process(s, spec, mtdSpec, xspec, null));
        } else if (preprocessing.equalsIgnoreCase("REGARIMA")) {
            RegArimaSpecification spec = RegArimaSpecification.fromString(option);
            if (spec == null) {
                return null;
            }
            if (air) {
                spec.setUsingAutoModel(false);
                spec.getArima().airline();
            }
            return new Results(MovingTradingDaysSaProcessor.process(s, spec, mtdSpec, xspec, null));
        } else {
            return null;
        }
    }

    public Results2 timeVarying(TsData s, boolean contrasts, String preprocessing, String option) {
        TimeVaryingTradingDaysSpecification mtdSpec = new TimeVaryingTradingDaysSpecification();
        mtdSpec.setOnContrast(contrasts);
        X11Specification xspec = new X11Specification();
        boolean air = option.endsWith("a");
        if (air) {
            option = option.substring(0, option.length() - 1);
        }

        if (preprocessing.equalsIgnoreCase("TRAMO")) {
            TramoSpecification spec = TramoSpecification.fromString(option).clone();
            if (spec == null) {
                return null;
            }
            if (air) {
                spec.setUsingAutoModel(false);
                spec.getArima().airline();
            }
            return new Results2(TimeVaryingTradingDaysSaProcessor.process(s, spec, mtdSpec, xspec, null));
        } else if (preprocessing.equalsIgnoreCase("REGARIMA")) {
            RegArimaSpecification spec = RegArimaSpecification.fromString(option);
            if (spec == null) {
                return null;
            }
            if (air) {
                spec.setUsingAutoModel(false);
                spec.getArima().airline();
            }
            return new Results2(TimeVaryingTradingDaysSaProcessor.process(s, spec, mtdSpec, xspec, null));
        } else {
            return null;
        }
    }

    public MovingTradingDaysSpecification mtdSpec(int windowLength, int smoothingLength, boolean reestimate) {
        SymmetricFilter sfilter = SeasonalFilterFactory.S3X3;
        switch (smoothingLength) {
            case 3:
                sfilter = SeasonalFilterFactory.S3X1;
                break;
            case 5:
                sfilter = SeasonalFilterFactory.S3X3;
                break;
            case 7:
                sfilter = SeasonalFilterFactory.S3X5;
                break;
            case 11:
                sfilter = SeasonalFilterFactory.S3X9;
                break;
        }
        MovingTradingDaysSpecification mspec = new MovingTradingDaysSpecification();
        mspec.setWindowLength(windowLength);
        mspec.setReestimate(reestimate);
        mspec.setSmoother(sfilter);
        return mspec;
    }

}
