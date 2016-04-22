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
import ec.demetra.ssf.univariate.DefaultSmoothingResults;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.SsfData;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.ISaResults;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.seats.SeatsToolkit;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.tstoolkit.ucarima.WienerKolmogorovEstimators;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class SSHSResults implements ISaResults {

    SSHSResults(TsData y, SSHSMonitor monitor, boolean b) {
        this.y = y;
        mul = b;
        this.monitor = monitor;
        SSHSMonitor.MixedEstimation rslt = monitor.getBestModel();
        if (rslt != null) {
            computeDecomposition(rslt.model);
        }
    }
    public static final String NOISE = "noisecomponent", NOISE_DATA = "noise", IRREGULAR = "irregular", NOISE_LBOUND = "lbound", NOISE_UBOUND = "ubound";
    private final SSHSMonitor monitor;
    private final boolean mul;
    private final TsData y;
    private DefaultSmoothingResults srslts;
    private TsData yc, t, sa, s, noise, level, slope, cycle;

    private void computeDecomposition(SSHSModel model) {
        SsfData data = new SsfData(y);
        ISsf ssf = SsfofSSHS.ofNoise(model, y.getStart().getPosition());
        srslts = DkToolkit.sqrtSmooth(ssf, data, true);
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
            decomposition.add(noise.exp(), ComponentType.Irregular);
        } else {
            decomposition.add(y, ComponentType.Series);
            decomposition.add(sa, ComponentType.SeasonallyAdjusted);
            decomposition.add(t, ComponentType.Trend);
            decomposition.add(s, ComponentType.Seasonal);
            decomposition.add(noise, ComponentType.Irregular);
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
        return noise;
    }

    // MAPPERS
    public static <T> void addMapping(String name, InformationMapper.Mapper<SSHSResults, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }
    private static final InformationMapper<SSHSResults> mapper = new InformationMapper<>();

    static {
        mapper.add(ModellingDictionary.Y_CMP, new InformationMapper.Mapper<SSHSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSHSResults source) {
                return source.mul ? source.y.exp() : source.y;
            }
        });
        mapper.add(ModellingDictionary.T_CMP, new InformationMapper.Mapper<SSHSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSHSResults source) {
                return source.mul ? source.t.exp() : source.t;
            }
        });
        mapper.add(ModellingDictionary.SA_CMP, new InformationMapper.Mapper<SSHSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSHSResults source) {
                return source.mul ? source.sa.exp() : source.sa;
            }
        });
        mapper.add(ModellingDictionary.S_CMP, new InformationMapper.Mapper<SSHSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSHSResults source) {
                return source.mul ? source.s.exp() : source.s;
            }
        });
        mapper.add(ModellingDictionary.I_CMP, new InformationMapper.Mapper<SSHSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSHSResults source) {
                return source.mul ? source.noise.exp() : source.noise;
            }
        });
        mapper.add(ModellingDictionary.Y_LIN, new InformationMapper.Mapper<SSHSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSHSResults source) {
                return source.y;
            }
        });
        mapper.add(ModellingDictionary.T_LIN, new InformationMapper.Mapper<SSHSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSHSResults source) {
                return source.t;
            }
        });
        mapper.add(ModellingDictionary.SA_LIN, new InformationMapper.Mapper<SSHSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSHSResults source) {
                return source.sa;
            }
        });
        mapper.add(ModellingDictionary.S_LIN, new InformationMapper.Mapper<SSHSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSHSResults source) {
                return source.s;
            }
        });
        mapper.add(ModellingDictionary.I_LIN, new InformationMapper.Mapper<SSHSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSHSResults source) {
                return source.noise;
            }
        });
        mapper.add(ModellingDictionary.SI_CMP, new InformationMapper.Mapper<SSHSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSHSResults source) {
                TsData si = TsData.add(source.s, source.noise);
                if (si == null) {
                    return null;
                }
                return source.mul ? si.exp() : si;
            }
        });
        mapper.add("residuals", new InformationMapper.Mapper<SSHSResults, TsData>(TsData.class) {

            @Override
            public TsData retrieve(SSHSResults source) {
                return source.getResiduals();
            }
        });
    }

    @Override
    public InformationSet getInformation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
