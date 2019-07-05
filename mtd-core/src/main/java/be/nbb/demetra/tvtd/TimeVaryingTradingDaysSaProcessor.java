/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.tvtd;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultPreprocessingFilter;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.IPreprocessingFilter;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.x11.DefaultPreprocessor;
import ec.satoolkit.x11.Mstatistics;
import ec.satoolkit.x11.X11Kernel;
import ec.satoolkit.x11.X11Results;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x11.X11Toolkit;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TimeVaryingTradingDaysSaProcessor {

    public TimeVaryingTradingDaysSaResults process(TsData s, RegArimaSpecification regSpec,
            TimeVaryingTradingDaysSpecification mtdSpec, X11Specification xspec, ProcessingContext context) {
        return process(regSpec.build(context).process(s, null), mtdSpec, xspec);
    }

    public TimeVaryingTradingDaysSaResults process(TsData s, TramoSpecification regSpec,
            TimeVaryingTradingDaysSpecification mtdSpec, X11Specification xspec, ProcessingContext context) {
        return process(regSpec.build(context).process(s, null), mtdSpec, xspec);
    }

    private TimeVaryingTradingDaysSaResults process(PreprocessingModel model, TimeVaryingTradingDaysSpecification mtdSpec, X11Specification xspec) {
        TimeVaryingTradingDaysEstimator mtde = new TimeVaryingTradingDaysEstimator(mtdSpec.isOnContrast(), mtdSpec.isAirline());
        TimeVaryingTradingDaysPreprocessingFilter filter = new TimeVaryingTradingDaysPreprocessingFilter(mtde);
        filter.setForecastHorizon(xspec.getForecastHorizon());
        filter.setBackcastHorizon(xspec.getBackcastHorizon());
        if (filter.process(model)) {
            TsDomain domain=model.description.getSeriesDomain();
            TimeVaryingTradingDaysCorrection mtdc = TimeVaryingTradingDaysCorrection.of(mtde);
            TimeVaryingTradingDaysPreprocessor mtdpp = new TimeVaryingTradingDaysPreprocessor(filter);

            X11Specification spec = prepareSpec(xspec, model);
            X11Toolkit toolkit = X11Toolkit.create(spec);
            toolkit.setPreprocessor(mtdpp);
            X11Kernel kernel = new X11Kernel();
            kernel.setToolkit(toolkit);
            X11Results rslts = kernel.process(model.interpolatedSeries(false));
            return TimeVaryingTradingDaysSaResults.builder()
                    .preprocessing(model)
                    .timeVaryingTradingDaysCorrection(mtdc)
                    .decomposition(rslts)
                    .finalDecomposition(finalStep(model, filter, rslts))
                    .mstatistics(Mstatistics.computeFromX11(spec.getMode(), rslts.getInformation()))
                    .build();
        } else {
            X11Specification spec = prepareSpec(xspec, model);
            X11Toolkit toolkit = X11Toolkit.create(spec);
            DefaultPreprocessingFilter dfilter = new DefaultPreprocessingFilter();
            dfilter.setForecastHorizon(xspec.getForecastHorizon());
            dfilter.setBackcastHorizon(xspec.getBackcastHorizon());
            toolkit.setPreprocessor(new DefaultPreprocessor(model, dfilter, false));
            X11Kernel kernel = new X11Kernel();
            kernel.setToolkit(toolkit);
            X11Results rslts = kernel.process(model.interpolatedSeries(false));
            return TimeVaryingTradingDaysSaResults.builder()
                    .preprocessing(model)
                    .timeVaryingTradingDaysCorrection(null)
                    .decomposition(rslts)
                    .finalDecomposition(finalStep(model, filter, rslts))
                    .mstatistics(Mstatistics.computeFromX11(spec.getMode(), rslts.getInformation()))
                    .build();
        }
    }

    private X11Specification prepareSpec(X11Specification inputspec, PreprocessingModel model) {
        X11Specification spec = inputspec.clone();
        if (spec.getMode() == DecompositionMode.PseudoAdditive) {
            throw new RuntimeException("Invalid specification: PseudoAdditive decomposition with moving trading days");
        }
        boolean mul = model.isMultiplicative();
        if (mul) {
            if (!spec.getMode().isMultiplicative()) {
                spec.setMode(DecompositionMode.Multiplicative);
            }
        } else {
            spec.setMode(DecompositionMode.Additive);
        }
        return spec;
    }

    private DefaultSeriesDecomposition finalStep(PreprocessingModel pm, IPreprocessingFilter filter, X11Results decomp) {

        TsData orig = pm.description.getOriginal();
        ISeriesDecomposition ldecomp = decomp.getSeriesDecomposition();
        boolean mul = ldecomp.getMode() != DecompositionMode.Additive;
        TsDomain domain = orig.getDomain();
        TsData fdata = ldecomp.getSeries(ComponentType.Series, ComponentInformation.Forecast);
        TsDomain fdomain = fdata == null ? null : fdata.getDomain();
        TsDomain cdomain = fdomain == null ? domain : domain.union(fdomain);
        TsData detT = filter.getCorrection(cdomain, ComponentType.Trend, false);
        TsData detS = filter.getCorrection(cdomain, ComponentType.Seasonal, false);
        TsData detI = filter.getCorrection(cdomain, ComponentType.Irregular, false);
        TsData detY = filter.getCorrection(cdomain, ComponentType.Series, false);
        TsData detSA = filter.getCorrection(cdomain, ComponentType.SeasonallyAdjusted, false);
//                    TsData detU = filter.getCorrection(cdomain, ComponentType.Undefined, false);

        DefaultSeriesDecomposition finals = new DefaultSeriesDecomposition(ldecomp.getMode());
        TsData y = inv_op(mul, orig, detY);
        finals.add(orig, ComponentType.Series);
        TsData t = op(mul, detT, ldecomp.getSeries(ComponentType.Trend,
                ComponentInformation.Value));
        if (t != null && !domain.equals(t.getDomain())) {
            t = t.fittoDomain(domain);
        }
        finals.add(t, ComponentType.Trend);
        TsData s = op(mul, detS, ldecomp.getSeries(ComponentType.Seasonal,
                ComponentInformation.Value));
        if (s != null && !domain.equals(s.getDomain())) {
            s = s.fittoDomain(domain);
        }
        finals.add(s, ComponentType.Seasonal);
        TsData i = op(mul, detI, ldecomp.getSeries(ComponentType.Irregular,
                ComponentInformation.Value));
        if (i != null && !domain.equals(i.getDomain())) {
            i = i.fittoDomain(domain);
        }
        finals.add(i, ComponentType.Irregular);
        finals.add(inv_op(mul, y, s), ComponentType.SeasonallyAdjusted);

        // forecasts...
        if (fdomain != null) {
            TsData ftl = ldecomp.getSeries(ComponentType.Trend,
                    ComponentInformation.Forecast);
            TsData ft = op(mul, detT, ftl);
            if (ft != null) {
                if (!fdomain.equals(ft.getDomain())) {
                    ft = ft.fittoDomain(fdomain);
                }
                finals.add(ft, ComponentType.Trend, ComponentInformation.Forecast);
            }
            TsData fsl = ldecomp.getSeries(ComponentType.Seasonal,
                    ComponentInformation.Forecast);
            TsData fs = op(mul, detS, fsl);
            if (fs != null) {
                if (!fdomain.equals(fs.getDomain())) {
                    fs = fs.fittoDomain(fdomain);
                }
                finals.add(fs, ComponentType.Seasonal, ComponentInformation.Forecast);
            }

            TsData fil = ldecomp.getSeries(ComponentType.Irregular,
                    ComponentInformation.Forecast);
            TsData fi = op(mul, detI, fil);
            if (fi != null) {
                if (!fdomain.equals(fi.getDomain())) {
                    fi = fi.fittoDomain(fdomain);
                }
                finals.add(fi, ComponentType.Irregular, ComponentInformation.Forecast);
            }
            TsData fy = pm.forecast(fdomain.getLength(), false);
            finals.add(fy, ComponentType.Series, ComponentInformation.Forecast);
            TsData fsa = op(mul, ft, fi);
            fsa = op(mul, fsa, detSA);
            if (fsa != null) {
                if (!fdomain.equals(fsa.getDomain())) {
                    fsa = fsa.fittoDomain(fdomain);
                }
                finals.add(fsa, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
            }
        }
        return finals;
    }

    private TsData op(boolean mul, TsData l, TsData r) {
        if (mul) {
            return TsData.multiply(l, r);
        } else {
            return TsData.add(l, r);
        }
    }

    private TsData inv_op(boolean mul, TsData l, TsData r) {
        if (mul) {
            return TsData.divide(l, r);
        } else {
            return TsData.subtract(l, r);
        }
    }
}
