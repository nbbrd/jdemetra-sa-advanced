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

import be.nbb.demetra.sssts.ssf.SSHSEstimation;
import be.nbb.demetra.sssts.ssf.SsfofSSHS;
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
public class SSHSMonitor {

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
        SSHSModel m = new SSHSModel();
        m.setNoisyPeriods(Arrays2.EMPTY_INT_ARRAY);
        BsmMonitor bsmMonitor = new BsmMonitor();
        bsmMonitor.setSpecification(mspec);
        if (!bsmMonitor.process(series, freq)) {
            return false;
        }
        BasicStructuralModel bsm = bsmMonitor.getResult();
        if (bsm.getVariance(Component.Noise)<0)
            bsm.setVariance(Component.Noise, 0);
        m.setBasicStructuralMode(bsm);
        MixedEstimation rslt = new MixedEstimation();
        rslt.model = m;
        rslt.ll = DkToolkit.likelihoodComputer(true, true).compute(SsfBsm.create(bsm), new SsfData(m_series));
        
        m_models.add(rslt);
        boolean noise = sspec.noisyComponent == Component.Noise;
        m_best = 0;
        if (sspec.noisyPeriods != null) {
            m = new SSHSModel();
            m.setBasicStructuralMode(bsm);
            m.setNoisyPeriods(sspec.noisyPeriods);
            MixedEstimation nrslt = estimate(m, noise);
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
                    compute1(bsm, rslt.ll.getLogLikelihood(), noise);
                    break;
                case ErrorVariance:
                    compute2(bsm, rslt.ll.getLogLikelihood(), noise);
                    break;
                case LikelihoodGradient:
                    compute3(bsm, rslt.ll.getLogLikelihood(), noise);
                    break;
            }
            return true;
        }
    }

    private void compute1(BasicStructuralModel bsm, double refll, boolean noise) {
        int freq = m_series.getFrequency().intValue();
        boolean switched;
        int iter = 0;
        boolean[] noisy = new boolean[freq];
        do {
            switched = false;
            for (int i = 0; i < freq; ++i) {
                noisy[i] = !noisy[i];
                SSHSModel m = new SSHSModel();
                m.setBasicStructuralMode(bsm.clone());
                int[] noisyPeriods = buildNoisyPeriods(noisy);
                boolean success = false;
                if (noisyPeriods != null) {
                    m.setNoisyPeriods(noisyPeriods);
                    String name = m.toString();
                    if (!m_computed.contains(name)) {
                        m_computed.add(name);
                        MixedEstimation rslt = estimate(m, noise);
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

    private void compute3(BasicStructuralModel bsm, double refll, boolean noise) {
        // we compute the one-step-ahead forecast errors
        int[] np = sortNoisyPeriods2(bsm, noise);
        for (int i = 1; i < np.length - 1; ++i) {
            SSHSModel m = new SSHSModel();
            m.setBasicStructuralMode(bsm.clone());
            int[] noisyPeriods = Arrays.copyOf(np, i);
            m.setNoisyPeriods(noisyPeriods);
            String name = m.toString();
            m_computed.add(name);
            MixedEstimation rslt = estimate(m, noise);
            if (rslt != null) {
                m_models.add(rslt);
                double ll = rslt.ll.getLogLikelihood();
                if (ll > refll) {
                    m_best = m_models.size() - 1;
                    refll = ll;
                }
            }

        }
    }

    private void compute2(BasicStructuralModel bsm, double refll, boolean noise) {
        int freq = m_series.getFrequency().intValue();
        // we compute the one-step-ahead forecast errors
        IReadDataBlock res = m_models.get(0).ll.getResiduals();
        TsData residuals = new TsData(m_series.getStart().plus(m_series.getLength() - res.getLength()), res);
        int[] np = sortNoisyPeriods(residuals);
        for (int i = 1; i < np.length - 1; ++i) {
            SSHSModel m = new SSHSModel();
            m.setBasicStructuralMode(bsm.clone());
            int[] noisyPeriods = Arrays.copyOf(np, i);
            m.setNoisyPeriods(noisyPeriods);
            String name = m.toString();
            m_computed.add(name);
            MixedEstimation rslt = estimate(m, noise);
            if (rslt != null) {
                m_models.add(rslt);
                double ll = rslt.ll.getLogLikelihood();
                if (ll > refll) {
                    m_best = m_models.size() - 1;
                    refll = ll;
                }
            }

        }
    }

    private int[] sortNoisyPeriods2(BasicStructuralModel bsm, boolean noise) {

        double[] ll = new double[m_series.getFrequency().intValue()];
        for (int i = 0; i < ll.length; ++i) {
            SSHSModel m = new SSHSModel();
            m.setBasicStructuralMode(bsm.clone());
            m.setNoisyPeriods(new int[]{i});
            m.setNoisyPeriodsVariance(eps);
            SsfData data = new SsfData(m_series);
            // alg.useSsq(ssq);
            int pstart = m_series.getStart().getPosition();
            ILikelihood ill = DkToolkit.likelihoodComputer().compute(noise ? SsfofSSHS.ofNoise(m, pstart) : SsfofSSHS.ofSeasonal(m, pstart), data);

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

    private MixedEstimation estimate(SSHSModel model, boolean noise) {
        try {
            MixedEstimation me = new MixedEstimation();
            int pstart = m_series.getStart().getPosition();
            SSHSEstimation estimation = new SSHSEstimation(noise, pstart);
            me.model = estimation.compute2(model.getBasicStructuralModel(), model.getNoisyPeriods(), m_series);
            me.ll = DkToolkit.likelihoodComputer(true, true).compute(noise ? SsfofSSHS.ofNoise(me.model, pstart) : SsfofSSHS.ofSeasonal(me.model, pstart), new SsfData(m_series));
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
        public SSHSModel model;
    }

    public List<MixedEstimation> getAllResults() {
        return m_models;
    }
}
