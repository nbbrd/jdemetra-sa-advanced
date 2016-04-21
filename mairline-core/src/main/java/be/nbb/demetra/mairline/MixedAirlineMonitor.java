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
package be.nbb.demetra.mairline;

import be.nbb.demetra.mairline.ssf.MixedAirlineEstimation;
import be.nbb.demetra.mairline.ssf.MixedAirlineSsf;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.univariate.SsfData;
import ec.satoolkit.diagnostics.CochranTest;
import ec.tstoolkit.design.Development;
import ec.demetra.eco.ILikelihood;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.sarima.SarimaModel;
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
public class MixedAirlineMonitor {

    private TsData m_series;
    private int m_best = -1;
    private final HashSet<String> m_computed = new HashSet<>();
    private double eps = 1;

    public boolean process(final TsData series, final MaSpecification spec) {
        if (series == null) {
            return false;
        }
        eps = spec.step;
        m_series = series;
        m_computed.clear();
        m_models.clear();
        m_best = -1;
        int freq = series.getFrequency().intValue();
        MixedAirlineModel m = new MixedAirlineModel();
        m.setNoisyPeriods(Arrays2.EMPTY_INT_ARRAY);
        if (spec.airline != null) {
            m.setAirline(spec.airline);
        } else {
            m.setFrequency(freq);
        }
        MixedEstimation rslt = estimate(m);
        if (rslt == null) {
            return false;
        }
        SarimaModel airline = rslt.model.getAirline();
        m_models.add(rslt);

        m_best = 0;
        if (spec.noisyPeriods != null || spec.allPeriods) {
            m = new MixedAirlineModel();
            m.setAirline(airline);
            if (spec.allPeriods) {
                int[] np = new int[freq];
                for (int i = 0; i < freq; ++i) {
                    np[i] = i;

                }
                m.setNoisyPeriods(np);
            } else {
                m.setNoisyPeriods(spec.noisyPeriods);

            }
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
            switch (spec.method) {
                case Iterative:
                    compute1(airline, rslt.ll.getLogLikelihood());
                    break;
                case ErrorVariance:
                    compute2(airline, rslt.ll.getLogLikelihood());
                    break;
                case LikelihoodGradient:
                    compute3(airline, rslt.ll.getLogLikelihood());
                    break;
            }
            return true;
        }
    }

    private void compute1(SarimaModel airline, double refll) {
        int freq = m_series.getFrequency().intValue();
        boolean switched;
        int iter = 0;
        boolean[] noisy = new boolean[freq];
        do {
            switched = false;
            for (int i = 0; i < freq; ++i) {
                noisy[i] = !noisy[i];
                MixedAirlineModel m = new MixedAirlineModel();
                m.setAirline(airline.clone());
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

    private void compute3(SarimaModel airline, double refll) {
        // we compute the one-step-ahead forecast errors
        int[] np = sortNoisyPeriods2(airline);
        for (int i = 1; i < np.length - 1; ++i) {
            MixedAirlineModel m = new MixedAirlineModel();
            m.setAirline(airline.clone());
            int[] noisyPeriods = Arrays.copyOf(np, i);
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
                }
            }

        }
    }

    private void compute2(SarimaModel airline, double refll) {
        int freq = m_series.getFrequency().intValue();
        // we compute the one-step-ahead forecast errors
        IReadDataBlock res = m_models.get(0).ll.getResiduals();
        TsData residuals = new TsData(m_series.getStart().plus(m_series.getLength() - res.getLength()), res);
        int[] np = sortNoisyPeriods(residuals);
        for (int i = 1; i < np.length - 1; ++i) {
            MixedAirlineModel m = new MixedAirlineModel();
            m.setAirline(airline.clone());
            int[] noisyPeriods = Arrays.copyOf(np, i);
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
                }
            }

        }
    }

    private int[] sortNoisyPeriods2(SarimaModel airline) {

        double[] ll = new double[m_series.getFrequency().intValue()];
        for (int i = 0; i < ll.length; ++i) {
            MixedAirlineModel m = new MixedAirlineModel();
            m.setAirline(airline.clone());
            m.setNoisyPeriods(new int[]{i});
            m.setNoisyPeriodsVariance(eps);
            SsfData data = new SsfData(m_series);
            // alg.useSsq(ssq);
            ILikelihood ill = DkToolkit.likelihoodComputer().compute(MixedAirlineSsf.of(m, m_series.getStart().getPosition()), data);

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

    private MixedEstimation estimate(MixedAirlineModel model) {
        try {
            MixedEstimation me = new MixedEstimation();
            MixedAirlineEstimation estimation = new MixedAirlineEstimation();
            me.model = estimation.compute2(model.getAirline(), model.getNoisyPeriods(), m_series);
            me.ll = DkToolkit.likelihoodComputer(true, true).compute(MixedAirlineSsf.of(me.model), new SsfData(m_series));
            return me;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private ArrayList<MixedEstimation> m_models = new ArrayList<>();

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

        public ILikelihood ll;
        public MixedAirlineModel model;
    }

    public List<MixedEstimation> getAllResults() {
        return m_models;
    }
}
