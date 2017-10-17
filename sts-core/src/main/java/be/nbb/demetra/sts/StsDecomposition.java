/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.sts;

import ec.demetra.ssf.implementations.structural.SsfBsm;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.implementations.structural.BasicStructuralModel;
import ec.demetra.ssf.implementations.structural.Component;
import ec.demetra.ssf.univariate.DefaultSmoothingResults;
import ec.demetra.ssf.univariate.ExtendedSsfData;
import ec.demetra.ssf.univariate.SsfData;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.ISaResults;
import ec.satoolkit.ISeriesDecomposition;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.information.InformationMapping;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.tstoolkit.ucarima.WienerKolmogorovEstimators;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
public class StsDecomposition implements ISaResults {

    public static final String MODEL = "model";
    public static final String SERIES = "series", LEVEL = "level", CYCLE = "cycle", SLOPE = "slope", NOISE = "noise", SEASONAL = "seasonal";
    private final DefaultSmoothingResults srslts_;
    private final TsData y_, yf_, t_, sa_, s_, i_, c_;
    private final boolean mul_;
    private final InformationSet info_ = new InformationSet();
    private UcarimaModel reduced_;
    private double errFactor_;
    private WienerKolmogorovEstimators wk_;
    private final BasicStructuralModel model;

    public StsDecomposition(TsData ylin, BasicStructuralModel model, boolean mul) {
        y_ = ylin;
        mul_ = mul;
        this.model = model;
        SsfBsm ssf = SsfBsm.create(model);
        ExtendedSsfData data = new ExtendedSsfData(new SsfData(ylin));
        data.setForecastsCount(ylin.getFrequency().intValue());
        TsDomain full = ylin.getDomain().extend(0, data.getForecastsCount());
        srslts_ = DkToolkit.sqrtSmooth(ssf, data, true);
//        DisturbanceSmoother smoother = new DisturbanceSmoother();
//        smoother.setSsf(model);
//        SsfData data = new SsfData(y.getValues().internalStorage(), null);
//        smoother.process(data);
//        srslts_ = smoother.calcSmoothedStates();
        InformationSet minfo = info_.subSet(MODEL);
        TsData noise, level, slope, seasonal = null, cycle = null;
        int pos = SsfBsm.searchPosition(model, Component.Noise);
        if (pos >= 0) {
            noise = new TsData(ylin.getStart(), srslts_.getComponent(pos));
            minfo.add(NOISE, noise);
            i_ = noise;
        } else {
            i_ = new TsData(full, 0);
        }
        pos = SsfBsm.searchPosition(model, Component.Cycle);
        if (pos >= 0) {
            cycle = new TsData(ylin.getStart(), srslts_.getComponent(pos));
            minfo.add(CYCLE, cycle);
            c_ = cycle;
        } else {
            c_ = null;
        }
        pos = SsfBsm.searchPosition(model, Component.Level);
        if (pos >= 0) {
            level = new TsData(ylin.getStart(), srslts_.getComponent(pos));
            minfo.add(LEVEL, level);
            t_ = TsData.add(level, cycle);
        } else {
            t_ = c_; //new TsData(y.getDomain(), 0);
        }
        pos = SsfBsm.searchPosition(model, Component.Slope);
        if (pos >= 0) {
            slope = new TsData(ylin.getStart(), srslts_.getComponent(pos));
            minfo.add(SLOPE, slope);
        }
        pos = SsfBsm.searchPosition(model, Component.Seasonal);
        if (pos >= 0) {
            seasonal = new TsData(ylin.getStart(), srslts_.getComponent(pos));
            minfo.add(SEASONAL, seasonal);
            s_ = seasonal;
        } else {
            s_ = new TsData(full, 0);
        }
        minfo.add(SERIES, ylin);
        TsData all = TsData.add(t_, s_);
        all = TsData.add(all, i_);
        all = all.update(y_);
        sa_ = TsData.subtract(all, seasonal);
        yf_ = all.drop(y_.getLength(), 0);
    }

    public UcarimaModel getUcarimaModel() {
        if (reduced_ == null) {
            reduced_ = model.computeReducedModel(false);
            errFactor_ = reduced_.normalize();
        }
        return reduced_;
    }

    public double getResidualsScalingFactor() {
        if (reduced_ == null) {
            reduced_ = model.computeReducedModel(false);
            errFactor_ = reduced_.normalize();
        }
        return Math.sqrt(errFactor_);

    }

    public WienerKolmogorovEstimators getWienerKolmogorovEstimators() {
        if (wk_ == null) {
            wk_ = new WienerKolmogorovEstimators(getUcarimaModel());
        }
        return wk_;

    }

    public List<String> getTsDataDictionary() {
        return info_.getDictionary(TsData.class);
    }

    @Override
    public boolean contains(String id) {
        if (MAPPING.contains(id)) {
            return true;
        }
        if (info_ != null) {
            if (!id.contains(InformationSet.STRSEP)) {
                return info_.deepSearch(id, Object.class) != null;
            } else {
                return info_.search(id, Object.class) != null;
            }

        } else {
            return false;
        }
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    public ISeriesDecomposition getComponents() {
        DefaultSeriesDecomposition decomposition
                = new DefaultSeriesDecomposition(DecompositionMode.Additive);
        TsDomain dom = y_.getDomain(), fdom = yf_.getDomain();
        decomposition.add(y_, ComponentType.Series);
        decomposition.add(sa_.fittoDomain(dom), ComponentType.SeasonallyAdjusted);
        decomposition.add(t_.fittoDomain(dom), ComponentType.Trend);
        decomposition.add(s_.fittoDomain(dom), ComponentType.Seasonal);
        decomposition.add(i_.fittoDomain(dom), ComponentType.Irregular);
        decomposition.add(yf_, ComponentType.Series, ComponentInformation.Forecast);
        decomposition.add(sa_.fittoDomain(fdom), ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
        decomposition.add(t_.fittoDomain(fdom), ComponentType.Trend, ComponentInformation.Forecast);
        decomposition.add(s_.fittoDomain(fdom), ComponentType.Seasonal, ComponentInformation.Forecast);
        decomposition.add(i_.fittoDomain(fdom), ComponentType.Irregular, ComponentInformation.Forecast);
        return decomposition;
    }

    @Override
    public ISeriesDecomposition getSeriesDecomposition() {
        if (!mul_) {
            return getComponents();
        }
        DefaultSeriesDecomposition decomposition
                = new DefaultSeriesDecomposition(DecompositionMode.Multiplicative);
        TsDomain dom = y_.getDomain(), fdom = yf_.getDomain();
        decomposition.add(y_.exp(), ComponentType.Series);
        decomposition.add(sa_.fittoDomain(dom).exp(), ComponentType.SeasonallyAdjusted);
        decomposition.add(t_.fittoDomain(dom).exp(), ComponentType.Trend);
        decomposition.add(s_.fittoDomain(dom).exp(), ComponentType.Seasonal);
        decomposition.add(i_.fittoDomain(dom).exp(), ComponentType.Irregular);
        decomposition.add(yf_.exp(), ComponentType.Series, ComponentInformation.Forecast);
        decomposition.add(sa_.fittoDomain(fdom).exp(), ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
        decomposition.add(t_.fittoDomain(fdom).exp(), ComponentType.Trend, ComponentInformation.Forecast);
        decomposition.add(s_.fittoDomain(fdom).exp(), ComponentType.Seasonal, ComponentInformation.Forecast);
        decomposition.add(i_.fittoDomain(fdom).exp(), ComponentType.Irregular, ComponentInformation.Forecast);
        return decomposition;
    }

    @Override
    public Map<String, Class> getDictionary() {
        // TODO
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        MAPPING.fillDictionary(null, map, false);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        if (MAPPING.contains(id)) {
            return MAPPING.getData(this, id, tclass);
        }
        if (!id.contains(InformationSet.STRSEP)) {
            return info_.deepSearch(id, tclass);
        } else {
            return info_.search(id, tclass);
        }
    }

    @Override
    public InformationSet getInformation() {
        return info_;
    }

    // MAPPING
    public static InformationMapping<StsDecomposition> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<StsDecomposition, T> extractor) {
        MAPPING.set(name, tclass, extractor);
    }

    public static <T> void setTsData(String name, Function<StsDecomposition, TsData> extractor) {
        MAPPING.set(name, extractor);
    }

    private static final InformationMapping<StsDecomposition> MAPPING = new InformationMapping<>(StsDecomposition.class);

    static {
        MAPPING.set(ModellingDictionary.Y_CMP, source -> source.mul_ ? source.y_.exp() : source.y_);
        MAPPING.set(ModellingDictionary.Y_CMP + SeriesInfo.F_SUFFIX,
                source -> source.mul_ ? source.yf_.exp() : source.yf_);
        MAPPING.set(ModellingDictionary.T_CMP, source -> {
            TsData x = source.t_.fittoDomain(source.y_.getDomain());
            return source.mul_ ? x.exp() : x;
        });
        MAPPING.set(ModellingDictionary.T_CMP + SeriesInfo.F_SUFFIX, source -> {
            TsData x = source.t_.fittoDomain(source.yf_.getDomain());
            return source.mul_ ? x.exp() : x;
        });
        MAPPING.set(ModellingDictionary.T_CMP + SeriesInfo.F_SUFFIX, source -> {
            TsData x = source.t_.fittoDomain(source.yf_.getDomain());
            return source.mul_ ? x.exp() : x;
        });
        MAPPING.set(ModellingDictionary.SA_CMP, source -> {
            TsData x = source.sa_.fittoDomain(source.y_.getDomain());
            return source.mul_ ? x.exp() : x;
        });
        MAPPING.set(ModellingDictionary.SA_CMP + SeriesInfo.F_SUFFIX, source -> {
            TsData x = source.sa_.fittoDomain(source.yf_.getDomain());
            return source.mul_ ? x.exp() : x;
        });
        MAPPING.set(ModellingDictionary.S_CMP, source -> {
            TsData x = source.s_.fittoDomain(source.y_.getDomain());
            return source.mul_ ? x.exp() : x;
        });
        MAPPING.set(ModellingDictionary.S_CMP + SeriesInfo.F_SUFFIX, source -> {
            TsData x = source.s_.fittoDomain(source.yf_.getDomain());
            return source.mul_ ? x.exp() : x;
        });
        MAPPING.set(ModellingDictionary.I_CMP, source -> {
            TsData x = source.i_.fittoDomain(source.y_.getDomain());
            return source.mul_ ? x.exp() : x;
        });
        MAPPING.set(ModellingDictionary.I_CMP + SeriesInfo.F_SUFFIX, source -> {
            TsData x = source.i_.fittoDomain(source.yf_.getDomain());
            return source.mul_ ? x.exp() : x;
        });
        MAPPING.set(ModellingDictionary.SI_CMP, source -> {
            TsData si = TsData.add(source.s_, source.i_);
            return source.mul_ ? si.exp() : si;
        });
        MAPPING.set(ModellingDictionary.Y_LIN, source -> source.y_);
        MAPPING.set(ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX, source -> source.yf_);
        MAPPING.set(ModellingDictionary.T_LIN, source -> source.t_.fittoDomain(source.y_.getDomain()));
        MAPPING.set(ModellingDictionary.T_LIN + SeriesInfo.F_SUFFIX, source -> source.t_.fittoDomain(source.yf_.getDomain()));
        MAPPING.set(ModellingDictionary.SA_LIN, source -> source.sa_.fittoDomain(source.y_.getDomain()));
        MAPPING.set(ModellingDictionary.SA_LIN + SeriesInfo.F_SUFFIX, source -> source.sa_.fittoDomain(source.yf_.getDomain()));
        MAPPING.set(ModellingDictionary.S_LIN, source -> source.s_.fittoDomain(source.y_.getDomain()));
        MAPPING.set(ModellingDictionary.S_LIN + SeriesInfo.F_SUFFIX, source -> source.s_.fittoDomain(source.yf_.getDomain()));
        MAPPING.set(ModellingDictionary.I_LIN, source -> source.i_.fittoDomain(source.y_.getDomain()));
        MAPPING.set(ModellingDictionary.I_LIN + SeriesInfo.F_SUFFIX, source -> source.i_.fittoDomain(source.yf_.getDomain()));
    }
}
