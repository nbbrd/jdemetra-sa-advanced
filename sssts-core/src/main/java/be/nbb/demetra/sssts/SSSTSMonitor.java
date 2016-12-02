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

import be.nbb.demetra.sssts.ssf.SSSTSEstimation;
import be.nbb.demetra.sssts.ssf.SsfofSSSTS;
import ec.demetra.ssf.implementations.structural.BasicStructuralModel;
import be.nbb.demetra.sts.BsmMonitor;
import ec.demetra.ssf.implementations.structural.ModelSpecification;
import ec.demetra.ssf.implementations.structural.SsfBsm;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.univariate.SsfData;
import ec.satoolkit.diagnostics.CochranTest;
import ec.tstoolkit.design.Development;
import ec.demetra.eco.ILikelihood;
import ec.demetra.ssf.dk.DkLikelihood;
import ec.demetra.ssf.implementations.structural.Component;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.Arrays2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class SSSTSMonitor {

    private TsData m_series;
    private int m_best = -1;
    private final HashSet<String> m_computed = new HashSet<>();
    private double eps = 1;

    public boolean process(final TsData series, final ModelSpecification mspec, final SeasonalSpecification sspec) {
        if (series == null) {
            return false;
        }
        eps = sspec.step;
        m_series = series;
        m_computed.clear();
        m_models.clear();
        m_best = -1;
        int freq = series.getFrequency().intValue();
        SSSTSModel m = new SSSTSModel();
        m.setNoisyComponent(sspec.noisyComponent);
        m.setNoisyPeriods(Arrays2.EMPTY_INT_ARRAY);
        m.setFrequency(freq);
        BsmMonitor bsmMonitor = new BsmMonitor();
        bsmMonitor.setSpecification(mspec);
        if (!bsmMonitor.process(series, freq)) {
            return false;
        }
        BasicStructuralModel bsm = bsmMonitor.getResult();
        if (bsm.getVariance(Component.Noise) < 0) {
            bsm.setVariance(Component.Noise, 0);
        }
        initialize(m, bsm);
        MixedEstimation rslt = estimate(m);
        m_models.add(rslt);
        m_best = 0;
        if (sspec.noisyPeriods != null) {
            m = rslt.model.clone();
            m.setNoisyPeriods(sspec.noisyPeriods);
            MixedEstimation nrslt = estimate(m);
            if (nrslt != null) {
                m_models.add(nrslt);
                double ll = nrslt.ll.getLogLikelihood();
                if (ll > rslt.ll.getLogLikelihood() + 2) {
                    m_best = 1;
                } else {
                    m_best = 0;
                }
                return true;
            } else {
                return false;
            }

        } else {
            switch (sspec.method) {
                case Iterative:
                    compute1(rslt.model, rslt.ll.getLogLikelihood());
                    break;
                case ErrorVariance:
                    compute2(rslt.model, rslt.ll.getLogLikelihood());
                    break;
                case LikelihoodGradient:
                    compute3(rslt.model, rslt.ll.getLogLikelihood());
                    break;
            }
            return true;
        }
    }

    private void initialize(SSSTSModel m, BasicStructuralModel bsm) {
        m.setLvar(bsm.getVariance(Component.Level));
        m.setSvar(bsm.getVariance(Component.Slope));
        m.setSeasvar(bsm.getVariance(Component.Seasonal));
        m.setNvar(bsm.getVariance(Component.Noise));
        m.setNoisyPeriodsVariance(1);
    }


    private void compute1(SSSTSModel model, double refll) {
        int freq = m_series.getFrequency().intValue();
        boolean switched;
        int iter = 0;
        boolean[] noisy = new boolean[freq];
        do {
            switched = false;
            for (int i = 0; i < freq; ++i) {
                noisy[i] = !noisy[i];
                SSSTSModel m = model.clone();
                int[] noisyPeriods = buildNoisyPeriods(noisy);
                boolean success = false;
                if (noisyPeriods != null) {
                    m.setNoisyPeriods(noisyPeriods);
                    String name = m.toString();
                    if (!m_computed.contains(name)) {
                        m_computed.add(name);
                        MixedEstimation rslt = estimate(m);
                        if (rslt != null) {
                            m_models.add(rslt);
                            double ll = rslt.ll.getLogLikelihood();
                            if (ll > refll) {
                                m_best = m_models.size() - 1;
                                refll = ll;
                                switched = true;
                                success = true;
                            }
                        }
                    }
                }
                if (!success) {
                    noisy[i] = !noisy[i];
                }
            }
        } while (iter++ <= 5 && switched);
    }

    private static final double DLL = 5;

    private void compute3(SSSTSModel model, double refll) {
        // we compute the one-step-ahead forecast errors
        int[] np = sortNoisyPeriods2(model);
        int[] usednp=new int[np.length];
        int nused=0;
        for (int i = 0; i < np.length; ++i) {
            usednp[nused]=np[i];
            SSSTSModel m = model.clone();
            int[] noisyPeriods = Arrays.copyOf(usednp, nused+1);
            m.setNoisyPeriods(noisyPeriods);
            String name = m.toString();
            m_computed.add(name);
            MixedEstimation rslt = estimate(m);
            if (rslt != null) {
                m_models.add(rslt);
                double ll = rslt.ll.getLogLikelihood();
                if (ll > refll) {
                    m_best = m_models.size() - 1;
                    refll = ll;
                    ++nused;
                } else if (ll < refll - DLL) {
                    break;
                }
            }
        }
    }

    private void compute2(SSSTSModel model, double refll) {
        // we compute the one-step-ahead forecast errors
        IReadDataBlock res = m_models.get(0).ll.getResiduals();
        TsData residuals = new TsData(m_series.getStart().plus(m_series.getLength() - res.getLength()), res);
        int[] np = sortNoisyPeriods(residuals);
        int[] usednp=new int[np.length];
        int nused=0;
        for (int i = 0; i <np.length; ++i) {
            usednp[nused]=np[i];
            SSSTSModel m = model.clone();
            int[] noisyPeriods = Arrays.copyOf(usednp, nused+1);
            m.setNoisyPeriods(noisyPeriods);
            String name = m.toString();
            m_computed.add(name);
            MixedEstimation rslt = estimate(m);
            if (rslt != null) {
                m_models.add(rslt);
                double ll = rslt.ll.getLogLikelihood();
                if (ll > refll) {
                    m_best = m_models.size() - 1;
                    refll = ll;
                    ++nused;
                }
            }

        }
    }

    private int[] sortNoisyPeriods2(SSSTSModel model) {

        double[] ll = new double[m_series.getFrequency().intValue()];
        for (int i = 0; i < ll.length; ++i) {
            SSSTSModel m = model.clone();
            m.setNoisyPeriods(new int[]{i});
            m.setNoisyPeriodsVariance(eps);
            SsfData data = new SsfData(m_series);
            // alg.useSsq(ssq);
            int pstart = m_series.getStart().getPosition();
            ILikelihood ill = DkToolkit.likelihoodComputer().compute(SsfofSSSTS.of(m, pstart), data);

            if (ill != null) {
                ll[i] = ill.getLogLikelihood();
            }
        }

        int[] sp = new int[ll.length];
        boolean[] used = new boolean[sp.length];
        for (int i = 0; i < sp.length; ++i) {
            int jmax = 0;
            double vmax = -Double.MAX_VALUE;
            for (int j = 0; j < sp.length; ++j) {
                if (!used[j] && ll[j] > vmax) {
                    jmax = j;
                    vmax = ll[j];
                }
            }
            used[jmax] = true;
            sp[i] = jmax;
        }
        return sp;
    }

    private int[] sortNoisyPeriods(TsData residuals) {
        // compute the one-step ahead residuals...

        // computes the Cochran test on the residuals
        CochranTest test = new CochranTest(residuals, false);
        test.calcCochranTest();
        double[] svar = test.getS();
        int[] sp = new int[svar.length];
        boolean[] used = new boolean[sp.length];
        for (int i = 0; i < sp.length; ++i) {
            int jmax = 0;
            double vmax = 0;
            for (int j = 0; j < sp.length; ++j) {
                if (!used[j] && svar[j] > vmax) {
                    jmax = j;
                    vmax = svar[j];
                }
            }
            used[jmax] = true;
            sp[i] = jmax;
        }
        return sp;
    }

    public MixedEstimation getBestModel() {
        if (m_best < 0) {
            return null;
        }
        return m_models.get(m_best);
    }

    public int getBestModelPosition() {
        return m_best;
    }

    private MixedEstimation estimate(SSSTSModel model) {
        try {
            MixedEstimation me = new MixedEstimation();
            int pstart = m_series.getStart().getPosition();
            SSSTSEstimation estimation = new SSSTSEstimation();
            me.model = estimation.compute2(model, m_series);
            me.ll = DkToolkit.likelihoodComputer(true, true).compute(SsfofSSSTS.of(me.model, pstart), new SsfData(m_series));
            return me;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private final ArrayList<MixedEstimation> m_models = new ArrayList<>();

    private int[] buildNoisyPeriods(boolean[] noisy) {
        int n = 0;
        for (int i = 0; i < noisy.length; ++i) {
            if (noisy[i]) {
                ++n;
            }
        }
        if (n == 0) {
            return null;
        }
        int[] p = new int[n];
        for (int i = 0, j = 0; j < n; ++i) {
            if (noisy[i]) {
                p[j++] = i;
            }
        }
        return p;
    }

    public static class MixedEstimation {

        public DkLikelihood ll;
        public SSSTSModel model;
    }

    public List<MixedEstimation> getAllResults() {
        return m_models;
    }
}
