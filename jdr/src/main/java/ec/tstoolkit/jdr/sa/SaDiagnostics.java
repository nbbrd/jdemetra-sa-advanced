/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.sa;

import jd2.algorithm.IProcResults;
import jd2.information.InformationMapping;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.ISaResults;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.diagnostics.CombinedSeasonalityTest;
import ec.satoolkit.diagnostics.FTest;
import ec.satoolkit.diagnostics.KruskalWallisTest;
import ec.satoolkit.diagnostics.StationaryVarianceDecomposition;
import ec.satoolkit.x11.DefaultSeasonalFilteringStrategy;
import ec.satoolkit.x11.DefaultTrendFilteringStrategy;
import ec.satoolkit.x11.FilterFactory;
import ec.satoolkit.x11.FilteredMeanEndPoints;
import ec.satoolkit.x11.IFiltering;
import ec.satoolkit.x11.MsrTable;
import ec.satoolkit.x11.SeriesEvolution;
import ec.satoolkit.x11.TrendCycleFilterFactory;
import ec.satoolkit.x11.X11Kernel;
import ec.satoolkit.x11.X11Results;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.data.AutoCorrelations;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.jdr.tests.CombinedSeasonalityTestInfo;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.modelling.DifferencingResults;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.arima.JointRegressionTest;
import ec.tstoolkit.modelling.arima.PreprocessingDictionary;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.diagnostics.OneStepAheadForecastingTest;
import ec.tstoolkit.modelling.arima.tramo.SeasonalityTests;
import ec.tstoolkit.modelling.arima.tramo.SpectralPeaks;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SaDiagnostics implements IProcResults {

    static final InformationMapping<SaDiagnostics> MAPPING = new InformationMapping<>(SaDiagnostics.class);

    public static InformationMapping<SaDiagnostics> getMapping() {
        return MAPPING;
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

//    
//}
    public static SaDiagnostics of(PreprocessingModel regarima, ISaResults decomposition, ISeriesDecomposition finals) {
        if (decomposition == null || finals == null) {
            return null;
        }
        return new SaDiagnostics(regarima, decomposition, finals);
    }

    private final PreprocessingModel regarima;
    private final ISaResults decomposition;
    private final ISeriesDecomposition finals;
    private final boolean mul;
    private final TsData lin, res, sa, irr, si, s, t;

    private SeasonalityTests ytests, rtests, satests, itests;
    private CombinedSeasonalityTest seasSI, seasSa, seasI, seasRes, seasSI3, seasSa3, seasI3, seasRes3;
    private OneStepAheadForecastingTest outOfSampleTest;
    private MsrTable msr;
    private StationaryVarianceDecomposition varDecomposition;

    private SaDiagnostics(PreprocessingModel regarima, ISaResults decomposition, ISeriesDecomposition finals) {
        this.regarima = regarima;
        this.decomposition = decomposition;
        this.finals = finals;
        sa = decomposition.getData(ModellingDictionary.SA_CMP, TsData.class);
        t = decomposition.getData(ModellingDictionary.T_CMP, TsData.class);
        s = decomposition.getData(ModellingDictionary.S_CMP, TsData.class);
        irr = decomposition.getData(ModellingDictionary.I_CMP, TsData.class);
        mul = decomposition.getSeriesDecomposition().getMode().isMultiplicative();
        if (regarima == null) {
            lin = decomposition.getData(ModellingDictionary.Y_CMP, TsData.class);
            res = null;
        } else {
            lin = regarima.linearizedSeries();
            res = regarima.getFullResiduals();
        }
        if (decomposition instanceof X11Results) {
            si = ((X11Results) decomposition).getData(InformationSet.concatenate(X11Kernel.D, X11Kernel.D8), TsData.class);
        } else if (mul) {
            si = TsData.multiply(s, irr);
        } else {
            si = TsData.add(s, irr);
        }
    }

    private StationaryVarianceDecomposition varDecomposition() {
        if (varDecomposition == null) {
            try {
                TsData O = regarima == null ? regarima.getData(ModellingDictionary.YC, TsData.class)
                        : finals.getData(ModellingDictionary.Y, TsData.class);
                TsData Cal = regarima == null ? null : regarima.getData(ModellingDictionary.CAL, TsData.class);
                TsData D = regarima == null ? null : regarima.getData(ModellingDictionary.DET, TsData.class);
                TsData P = mul ? TsData.divide(D, Cal) : TsData.subtract(D, Cal);
                varDecomposition = new StationaryVarianceDecomposition();
                varDecomposition.process(O, t, s, irr, Cal, P, mul);
            } catch (Exception err) {
                return null;
            }
        }
        return varDecomposition;
    }

    private SeasonalityTests resTests() {
        if (res == null) {
            return null;
        }
        if (rtests == null) {
            rtests = SeasonalityTests.seasonalityTest(res, 0, false, true);
        }
        return rtests;
    }

    private SeasonalityTests yTests() {
        if (ytests == null) {
            ytests = SeasonalityTests.seasonalityTest(lin, 1, true, true);
        }
        return ytests;
    }

    private SeasonalityTests saTests() {
        if (satests == null) {
            satests = SeasonalityTests.seasonalityTest(mul ? sa.log() : sa, 1, true, true);
        }
        return satests;
    }

    private SeasonalityTests irrTests() {
        if (itests == null) {
            itests = SeasonalityTests.seasonalityTest(mul ? irr.log() : irr, 0, true, true);
        }
        return itests;
    }

    private CombinedSeasonalityTest csiTest(boolean last) {
        if (last) {
            if (seasSI3 == null) {
                int freq = si.getFrequency().intValue();
                TsPeriodSelector sel = new TsPeriodSelector();
                sel.last(freq * 3);
                seasSI3 = new CombinedSeasonalityTest(si.select(sel), mul);
            }
            return seasSI3;
        } else {
            if (seasSI == null) {
                seasSI = new CombinedSeasonalityTest(si, mul);
            }
            return seasSI;
        }
    }

    private TsData dsa() {
        int freq = sa.getFrequency().intValue();
        return sa.delta(Math.max(1, freq / 4));
    }

    private CombinedSeasonalityTest csaTest(boolean last) {
        if (last) {
            if (seasSa3 == null) {
                int freq = sa.getFrequency().intValue();
                TsData ts = sa.delta(Math.max(1, freq / 4));
                TsPeriodSelector sel = new TsPeriodSelector();
                sel.last(freq * 3);
                seasSa3 = new CombinedSeasonalityTest(ts.select(sel), mul);
            }
            return seasSa3;
        } else {
            if (seasSa == null) {
                int freq = sa.getFrequency().intValue();
                TsData ts = sa.delta(Math.max(1, freq / 4));
                seasSa = new CombinedSeasonalityTest(ts, mul);
            }
            return seasSa;
        }
    }

    private CombinedSeasonalityTest cresTest(boolean last) {
        if (res == null) {
            return null;
        }
        int freq = res.getFrequency().intValue();
        if (res.getLength() < 3 * freq) {
            return null;
        }
        if (last) {
            if (seasRes3 == null) {
                TsPeriodSelector sel = new TsPeriodSelector();
                sel.last(freq * 3);
                seasRes3 = new CombinedSeasonalityTest(res.select(sel), false);
            }
            return seasRes3;
        } else {
            if (seasRes == null) {
                seasRes = new CombinedSeasonalityTest(res, false);
            }
            return seasRes;
        }
    }

    private CombinedSeasonalityTest ciTest(boolean last) {
        if (last) {
            if (seasI3 == null) {
                int freq = irr.getFrequency().intValue();
                TsPeriodSelector sel = new TsPeriodSelector();
                sel.last(freq * 3);
                seasI3 = new CombinedSeasonalityTest(irr.select(sel), mul);
            }
            return seasI3;
        } else {
            if (seasI == null) {
                seasI = new CombinedSeasonalityTest(irr, mul);
            }
            return seasI;
        }
    }

    private static final double FLEN = 1.5;

    private OneStepAheadForecastingTest forecastingTest() {
        if (regarima == null) {
            return null;
        }
        if (outOfSampleTest == null) {
            try {
                int ifreq = regarima.description.getFrequency();
                int nback = (int) (FLEN * ifreq);
                if (nback < 5) {
                    nback = 5;
                }
                OneStepAheadForecastingTest os = new OneStepAheadForecastingTest(nback);
                if (!os.test(regarima.estimation.getRegArima())) {
                    return null;
                }
                outOfSampleTest = os;
            } catch (Exception err) {

            }
        }
        return outOfSampleTest;
    }

    private static StatisticalTest tdAr(TsData s) {
        try {
            RegModel reg = new RegModel();
            DataBlock y = new DataBlock(s);
            reg.setY(y.drop(1, 0));
            GregorianCalendarVariables tdvars = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
            int ntd = tdvars.getDim();
            TsDomain edomain = s.getDomain().drop(1, 0);
            List<DataBlock> bvars = new ArrayList<>(ntd);
            for (int i = 0; i < ntd; ++i) {
                bvars.add(new DataBlock(edomain.getLength()));
            }
            tdvars.data(edomain, bvars);
            //       BackFilter ur = context.description.getArimaComponent().getDifferencingFilter();
            reg.addX(y.drop(0, 1));
            for (int i = 0; i < ntd; ++i) {
                DataBlock cur = bvars.get(i);
                reg.addX(cur);
            }
            Ols ols = new Ols();
            reg.setMeanCorrection(true);
            if (!ols.process(reg)) {
                return null;
            }
            JointRegressionTest test = new JointRegressionTest(.01);
            test.accept(ols.getLikelihood(), 0, 2, ntd, null);
            return test.getTest();
        } catch (Exception err) {
            return null;
        }
    }

    private static ec.tstoolkit.stats.StatisticalTest td(TsData s) {
        try {
            RegModel reg = new RegModel();
            DataBlock y = new DataBlock(s);
            y.add(-y.average());
            reg.setY(y);
            GregorianCalendarVariables tdvars = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
            int ntd = tdvars.getDim();
            TsDomain edomain = s.getDomain();
            List<DataBlock> bvars = new ArrayList<>(ntd);
            for (int i = 0; i < ntd; ++i) {
                bvars.add(new DataBlock(edomain.getLength()));
            }
            tdvars.data(edomain, bvars);
            //       BackFilter ur = context.description.getArimaComponent().getDifferencingFilter();
            for (int i = 0; i < ntd; ++i) {
                DataBlock cur = bvars.get(i);
                reg.addX(cur);
            }
            //           reg.setMeanCorrection(true);
            Ols ols = new Ols();

            if (!ols.process(reg)) {
                return null;
            }
            JointRegressionTest test = new JointRegressionTest(.01);
            test.accept(ols.getLikelihood(), 0, 0, ntd, null);
            return test.getTest();
        } catch (Exception err) {
            return null;
        }
    }

    private MsrTable msr() {
        if (msr == null) {
            if (s != null) {
                if (decomposition instanceof X11Results) {
                    DecompositionMode mode = decomposition.getSeriesDecomposition().getMode();
                    SymmetricFilter f7 = FilterFactory.makeSymmetricFilter(7);
                    DefaultSeasonalFilteringStrategy fseas = new DefaultSeasonalFilteringStrategy(
                            f7, new FilteredMeanEndPoints(f7));
                    TsData d1 = decomposition.getData("d-tables.d1", TsData.class);
                    TsData d7 = decomposition.getData("d-tables.d7", TsData.class);
                    TsData d9;
                    switch (mode) {
                        case Multiplicative:
                        case PseudoAdditive:
                            d9 = TsData.divide(d1, d7);
                            break;
                        case LogAdditive:
                            d9 = TsData.divide(d1, d7).log();
                            break;
                        default:
                            d9 = TsData.subtract(d1, d7);
                    }
                    TsData s1 = fseas.process(d9, null);
                    TsData s2;
                    switch (mode) {
                        case Multiplicative:
                            s2 = TsData.divide(d9, s1);
                            break;
                        case PseudoAdditive:
                            s2 = d9.minus(s1).plus(1);
                            break;
                        default:
                            s2 = TsData.subtract(d9, s1);
                    }
                    return MsrTable.create(s1, s2,
                            mode == DecompositionMode.Multiplicative
                            || mode == DecompositionMode.PseudoAdditive);
                } else {
                    msr = MsrTable.create(s, irr, mul);
                }
            }
        }
        return msr;
    }

    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.emptyList();
    }

    double[] allVariances() {
        StationaryVarianceDecomposition vd = varDecomposition();
        if (vd == null) {
            return null;
        }
        return new double[]{
            vd.getVarC(),
            vd.getVarS(),
            vd.getVarI(),
            vd.getVarTD(),
            vd.getVarP(),
            vd.getVarTotal()
        };
    }

    public static final String SEAS_LIN_QS = "seas-lin-qs",
            SEAS_LIN_F = "seas-lin-f",
            SEAS_LIN_FRIEDMAN = "seas-lin-friedman",
            SEAS_LIN_KW = "seas-lin-kw",
            SEAS_LIN_PERIODOGRAM = "seas-lin-periodogram",
            SEAS_LIN_SP = "seas-lin-spectralpeaks",
            SEAS_SI_COMBINED = "seas-si-combined",
            SEAS_SI_EVOLUTIVE = "seas-si-evolutive",
            SEAS_SI_STABLE = "seas-si-stable",
            SEAS_RES_QS = "seas-res-qs",
            SEAS_RES_F = "seas-res-f",
            SEAS_RES_FRIEDMAN = "seas-res-friedman",
            SEAS_RES_KW = "seas-res-kw",
            SEAS_RES_PERIODOGRAM = "seas-res-periodogram",
            SEAS_RES_COMBINED = "seas-res-combined",
            SEAS_RES_COMBINED3 = "seas-res-combined3",
            SEAS_RES_EVOLUTIVE = "seas-res-evolutive",
            SEAS_RES_STABLE = "seas-res-stable",
            SEAS_RES_SP = "seas-res-spectralpeaks",
            SEAS_SA_QS = "seas-sa-qs",
            SEAS_SA_F = "seas-sa-f",
            SEAS_SA_FRIEDMAN = "seas-sa-friedman",
            SEAS_SA_KW = "seas-sa-kw",
            SEAS_SA_PERIODOGRAM = "seas-sa-periodogram",
            SEAS_SA_COMBINED = "seas-sa-combined",
            SEAS_SA_COMBINED3 = "seas-sa-combined3",
            SEAS_SA_STABLE = "seas-sa-stable",
            SEAS_SA_EVOLUTIVE = "seas-sa-evolutive",
            SEAS_SA_SP = "seas-sa-spectralpeaks",
            SEAS_SA_AC1 = "seas-sa-ac1",
            SEAS_I_QS = "seas-i-qs",
            SEAS_I_F = "seas-i-f",
            SEAS_I_FRIEDMAN = "seas-i-friedman",
            SEAS_I_KW = "seas-i-kw",
            SEAS_I_PERIODOGRAM = "seas-i-periodogram",
            SEAS_I_COMBINED = "seas-i-combined",
            SEAS_I_COMBINED3 = "seas-i-combined3",
            SEAS_I_STABLE = "seas-i-stable",
            SEAS_I_EVOLUTIVE = "seas-i-evolutive",
            SEAS_I_SP = "seas-i-spectralpeaks",
            FCAST_INSAMPLE_MEAN = "fcast-insample-mean",
            FCAST_OUTSAMPLE_MEAN = "fcast-outsample-mean",
            FCAST_OUTSAMPLE_VARIANCE = "fcast-outsample-variance",
            LOG_STAT = "logstat",
            LEVEL_STAT = "levelstat",
            TD_RES_ALL = "td-res-all",
            TD_RES_LAST = "td-res-last",
            TD_I_ALL = "td-i-all",
            TD_I_LAST = "td-i-last",
            TD_SA_ALL = "td-sa-all",
            TD_SA_LAST = "td-sa-last",
            IC_RATIO = "ic-ratio",
            IC_RATIO_HENDERSON = "ic-ratio-henderson",
            MSR_GLOBAL = "msr-global",
            MSR = "msr";

    static {
        MAPPING.set("qs", ec.tstoolkit.information.StatisticalTest.class,
                source -> {
                    StatisticalTest qs = source.saTests().getQs();
                    if (qs == null) {
                        return null;
                    } else {
                        return ec.tstoolkit.information.StatisticalTest.of(qs);
                    }
                });

        MAPPING.set("ftest", ec.tstoolkit.information.StatisticalTest.class,
                source -> {
                    FTest ftest = new FTest();
                    if (ftest.test(source.sa)) {
                        return ec.tstoolkit.information.StatisticalTest.of(ftest.getFTest());
                    } else {
                        return null;
                    }
                });

        MAPPING.set("qs.on.i", ec.tstoolkit.information.StatisticalTest.class,
                source -> {
                    StatisticalTest qs = source.irrTests().getQs();
                    if (qs == null) {
                        return null;
                    } else {
                        return ec.tstoolkit.information.StatisticalTest.of(qs);
                    }
                });
        MAPPING.set("ftest.on.i", ec.tstoolkit.information.StatisticalTest.class,
                source -> {
                    FTest ftest = new FTest();
                    if (ftest.test(source.irr)) {
                        return ec.tstoolkit.information.StatisticalTest.of(ftest.getFTest());
                    } else {
                        return null;
                    }
                });

        MAPPING.delegate("combined.all", CombinedSeasonalityTestInfo.getMapping(), source -> source.csiTest(false));
        MAPPING.delegate("combined.end", CombinedSeasonalityTestInfo.getMapping(), source -> source.csiTest(true));
        MAPPING.delegate("combined.residual.all", CombinedSeasonalityTestInfo.getMapping(), source
                -> source.csaTest(false));
        MAPPING.delegate("combined.residual.end", CombinedSeasonalityTestInfo.getMapping(), source
                -> source.csaTest(true));
        MAPPING.set("residual.all", ec.tstoolkit.information.StatisticalTest.class, source
                -> ec.tstoolkit.information.StatisticalTest.of(source.csaTest(false).getStableSeasonality()));
        MAPPING.set("residual.end", ec.tstoolkit.information.StatisticalTest.class, source
                -> ec.tstoolkit.information.StatisticalTest.of(source.csaTest(true).getStableSeasonality()));
        MAPPING.set("residualtd", ec.tstoolkit.information.StatisticalTest.class,
                source -> {
                    TsData s = source.sa;
                    if (source.mul) {
                        s = s.log();
                    }
                    ec.tstoolkit.stats.StatisticalTest test = tdAr(s);
                    if (test == null) {
                        return null;
                    } else {
                        return ec.tstoolkit.information.StatisticalTest.of(test);
                    }
                });
        
        MAPPING.set("residualtd.on.i", ec.tstoolkit.information.StatisticalTest.class, 
                source -> {
                    TsData s = source.irr;
                    if (source.mul) {
                        s = s.log();
                    }
                    ec.tstoolkit.stats.StatisticalTest test = tdAr(s);
                    if (test == null) {
                        return null;
                    } else {
                        return ec.tstoolkit.information.StatisticalTest.of(test);
                    }
                });
        
        MAPPING.set("variancedecomposition", double[].class, source -> source.allVariances());

////////////////////// LOG/LEVEL
        MAPPING.set(LOG_STAT, Double.class, source -> {
            if (source.regarima == null) {
                return null;
            }
            return source.regarima.getData(
                    InformationSet.concatenate(PreprocessingDictionary.TRANSFORMATION, "log"), Double.class);
        });
        MAPPING.set(LEVEL_STAT, Double.class, source -> {
            if (source.regarima == null) {
                return null;
            }
            return source.regarima.getData(
                    InformationSet.concatenate(PreprocessingDictionary.TRANSFORMATION, "level"), Double.class);
        });

////////////////////// FCASTS
        MAPPING.set(FCAST_INSAMPLE_MEAN, ec.tstoolkit.information.StatisticalTest.class, source -> {
            OneStepAheadForecastingTest ftest = source.forecastingTest();
            if (ftest == null) {
                return null;
            }
            return ec.tstoolkit.information.StatisticalTest.of(ftest.inSampleMeanTest());
        });

        MAPPING.set(FCAST_OUTSAMPLE_MEAN, ec.tstoolkit.information.StatisticalTest.class, source -> {
            OneStepAheadForecastingTest ftest = source.forecastingTest();
            if (ftest == null) {
                return null;
            }
            return ec.tstoolkit.information.StatisticalTest.of(ftest.outOfSampleMeanTest());
        });

        MAPPING.set(FCAST_OUTSAMPLE_VARIANCE, ec.tstoolkit.information.StatisticalTest.class, source -> {
            OneStepAheadForecastingTest ftest = source.forecastingTest();
            if (ftest == null) {
                return null;
            }
            return ec.tstoolkit.information.StatisticalTest.of(ftest.mseTest());
        });

        //////////  Y
        MAPPING.set(SEAS_LIN_F, ec.tstoolkit.information.StatisticalTest.class, source -> {
            FTest ftest = new FTest();
            if (ftest.test(source.lin)) {
                return ec.tstoolkit.information.StatisticalTest.of(ftest.getFTest());
            } else {
                return null;
            }
        });

        MAPPING.set(SEAS_LIN_QS, ec.tstoolkit.information.StatisticalTest.class, source -> {
            SeasonalityTests ytests = source.yTests();
            if (ytests == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(ytests.getQs());
            }
        });

        MAPPING.set(SEAS_LIN_KW, ec.tstoolkit.information.StatisticalTest.class, source -> {
            TsData lin = source.lin;
            lin = DifferencingResults.create(lin, 1, true).getDifferenced();
            KruskalWallisTest kw = new KruskalWallisTest(lin);
            return ec.tstoolkit.information.StatisticalTest.of(kw);
        });

        MAPPING.set(SEAS_LIN_FRIEDMAN, ec.tstoolkit.information.StatisticalTest.class, source -> {
            SeasonalityTests ytests = source.yTests();
            if (ytests == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(ytests.getNonParametricTest());
            }
        });

        MAPPING.set(SEAS_LIN_PERIODOGRAM, ec.tstoolkit.information.StatisticalTest.class, source -> {
            SeasonalityTests ytests = source.yTests();
            if (ytests == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(ytests.getPeriodogramTest());
            }
        });

        MAPPING.set(SEAS_LIN_SP, String.class, source -> {
            SeasonalityTests ytests = source.yTests();
            if (ytests == null) {
                return null;
            } else {
                SpectralPeaks[] spectralPeaks = ytests.getSpectralPeaks();
                if (spectralPeaks == null) {
                    return null;
                } else {
                    return SpectralPeaks.format(spectralPeaks);
                }
            }
        });

        MAPPING.set(SEAS_SI_COMBINED, String.class, source -> {
            CombinedSeasonalityTest sitest = source.csiTest(false);
            if (sitest == null) {
                return null;
            } else {
                return sitest.getSummary().name();
            }
        });

        MAPPING.set(SEAS_SI_EVOLUTIVE, ec.tstoolkit.information.StatisticalTest.class, source -> {
            CombinedSeasonalityTest sitest = source.csiTest(false);
            if (sitest == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(sitest.getEvolutiveSeasonality());
            }
        });

        MAPPING.set(SEAS_SI_STABLE, ec.tstoolkit.information.StatisticalTest.class, source -> {
            CombinedSeasonalityTest sitest = source.csiTest(false);
            if (sitest == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(sitest.getStableSeasonality());
            }
        });

        //////////  Residuals 
        MAPPING.set(SEAS_RES_F, ec.tstoolkit.information.StatisticalTest.class, source -> {
            if (source.res == null) {
                return null;
            }
            FTest ftest = new FTest();
            if (ftest.test(source.res)) {
                return ec.tstoolkit.information.StatisticalTest.of(ftest.getFTest());
            } else {
                return null;
            }
        });

        MAPPING.set(SEAS_RES_QS, ec.tstoolkit.information.StatisticalTest.class, source -> {
            SeasonalityTests rtests = source.resTests();
            if (rtests == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(rtests.getQs());
            }
        });

        MAPPING.set(SEAS_RES_KW, ec.tstoolkit.information.StatisticalTest.class, source -> {
            if (source.res == null) {
                return null;
            }
            KruskalWallisTest kw = new KruskalWallisTest(source.res);
            return ec.tstoolkit.information.StatisticalTest.of(kw);
        });

        MAPPING.set(SEAS_RES_FRIEDMAN, ec.tstoolkit.information.StatisticalTest.class, source -> {
            SeasonalityTests rtests = source.resTests();
            if (rtests == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(rtests.getNonParametricTest());
            }
        });
        MAPPING.set(SEAS_RES_PERIODOGRAM, ec.tstoolkit.information.StatisticalTest.class, source -> {
            SeasonalityTests rtests = source.resTests();
            if (rtests == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(rtests.getPeriodogramTest());
            }
        });

        MAPPING.set(SEAS_RES_SP, String.class, source -> {
            SeasonalityTests rtests = source.resTests();
            if (rtests == null) {
                return null;
            } else {
                return SpectralPeaks.format(rtests.getSpectralPeaks());
            }
        });

        MAPPING.set(SEAS_RES_COMBINED, String.class, source -> {
            CombinedSeasonalityTest rtest = source.cresTest(false);
            if (rtest == null) {
                return null;
            } else {
                return rtest.getSummary().name();
            }
        });

        MAPPING.set(SEAS_RES_COMBINED3, String.class, source -> {
            CombinedSeasonalityTest rtest = source.cresTest(true);
            if (rtest == null) {
                return null;
            } else {
                return rtest.getSummary().name();
            }
        });

        MAPPING.set(SEAS_RES_EVOLUTIVE, ec.tstoolkit.information.StatisticalTest.class, source -> {
            CombinedSeasonalityTest rtest = source.cresTest(false);
            if (rtest == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(rtest.getEvolutiveSeasonality());
            }
        });

        MAPPING.set(SEAS_RES_STABLE, ec.tstoolkit.information.StatisticalTest.class, source -> {
            CombinedSeasonalityTest rtest = source.cresTest(false);
            if (rtest == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(rtest.getStableSeasonality());
            }
        });

        /////////////////// Irregular
        MAPPING.set(SEAS_I_F, ec.tstoolkit.information.StatisticalTest.class, source -> {
            FTest ftest = new FTest();
            if (ftest.test(source.irr)) {
                return ec.tstoolkit.information.StatisticalTest.of(ftest.getFTest());
            } else {
                return null;
            }
        });

        MAPPING.set(SEAS_I_QS, ec.tstoolkit.information.StatisticalTest.class, source -> {
            SeasonalityTests itests = source.irrTests();
            if (itests == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(itests.getQs());
            }
        });

        MAPPING.set(SEAS_I_KW, ec.tstoolkit.information.StatisticalTest.class, source -> {
            KruskalWallisTest kw = new KruskalWallisTest(source.irr);
            return ec.tstoolkit.information.StatisticalTest.of(kw);
        });

        MAPPING.set(SEAS_I_PERIODOGRAM, ec.tstoolkit.information.StatisticalTest.class, source -> {
            SeasonalityTests itests = source.irrTests();
            if (itests == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(itests.getPeriodogramTest());
            }
        });

        MAPPING.set(SEAS_I_SP, String.class, source -> {
            SeasonalityTests itests = source.irrTests();
            if (itests == null) {
                return null;
            } else {
                return SpectralPeaks.format(itests.getSpectralPeaks());
            }
        });

        MAPPING.set(SEAS_I_COMBINED, String.class, source -> {
            CombinedSeasonalityTest itest = source.ciTest(false);
            if (itest == null) {
                return null;
            } else {
                return itest.getSummary().name();
            }
        });

        MAPPING.set(SEAS_I_COMBINED3, String.class, source -> {
            CombinedSeasonalityTest itest = source.ciTest(true);
            if (itest == null) {
                return null;
            } else {
                return itest.getSummary().name();
            }
        });

        MAPPING.set(SEAS_I_EVOLUTIVE, ec.tstoolkit.information.StatisticalTest.class, source -> {
            CombinedSeasonalityTest itest = source.ciTest(false);
            if (itest == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(itest.getEvolutiveSeasonality());
            }
        });

        MAPPING.set(SEAS_I_STABLE, ec.tstoolkit.information.StatisticalTest.class, source -> {
            CombinedSeasonalityTest itest = source.ciTest(false);
            if (itest == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(itest.getStableSeasonality());
            }
        });

        /////////////////////// SA
        MAPPING.set(SEAS_SA_F, ec.tstoolkit.information.StatisticalTest.class, source -> {
            FTest ftest = new FTest();
            if (ftest.test(source.sa)) {
                return ec.tstoolkit.information.StatisticalTest.of(ftest.getFTest());
            } else {
                return null;
            }
        });

        MAPPING.set(SEAS_SA_QS, ec.tstoolkit.information.StatisticalTest.class, source -> {
            SeasonalityTests satests = source.saTests();
            if (satests == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(satests.getQs());
            }
        });

        MAPPING.set(SEAS_SA_KW, ec.tstoolkit.information.StatisticalTest.class, source -> {
            TsData sa = source.sa;
            sa = DifferencingResults.create(source.mul ? sa.log() : sa, 1, true).getDifferenced();
            KruskalWallisTest kw = new KruskalWallisTest(sa);
            return ec.tstoolkit.information.StatisticalTest.of(kw);
        });

        MAPPING.set(SEAS_SA_FRIEDMAN, ec.tstoolkit.information.StatisticalTest.class, source -> {
            SeasonalityTests satests = source.saTests();
            if (satests == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(satests.getNonParametricTest());
            }
        });

        MAPPING.set(SEAS_SA_PERIODOGRAM, ec.tstoolkit.information.StatisticalTest.class, source -> {
            SeasonalityTests satests = source.saTests();
            if (satests == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(satests.getPeriodogramTest());
            }
        });

        MAPPING.set(SEAS_SA_SP, String.class, source -> {
            SeasonalityTests satests = source.saTests();
            if (satests == null) {
                return null;
            } else {
                return SpectralPeaks.format(satests.getSpectralPeaks());
            }
        });

        MAPPING.set(SEAS_SA_COMBINED, String.class, source -> {
            CombinedSeasonalityTest satest = source.csaTest(false);
            if (satest == null) {
                return null;
            } else {
                return satest.getSummary().name();
            }
        });

        MAPPING.set(SEAS_SA_COMBINED3, String.class, source -> {
            CombinedSeasonalityTest satest = source.csaTest(true);
            if (satest == null) {
                return null;
            } else {
                return satest.getSummary().name();
            }
        });

        MAPPING.set(SEAS_SA_EVOLUTIVE, ec.tstoolkit.information.StatisticalTest.class, source -> {
            CombinedSeasonalityTest satest = source.csaTest(false);
            if (satest == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(satest.getEvolutiveSeasonality());
            }
        });

        MAPPING.set(SEAS_SA_STABLE, ec.tstoolkit.information.StatisticalTest.class, source -> {
            CombinedSeasonalityTest satest = source.csaTest(false);
            if (satest == null) {
                return null;
            } else {
                return ec.tstoolkit.information.StatisticalTest.of(satest.getStableSeasonality());
            }
        });

        MAPPING.set(SEAS_SA_AC1, Double.class, source -> {
            TsData dsa = source.sa.delta(1);
            AutoCorrelations ac = new AutoCorrelations(dsa);
            return ac.autoCorrelation(1);
        });

        MAPPING.set(TD_SA_ALL, ec.tstoolkit.information.StatisticalTest.class, source -> {
            TsData s = source.sa;
            if (source.mul) {
                s = s.log();
            }
            ec.tstoolkit.stats.StatisticalTest test = tdAr(s);
            return ec.tstoolkit.information.StatisticalTest.of(test);
        });

        MAPPING.set(TD_SA_LAST, ec.tstoolkit.information.StatisticalTest.class, source -> {
            TsData s = source.sa;
            if (source.mul) {
                s = s.log();
            }
            int ifreq = s.getFrequency().intValue();
            s = s.drop(Math.max(0, s.getLength() - ifreq * 8 - 1), 0);
            ec.tstoolkit.stats.StatisticalTest test = tdAr(s);
            return ec.tstoolkit.information.StatisticalTest.of(test);
        });

        MAPPING.set(TD_I_ALL, ec.tstoolkit.information.StatisticalTest.class, source -> {
            TsData s = source.irr;
            if (source.mul) {
                s = s.log();
            }
            ec.tstoolkit.stats.StatisticalTest test = tdAr(s);
            return ec.tstoolkit.information.StatisticalTest.of(test);
        });

        MAPPING.set(TD_I_LAST, ec.tstoolkit.information.StatisticalTest.class, source -> {
            TsData s = source.irr;
            if (source.mul) {
                s = s.log();
            }
            int ifreq = s.getFrequency().intValue();
            s = s.drop(Math.max(0, s.getLength() - ifreq * 8 - 1), 0);
            ec.tstoolkit.stats.StatisticalTest test = tdAr(s);
            return ec.tstoolkit.information.StatisticalTest.of(test);
        });

        MAPPING.set(TD_RES_ALL, ec.tstoolkit.information.StatisticalTest.class, source -> {
            TsData s = source.res;
            if (s == null) {
                return null;
            }
            ec.tstoolkit.stats.StatisticalTest test = td(s);
            return ec.tstoolkit.information.StatisticalTest.of(test);
        });

        MAPPING.set(TD_RES_LAST, ec.tstoolkit.information.StatisticalTest.class, source -> {
            TsData s = source.res;
            if (s == null) {
                return null;
            }
            int ifreq = s.getFrequency().intValue();
            s = s.drop(Math.max(0, s.getLength() - ifreq * 8 - 1), 0);
            ec.tstoolkit.stats.StatisticalTest test = td(s);
            return ec.tstoolkit.information.StatisticalTest.of(test);
        });

        MAPPING.set(IC_RATIO_HENDERSON, Double.class, source -> {
            TsData sa = source.sa;
            int freq = sa.getFrequency().intValue();
            int filterLength = freq + 1;
            SymmetricFilter trendFilter = TrendCycleFilterFactory.makeHendersonFilter(filterLength);// .defaultHendersonFilterForFrequency(freq);
            IFiltering strategy = new DefaultTrendFilteringStrategy(trendFilter,
                    null, filterLength + " terms Henderson moving average");
            TsData sc = strategy.process(sa, sa.getDomain());
            TsData si = source.mul ? TsData.divide(sa, sc) : TsData.subtract(sa, sc);
            double gc = SeriesEvolution.calcAbsMeanVariations(sc, null, 1, source.mul, null);
            double gi = SeriesEvolution.calcAbsMeanVariations(si, null, 1, source.mul, null);
            return (12 / freq) * gi / gc;
        });

        MAPPING.set(IC_RATIO, Double.class, source -> {
            TsData sa = source.sa;
            int freq = sa.getFrequency().intValue();
            TsData sc = source.t;
            TsData si = source.mul ? TsData.divide(sa, sc) : TsData.subtract(sa, sc);
            double gc = SeriesEvolution.calcAbsMeanVariations(sc, null, 1, source.mul, null);
            double gi = SeriesEvolution.calcAbsMeanVariations(si, null, 1, source.mul, null);
            return (12 / freq) * gi / gc;
        });

        MAPPING.set(MSR_GLOBAL, Double.class, source -> {
            MsrTable msr = source.msr();
            if (msr == null) {
                return null;
            }
            return msr.getGlobalMsr();
        });

        MAPPING.setArray(MSR, 1, 12, Double.class, (source, i) -> {
            MsrTable msr = source.msr();
            if (msr == null) {
                return null;
            }
            return i <= 0 || i > msr.getCount() ? null : msr.getRMS(i - 1);
        });
    }
}
