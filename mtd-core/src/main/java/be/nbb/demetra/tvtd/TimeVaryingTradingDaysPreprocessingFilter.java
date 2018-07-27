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
package be.nbb.demetra.tvtd;

import ec.satoolkit.IPreprocessingFilter;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.regression.ILengthOfPeriodVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Jean Palate
 */
public class TimeVaryingTradingDaysPreprocessingFilter implements IPreprocessingFilter {

    private final TimeVaryingTradingDaysEstimator tvtde;
    private int nf = -2, nb = -2;

    public TimeVaryingTradingDaysPreprocessingFilter(TimeVaryingTradingDaysEstimator tvtde) {
        this.tvtde = tvtde;
    }

    public PreprocessingModel getModel() {
        return tvtde.getModel();
    }

    public TimeVaryingTradingDaysEstimator getTimeVaryingTradingDaysEstimator() {
        return tvtde;
    }

    public int getForecastHorizon() {
        return nf;
    }

    public void setForecastHorizon(int nf) {
        this.nf = nf;
    }

    public int getBackcastHorizon() {
        return nb;
    }

    public void setBackcastHorizon(int nb) {
        this.nb = nb;
    }

    @Deprecated
    public boolean isSeasonalMeanCorrection() {
        return false;
    }

    @Override
    public boolean process(PreprocessingModel model) {
        return tvtde.process(model);
    }

    @Deprecated
    private double seasMeanCorrection(PreprocessingModel model) {
        return 0;
    }

    @Override
    public TsData getCorrectedSeries(boolean transformed) {
        PreprocessingModel model = tvtde.getModel();
        boolean mul = (!transformed) && model.isMultiplicative();
        TsData lin = model.linearizedSeries(true);
        TsDomain domain=model.description.getSeriesDomain();
        TsData mtd=tvtde.tdEffect(domain);
        TsData td=model.deterministicEffect(domain, v->v instanceof ITradingDaysVariable);
        lin=TsData.add(lin, TsData.subtract(td, mtd));
        if (mul) {
            lin = lin.exp();
        }
        return lin;
    }

    private int forecastLength(int freq) {
        if (nf == 0 || freq == 0) {
            return 0;
        } else if (nf < 0) {
            return -freq * nf;
        } else {
            return nf;
        }
    }

    private int backcastLength(int freq) {
        if (nb == 0 || freq == 0) {
            return 0;
        } else if (nb < 0) {
            return -freq * nb;
        } else {
            return nb;
        }
    }

    @Override
    public TsData getCorrectedForecasts(boolean transformed) {
        if (nf == 0) {
            return null;
        }
        PreprocessingModel model = tvtde.getModel();
        int nf = forecastLength(model.description.getFrequency());
        TsData f = model.linearizedForecast(nf, true);
        TsData mtd=tvtde.tdEffect(f.getDomain());
        TsData td=model.deterministicEffect(f.getDomain(), v->v instanceof ITradingDaysVariable);
        f=TsData.add(f, TsData.subtract(td, mtd));
        if ((!transformed) && model.isMultiplicative()) {
            f.apply(x -> Math.exp(x));
        }
        return f;
    }

    @Override
    public TsData getCorrectedBackcasts(boolean transformed) {
        if (nb == 0) {
            return null;
        }
        PreprocessingModel model = tvtde.getModel();
        int nb = backcastLength(model.description.getFrequency());
        TsData b = model.linearizedBackcast(nb, true);
        TsData mtd=tvtde.tdEffect(b.getDomain());
        TsData td=model.deterministicEffect(b.getDomain(), v->v instanceof ITradingDaysVariable);
        b=TsData.add(b, TsData.subtract(td, mtd));
        if ((!transformed) && model.isMultiplicative()) {
            b.apply(x -> Math.exp(x));
        }
        return b;
    }

    @Override
    public TsData getCorrection(TsDomain domain, ComponentType type, boolean transformed) {
        PreprocessingModel model = tvtde.getModel();
        switch (type) {
            case Series:
                TsData x = model.deterministicEffect(domain, type);
                if (!transformed) {
                    model.backTransform(x, false, false);
                }
                return x;
            case Trend:
                TsData t = model.deterministicEffect(domain, type);
                if (!transformed) {
                    model.backTransform(t, true, false);
                }
                return t;
            case Seasonal:
                TsData s = model.deterministicEffect(domain, type);
                TsData mh = model.movingHolidaysEffect(domain);
                TsData lp = model.deterministicEffect(domain, v -> v instanceof ILengthOfPeriodVariable);
                TsData td = tvtde.tdEffect(domain);
                TsData c = TsData.add(mh, TsData.add(lp, td));
                TsData sc = TsData.add(s, c);
                if (!transformed) {
                    model.backTransform(sc, false, true);
                }
                return sc;
            case SeasonallyAdjusted:
                TsData sa = model.deterministicEffect(domain, type);
                if (!transformed) {
                    model.backTransform(sa, false, false);
                }
                return sa;
            case Undefined:
                TsData undef = model.deterministicEffect(domain, type);
                if (!transformed) {
                    model.backTransform(undef, false, false);
                }
                return undef;
            case Irregular:
                TsData i = model.deterministicEffect(domain, type);
                if (!transformed) {
                    model.backTransform(i, false, false);
                }
                return i;

            default:
                return null;
        }

    }

    @Override
    @Deprecated
    public double getBiasCorrection(ComponentType type) {
        return 0;
    }

//    @Override
//    public TsData filter(String id, TsData data) {
//        return data;
//    }
    @Override
    public boolean isInitialized() {
        return tvtde != null;
    }
}
