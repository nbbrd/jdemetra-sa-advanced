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
import ec.satoolkit.x11.IX11Preprocessor;
import ec.satoolkit.x11.X11Context;
import ec.satoolkit.x11.X11Kernel;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.regression.AdditiveOutlier;
import ec.tstoolkit.timeseries.regression.ILengthOfPeriodVariable;
import ec.tstoolkit.timeseries.regression.LevelShift;
import ec.tstoolkit.timeseries.regression.SeasonalOutlier;
import ec.tstoolkit.timeseries.regression.TransitoryChange;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 * Extension of the series using information provided by a PreprocessingModel.
 * The actual computation is provided by the PreprocessingModel itself
 *
 * @author Jean Palate
 */
public class TimeVaryingTradingDaysPreprocessor implements
        IX11Preprocessor {

    private X11Context context;
    private final TimeVaryingTradingDaysPreprocessingFilter filter;

    /**
     * Creates a new default pre-processor
     *
     * @param filter
     */
    public TimeVaryingTradingDaysPreprocessor(TimeVaryingTradingDaysPreprocessingFilter filter) {
        this.filter=filter;
    }

    /**
     *
     * @param s
     * @param info
     */
    @Override
    public void preprocess(InformationSet info) {
        
        TsData s = filter.getCorrectedSeries(false);
        TsData fs = filter.getCorrectedForecasts(false);
        TsData bs = filter.getCorrectedBackcasts(false);
        TsData sall = s.update(fs);
        if (bs != null) {
            sall = bs.update(sall);
        }
        InformationSet atables = info.subSet(X11Kernel.A);
        InformationSet btables = info.subSet(X11Kernel.B);
        btables.set(X11Kernel.B1, sall);
        if (fs != null) {
            atables.set(X11Kernel.A1a, fs);
        }
        if (bs != null) {
            atables.set(X11Kernel.A1b, bs);
        }
        // complete the information sets using the pre-processing model
        PreprocessingModel model=filter.getModel();
        TsDomain domain = model.description.getSeriesDomain();
        // extend the domain for forecasts
        int nf = context.getForecastHorizon(), nb = context.getBackcastHorizon(), ny = context.getFrequency();
        domain = domain.extend(nb, nf == 0 ? ny : nf);
        

        TsData mh = model.movingHolidaysEffect(domain);
        TsData lp = model.deterministicEffect(domain, v->v instanceof ILengthOfPeriodVariable);
        TsData td=filter.getTimeVaryingTradingDaysEstimator().getTdEffect();
        td=TsData.add(td, lp);
        model.backTransform(td, false, true);
        model.backTransform(mh, false, false);
        atables.add(X11Kernel.A6, td);
        atables.add(X11Kernel.A7, mh);
        //d.add(X11Kernel.D18, cal);
        TsData p = model.outliersEffect(domain);
        TsData pt = model.deterministicEffect(domain, LevelShift.class);
        TsData ps = model.deterministicEffect(domain, SeasonalOutlier.class);
        TsData pa = model.deterministicEffect(domain, AdditiveOutlier.class);
        TsData pc = model.deterministicEffect(domain, TransitoryChange.class);
        TsData ut = model.userEffect(domain, ComponentType.Trend);
        TsData ua = model.userEffect(domain, ComponentType.Irregular);
        TsData us = model.userEffect(domain, ComponentType.Seasonal);
        TsData usa = model.userEffect(domain, ComponentType.SeasonallyAdjusted);
        TsData uu = model.userEffect(domain, ComponentType.Undefined);
        TsData user = model.userEffect(domain, ComponentType.Series);

        pt = TsData.add(pt, ut);
        ps = TsData.add(ps, us);
        pa = TsData.add(pa, ua);
        TsData pi = TsData.add(pa, pc);
        TsData pall = TsData.add(pt, TsData.add(ps, pi));
        TsData u = TsData.add(usa, user);
        model.backTransform(p, false, false);
        model.backTransform(pt, false, false);
        model.backTransform(ps, false, false);
        model.backTransform(pa, false, false);
        model.backTransform(pc, false, false);
        model.backTransform(pi, false, false);
        model.backTransform(pall, false, false);
        model.backTransform(usa, false, false);
        model.backTransform(uu, false, false);
        model.backTransform(user, false, false);
        model.backTransform(u, false, false);

        atables.add(X11Kernel.A8t, pt);
        atables.add(X11Kernel.A8s, ps);
        atables.add(X11Kernel.A8i, pi);
        atables.add(X11Kernel.A8, pall);
        atables.add(X11Kernel.A9, u);
        atables.add(X11Kernel.A9sa, usa);
        atables.add(X11Kernel.A9u, uu);
        atables.add(X11Kernel.A9ser, user);
    }

    @Override
    public void setContext(X11Context context) {
        this.context=context;
    }

 }
