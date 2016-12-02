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

import be.nbb.demetra.sssts.ssf.SsfofSSSTS;
import ec.demetra.ssf.dk.DkToolkit;
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
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class SSSTSResults implements ISaResults {

    SSSTSResults(TsData y, SSSTSMonitor monitor, boolean noise, boolean b) {
        this.y = y;
        mul = b;
        this.monitor = monitor;
        SSSTSMonitor.MixedEstimation rslt = monitor.getBestModel();
        if (rslt != null) {
            computeDecomposition(rslt.model, noise);
        }
    }
    public static final String NOISE = "noisecomponent", NOISE_DATA = "noise", IRREGULAR = "irregular", NOISE_LBOUND = "lbound", NOISE_UBOUND = "ubound";
    private final SSSTSMonitor monitor;
    private final boolean mul;
    private final TsData y;
    private DefaultSmoothingResults srslts;
    private TsData t, sa, s, irr, level, slope, cycle, fy, ft, fsa, fs;

    private void computeDecomposition(SSSTSModel model, boolean noise) {
        int nf = y.getFrequency().intValue();
        ISsfData data = new ExtendedSsfData(y, nf);
        int start = y.getStart().getPosition();
        ISsf of = SsfofSSSTS.of(model, start);
        srslts = DkToolkit.smooth(of, data, false);
        double[] xt = new double[y.getLength()];
        double[] xi = new double[y.getLength()];
        double[] xft = new double[nf];
        int cur = 0, j = start;
        for (; cur < xt.length; ++cur) {
            xt[cur] = srslts.a(cur).get(j);
            if ((++j) % nf == 0) {
                j = 0;
            }
            xi[cur] = y.get(cur) - xt[cur];
        }
        for (int i = 0; i < nf; ++i, ++cur) {
            xft[i] = srslts.a(cur).get(j);
            if ((++j) % nf == 0) {
                j = 0;
            }
        }
        t = new TsData(y.getStart(), xt, false);
        irr = new TsData(y.getStart(), xi, false);
        s=new TsData(y.getStart(), y.getLength());
        s.set(()->0);
        fs=new TsData(y.getEnd(), nf);
        fs.set(()->0);
        ft = new TsData(y.getEnd(), xft, false);
        sa=TsData.subtract(y, s);
//        if (noise) {
//            ISsf ssf = SsfofSSSTS.ofNoise2(model, start);
//            srslts = DkToolkit.sqrtSmooth(ssf, data, true);
//            NoisyMeasurement m = (NoisyMeasurement) ssf.getMeasurement();
//            CompositeMeasurement cm = (CompositeMeasurement) m.getMeasurement();
//            cmps = new Matrix(data.getLength(), cm.getComponentsCount());
//            for (int i = 0; i < cmps.getRowsCount(); ++i) {
//                cm.ZX(i, srslts.a(i), cmps.row(i));
//            }
//
//        } else {
//            ISsf ssf = SsfofSSSTS.ofSeasonal(model, start);
//            srslts = DkToolkit.sqrtSmooth(ssf, data, true);
//            CompositeMeasurement cm = (CompositeMeasurement) ssf.getMeasurement();
//            cmps = new Matrix(data.getLength(), cm.getComponentsCount());
//            for (int i = 0; i < cmps.getRowsCount(); ++i) {
//                cm.ZX(i, srslts.a(i), cmps.row(i));
//            }
//        }
//        t = new TsData(y.getStart(), cmps.column(0).drop(0, nf));
//        ft = new TsData(y.getEnd(), cmps.column(0).drop(y.getLength(), 0));
//        s = new TsData(y.getStart(), cmps.column(1).drop(0, nf));
//        fs = new TsData(y.getEnd(), cmps.column(1).drop(y.getLength(), 0));
//        irr=TsData.subtract(y, TsData.add(t, s));
//        sa=TsData.subtract(y, s);
    }

    @Override
    public Map<String, Class> getDictionary() {
        // TODO
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        mapper.fillDictionary(null, map);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return mapper.getData(this, id, tclass);
    }

    @Override
    public boolean contains(String id) {
        synchronized (mapper) {
            return mapper.contains(id);
        }
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

    public SSSTSMonitor.MixedEstimation getBestModel() {
        return monitor.getBestModel();
    }

    public int getBestModelPosition() {
        return monitor.getBestModelPosition();
    }

    public List<SSSTSMonitor.MixedEstimation> getAllModels() {
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

    // MAPPERS
    public static <T> void addMapping(String name, InformationMapper.Mapper<SSSTSResults, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }
    private static final InformationMapper<SSSTSResults> mapper = new InformationMapper<>();

    static {
        mapper.add(ModellingDictionary.Y_CMP, new InformationMapper.Mapper<SSSTSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSSTSResults source) {
                return source.mul ? source.y.exp() : source.y;
            }
        });
        mapper.add(ModellingDictionary.T_CMP, new InformationMapper.Mapper<SSSTSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSSTSResults source) {
                return source.mul ? source.t.exp() : source.t;
            }
        });
        mapper.add(ModellingDictionary.SA_CMP, new InformationMapper.Mapper<SSSTSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSSTSResults source) {
                return source.mul ? source.sa.exp() : source.sa;
            }
        });
        mapper.add(ModellingDictionary.S_CMP, new InformationMapper.Mapper<SSSTSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSSTSResults source) {
                return source.mul ? source.s.exp() : source.s;
            }
        });
        mapper.add(ModellingDictionary.I_CMP, new InformationMapper.Mapper<SSSTSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSSTSResults source) {
                return source.mul ? source.irr.exp() : source.irr;
            }
        });
        mapper.add(ModellingDictionary.Y_LIN, new InformationMapper.Mapper<SSSTSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSSTSResults source) {
                return source.y;
            }
        });
        mapper.add(ModellingDictionary.T_LIN, new InformationMapper.Mapper<SSSTSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSSTSResults source) {
                return source.t;
            }
        });
        mapper.add(ModellingDictionary.SA_LIN, new InformationMapper.Mapper<SSSTSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSSTSResults source) {
                return source.sa;
            }
        });
        mapper.add(ModellingDictionary.S_LIN, new InformationMapper.Mapper<SSSTSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSSTSResults source) {
                return source.s;
            }
        });
        mapper.add(ModellingDictionary.I_LIN, new InformationMapper.Mapper<SSSTSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSSTSResults source) {
                return source.irr;
            }
        });
        mapper.add(ModellingDictionary.SI_CMP, new InformationMapper.Mapper<SSSTSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSSTSResults source) {
                TsData si = TsData.add(source.s, source.irr);
                if (si == null) {
                    return null;
                }
                return source.mul ? si.exp() : si;
            }
        });
        mapper.add("residuals", new InformationMapper.Mapper<SSSTSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSSTSResults source) {
                return source.getResiduals();
            }
        });
    }

    @Override
    public InformationSet getInformation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
