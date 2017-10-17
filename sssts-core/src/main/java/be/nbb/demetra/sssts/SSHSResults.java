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
package be.nbb.demetra.sssts;

import be.nbb.demetra.sssts.ssf.SsfofSSHS;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.implementations.CompositeMeasurement;
import ec.demetra.ssf.implementations.NoisyMeasurement;
import ec.demetra.ssf.univariate.DefaultSmoothingResults;
import ec.demetra.ssf.univariate.ExtendedSsfData;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.ISaResults;
import ec.satoolkit.ISeriesDecomposition;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.information.InformationMapping;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
public class SSHSResults implements ISaResults {

    SSHSResults(TsData y, SSHSMonitor monitor, boolean noise, boolean b) {
        this.y = y;
        mul = b;
        this.monitor = monitor;
        SSHSMonitor.MixedEstimation rslt = monitor.getBestModel();
        if (rslt != null) {
            computeDecomposition(rslt.model, noise);
        }
    }
    public static final String NOISE = "noisecomponent", NOISE_DATA = "noise", IRREGULAR = "irregular", NOISE_LBOUND = "lbound", NOISE_UBOUND = "ubound";
    private final SSHSMonitor monitor;
    private final boolean mul;
    private final TsData y;
    private DefaultSmoothingResults srslts;
    private TsData t, sa, s, irr, level, slope, cycle, fy, ft, fsa, fs;

    private void computeDecomposition(SSHSModel model, boolean noise) {
        int nf = y.getFrequency().intValue();
        ISsfData data = new ExtendedSsfData(y, nf);
        int start = y.getStart().getPosition();
        Matrix cmps;
        if (noise) {
            ISsf ssf = SsfofSSHS.ofNoise2(model, start);
            srslts = DkToolkit.sqrtSmooth(ssf, data, true);
            NoisyMeasurement m = (NoisyMeasurement) ssf.getMeasurement();
            CompositeMeasurement cm = (CompositeMeasurement) m.getMeasurement();
            cmps = new Matrix(data.getLength(), cm.getComponentsCount());
            for (int i = 0; i < cmps.getRowsCount(); ++i) {
                cm.ZX(i, srslts.a(i), cmps.row(i));
            }

        } else {
            ISsf ssf = SsfofSSHS.ofSeasonal(model, start);
            srslts = DkToolkit.sqrtSmooth(ssf, data, true);
            CompositeMeasurement cm = (CompositeMeasurement) ssf.getMeasurement();
            cmps = new Matrix(data.getLength(), cm.getComponentsCount());
            for (int i = 0; i < cmps.getRowsCount(); ++i) {
                cm.ZX(i, srslts.a(i), cmps.row(i));
            }
        }
        t = new TsData(y.getStart(), cmps.column(0).drop(0, nf));
        ft = new TsData(y.getEnd(), cmps.column(0).drop(y.getLength(), 0));
        s = new TsData(y.getStart(), cmps.column(1).drop(0, nf));
        fs = new TsData(y.getEnd(), cmps.column(1).drop(y.getLength(), 0));
        irr = TsData.subtract(y, TsData.add(t, s));
        sa = TsData.subtract(y, s);
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
        return MAPPING.getData(this, id, tclass);
    }

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public ISeriesDecomposition getSeriesDecomposition() {
        DefaultSeriesDecomposition decomposition
                = new DefaultSeriesDecomposition(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive);
        if (mul) {
            decomposition.add(y.exp(), ComponentType.Series);
            decomposition.add(sa.exp(), ComponentType.SeasonallyAdjusted);
            decomposition.add(t.exp(), ComponentType.Trend);
            decomposition.add(s.exp(), ComponentType.Seasonal);
            decomposition.add(irr.exp(), ComponentType.Irregular);
        } else {
            decomposition.add(y, ComponentType.Series);
            decomposition.add(sa, ComponentType.SeasonallyAdjusted);
            decomposition.add(t, ComponentType.Trend);
            decomposition.add(s, ComponentType.Seasonal);
            decomposition.add(irr, ComponentType.Irregular);
        }
        return decomposition;
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    public SSHSMonitor.MixedEstimation getBestModel() {
        return monitor.getBestModel();
    }

    public int getBestModelPosition() {
        return monitor.getBestModelPosition();
    }

    public List<SSHSMonitor.MixedEstimation> getAllModels() {
        return monitor.getAllResults();
    }

    public TsData getResiduals() {
        TsDomain domain = y.getDomain();
        IReadDataBlock res = monitor.getBestModel().ll.getResiduals();
        return new TsData(domain.getStart().plus(domain.getLength() - res.getLength()), res);

    }

    public TsData getNoise() {
        return irr;
    }

    // MAPPING
    public static InformationMapping<SSHSResults> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<SSHSResults, T> extractor) {
        MAPPING.set(name, tclass, extractor);
    }

    public static <T> void setTsData(String name, Function<SSHSResults, TsData> extractor) {
        MAPPING.set(name, extractor);
    }

    private static final InformationMapping<SSHSResults> MAPPING = new InformationMapping<>(SSHSResults.class);

    static {
        MAPPING.set(ModellingDictionary.Y_CMP, source -> source.mul ? source.y.exp() : source.y);
        MAPPING.set(ModellingDictionary.T_CMP, source -> source.mul ? source.t.exp() : source.t);
        MAPPING.set(ModellingDictionary.SA_CMP, source -> source.mul ? source.sa.exp() : source.sa);
        MAPPING.set(ModellingDictionary.S_CMP, source -> source.mul ? source.s.exp() : source.s);
        MAPPING.set(ModellingDictionary.I_CMP, source -> source.mul ? source.irr.exp() : source.irr);
        MAPPING.set(ModellingDictionary.Y_LIN, source -> source.y);
        MAPPING.set(ModellingDictionary.T_LIN, source -> source.t);
        MAPPING.set(ModellingDictionary.SA_LIN, source -> source.sa);
        MAPPING.set(ModellingDictionary.S_LIN, source -> source.s);
        MAPPING.set(ModellingDictionary.I_LIN, source -> source.irr);
        MAPPING.set(ModellingDictionary.SI_CMP, source -> {
            TsData si = TsData.add(source.s, source.irr);
            if (si == null) {
                return null;
            }
            return source.mul ? si.exp() : si;
        });
        MAPPING.set("residuals", source -> source.getResiduals());
    }

    @Override
    public InformationSet getInformation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
