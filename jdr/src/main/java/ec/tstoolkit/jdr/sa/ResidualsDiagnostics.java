/*
* Copyright 2013 National Bank of Belgium
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
package ec.tstoolkit.jdr.sa;

import jd2.algorithm.IProcResults;
import jd2.information.InformationMapping;
import ec.satoolkit.GenericSaResults;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcQuality;
import ec.tstoolkit.data.Periodogram;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.stats.DoornikHansenTest;
import ec.tstoolkit.stats.LjungBoxTest;
import ec.tstoolkit.stats.NiidTests;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Kristof Bayens
 */
public class ResidualsDiagnostics implements IProcResults {

    private static final double N0 = 0.1, N1 = .01;
    private static final double TD0 = 0.1, TD1 = 0.01, TD2 = 0.001;
    private static final double S0 = 0.1, S1 = 0.01, S2 = 0.001;

    public static final String NORMALITY = "normality", INDEPENDENCE = "independence",
            TD_PEAK = "tdpeaks", S_PEAK = "seaspeaks";
    public static final String NAME = "Regarima residuals";
    public static List<String> ALL = Collections.unmodifiableList(Arrays.asList(NORMALITY, INDEPENDENCE, TD_PEAK, S_PEAK));

    private final List<String> warnings_ = new ArrayList<>();

    private NiidTests stats_;
    private Periodogram periodogram_;
    private int freq_;

    static ResidualsDiagnostics of(CompositeResults crslts) {
        try {
            PreprocessingModel regarima = GenericSaResults.getPreprocessingModel(crslts);
            if (regarima instanceof PreprocessingModel) {
                return new ResidualsDiagnostics(regarima);
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public ResidualsDiagnostics(PreprocessingModel rslts) {
        testRegarima(rslts);
    }

    private boolean testRegarima(PreprocessingModel regarima) {
        try {
            TsData res = regarima.getFullResiduals();
            freq_ = res.getFrequency().intValue();
            stats_ = new NiidTests(res, freq_, regarima.description.getArimaComponent().getFreeParametersCount(), true);
            periodogram_ = new Periodogram(res);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getName() {
        return NAME;
    }

    public List<String> getTests() {
        return ALL;
    }

    public ProcQuality getDiagnostic(String test) {
        double pval = 0;
        switch (test) {
            case NORMALITY:
                if (stats_ == null) {
                    return ProcQuality.Undefined;
                }
                DoornikHansenTest dht = stats_.getNormalityTest();
                if (dht == null) {
                    return ProcQuality.Undefined;
                }
                pval = dht.getPValue();
                if (pval > N0) {
                    return ProcQuality.Good;
                } else if (pval < N1) {
                    return ProcQuality.Bad;
                } else {
                    return ProcQuality.Uncertain;
                }
            case INDEPENDENCE:
                if (stats_ == null) {
                    return ProcQuality.Undefined;
                }
                LjungBoxTest lbt = stats_.getLjungBox();
                if (lbt == null) {
                    return ProcQuality.Undefined;
                }
                pval = lbt.getPValue();
                if (pval > N0) {
                    return ProcQuality.Good;
                } else if (pval < N1) {
                    return ProcQuality.Bad;
                } else {
                    return ProcQuality.Uncertain;
                }
            case TD_PEAK: {
                if (periodogram_ == null) {
                    return ProcQuality.Undefined;
                }
                double[] tdfreqs = Periodogram.getTradingDaysFrequencies(freq_);
                double[] p = periodogram_.getS();
                double xmax = 0;
                double dstep = periodogram_.getIntervalInRadians();
                for (int i = 0; i < tdfreqs.length; ++i) {
                    int i0 = (int) (tdfreqs[i] / dstep);
                    double xcur = p[i0];
                    if (xcur > xmax) {
                        xmax = xcur;
                    }
                    xcur = p[i0 + 1];
                    if (xcur > xmax) {
                        xmax = xcur;
                    }
                }
                pval = 1 - Math.pow(1 - Math.exp(-xmax * .5), tdfreqs.length);
                if (pval < TD2) {
                    return ProcQuality.Severe;
                }
                if (pval < TD1) {
                    return ProcQuality.Bad;
                } else if (pval > TD0) {
                    return ProcQuality.Good;
                } else {
                    return ProcQuality.Uncertain;
                }
            }
            case S_PEAK: {
                if (periodogram_ == null) {
                    return ProcQuality.Undefined;
                }
                double[] seasfreqs = new double[(freq_ - 1) / 2];
                // seas freq in radians...
                for (int i = 0; i < seasfreqs.length; ++i) {
                    seasfreqs[i] = (i + 1) * 2 * Math.PI / freq_;
                }

                double[] p = periodogram_.getS();
                double xmax = 0;
                double dstep = periodogram_.getIntervalInRadians();
                for (int i = 0; i < seasfreqs.length; ++i) {
                    int i0 = (int) (seasfreqs[i] / dstep);
                    double xcur = p[i0];
                    if (xcur > xmax) {
                        xmax = xcur;
                    }
                    xcur = p[i0 + 1];
                    if (xcur > xmax) {
                        xmax = xcur;
                    }
                }
                pval = 1 - Math.pow(1 - Math.exp(-xmax * .5), seasfreqs.length);
                if (pval < S2) {
                    return ProcQuality.Severe;
                }
                if (pval < S1) {
                    return ProcQuality.Bad;
                } else if (pval > S0) {
                    return ProcQuality.Good;
                } else {
                    return ProcQuality.Uncertain;
                }
            }
            default:
                break;
        }
        return ProcQuality.Undefined;
    }

    public double getValue(String test) {
        try {
            double pval = 0;
            switch (test) {
                case NORMALITY:
                    if (stats_ != null) {
                        DoornikHansenTest dht = stats_.getNormalityTest();
                        pval = dht.getPValue();
                    }
                    break;
                case INDEPENDENCE:
                    if (stats_ != null) {
                        LjungBoxTest lbt = stats_.getLjungBox();
                        pval = lbt.getPValue();
                    }
                    break;
                case TD_PEAK:
                    if (periodogram_ != null) {
                        double[] tdfreqs = Periodogram.getTradingDaysFrequencies(freq_);
                        double[] p = periodogram_.getS();
                        double xmax = 0;
                        double dstep = periodogram_.getIntervalInRadians();
                        for (int i = 0; i < tdfreqs.length; ++i) {
                            int i0 = (int) (tdfreqs[i] / dstep);
                            double xcur = p[i0];
                            if (xcur > xmax) {
                                xmax = xcur;
                            }
                            xcur = p[i0 + 1];
                            if (xcur > xmax) {
                                xmax = xcur;
                            }
                        }
                        pval = 1 - Math.pow(1 - Math.exp(-xmax * .5), tdfreqs.length);
                    }
                    break;
                case S_PEAK:
                    if (periodogram_ != null) {
                        double[] seasfreqs = new double[(freq_ - 1) / 2];
                        // seas freq in radians...
                        for (int i = 0; i < seasfreqs.length; ++i) {
                            seasfreqs[i] = (i + 1) * 2 * Math.PI / freq_;
                        }

                        double[] p = periodogram_.getS();
                        double xmax = 0;
                        double dstep = periodogram_.getIntervalInRadians();
                        for (int i = 0; i < seasfreqs.length; ++i) {
                            int i0 = (int) (seasfreqs[i] / dstep);
                            double xcur = p[i0];
                            if (xcur > xmax) {
                                xmax = xcur;
                            }
                            xcur = p[i0 + 1];
                            if (xcur > xmax) {
                                xmax = xcur;
                            }
                        }
                        pval = 1 - Math.pow(1 - Math.exp(-xmax * .5), seasfreqs.length);
                    }
                    break;
                default:
                    break;
            }
            return pval;
        } catch (Exception err) {
            return Double.NaN;
        }
    }

    public List<String> getWarnings() {
        return warnings_;
    }

    public double getNIIDBound(ProcQuality quality) {
        switch (quality) {
            case Bad:
                return N1;
            case Uncertain:
                return N0;
            default:
                return Double.NaN;
        }
    }

    public double getTDPeriodogram(ProcQuality quality) {
        switch (quality) {
            case Severe:
                return TD2;
            case Bad:
                return TD1;
            case Uncertain:
                return TD0;
            default:
                return Double.NaN;
        }
    }

    public double getSPeriodogram(ProcQuality quality) {
        switch (quality) {
            case Severe:
                return S2;
            case Bad:
                return S1;
            case Uncertain:
                return S0;
            default:
                return Double.NaN;
        }
    }

    static final InformationMapping<ResidualsDiagnostics> MAPPING = new InformationMapping<>(ResidualsDiagnostics.class);

    public static InformationMapping<ResidualsDiagnostics> getMapping() {
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
    
    static{
        MAPPING.set("normality.value", Double.class, source->source.getValue(NORMALITY));
        MAPPING.set("normality", String.class, source->source.getDiagnostic(NORMALITY).name());
        MAPPING.set("independence.value", Double.class, source->source.getValue(INDEPENDENCE));
        MAPPING.set("independence", String.class, source->source.getDiagnostic(INDEPENDENCE).name());
        MAPPING.set("tdpeaks.value", Double.class, source->source.getValue(TD_PEAK));
        MAPPING.set("tdpeaks", String.class, source->source.getDiagnostic(TD_PEAK).name());
        MAPPING.set("seaspeaks.value", Double.class, source->source.getValue(S_PEAK));
        MAPPING.set("seaspeaks", String.class, source->source.getDiagnostic(S_PEAK).name());
    }
    
}
