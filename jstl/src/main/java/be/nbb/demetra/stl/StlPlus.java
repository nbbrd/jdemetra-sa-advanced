/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this fit except in compliance with the Licence.
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
package be.nbb.demetra.stl;

import ec.demetra.realfunctions.RealFunction;
import ec.tstoolkit.data.IReadDataBlock;
import java.util.Arrays;
import java.util.function.IntToDoubleFunction;

/**
 * Java implementation of the original FORTRAN routine
 *
 * Source; R.B. Cleveland, W.S.Cleveland, J.E. McRae, and I. Terpenning, STL: A
 * Seasonal-Trend Decomposition Procedure Based on Loess, Statistics Research
 * Report, AT&T Bell Laboratories.
 *
 * @author Jean Palate
 */
public class StlPlus {

    protected static final RealFunction W = x -> {
        double t = 1 - x * x * x;
        return t * t * t;
    };

    private final LoessFilter tfilter;
    private final SeasonalFilter[] sfilter;
    protected RealFunction wfn = x -> {
        double t = 1 - x * x;
        return t * t;
    };

    protected RealFunction loessfn = x -> {
        double t = 1 - x * x * x;
        return t * t * t;
    };

    protected double[] y;
    protected double[][] season;
    protected double[] trend;
    protected double[] irr;
    protected double[] weights;
    protected double[] fit;

    private static final int MAXSTEP = 100;

    private int ni = 2, no = 0;
    private double wthreshold = .001;

    private int n() {
        return y.length;
    }

    public StlPlus(final LoessFilter tfilter, final SeasonalFilter sfilter) {
        this.tfilter = tfilter;
        this.sfilter = new SeasonalFilter[]{sfilter};
    }

    public StlPlus(final LoessFilter tfilter, final SeasonalFilter[] sfilter) {
        this.tfilter = tfilter;
        this.sfilter = sfilter;
    }

    public StlPlus(final int period, final int swindow) {
        LoessSpecification sspec = LoessSpecification.of(swindow, 0);
        int twindow = (int) Math.ceil((1.5 * period) / (1 - 1.5 / swindow));
        if (twindow % 2 == 0) {
            ++twindow;
        }
        LoessSpecification tspec = LoessSpecification.of(twindow);
        tfilter = new LoessFilter(tspec);
        sfilter = new SeasonalFilter[]{new SeasonalFilter(sspec, LoessSpecification.of(period + 1), period)};
    }

    public boolean process(IReadDataBlock data) {

        if (!initializeProcessing(data)) {
            return false;
        }
        int istep = 0;
        do {
            stlstp();
            if (++istep > no) {
                return finishProcessing();
            }
            if (weights == null) {
                weights = new double[n()];
                fit = new double[n()];
            }
            for (int i = 0; i < n(); ++i) {
                fit[i] = trend[i];
                for (int j = 0; j < season.length; ++j) {
                    fit[i] += season[j][i];
                }
            }
            stlrwt(fit, weights);
        } while (true);
    }

    private boolean finishProcessing() {
        for (int i = 0; i < n(); ++i) {
            fit[i] = trend[i];
            for (int j = 0; j < season.length; ++j) {
                fit[i] += season[j][i];
            }
            irr[i] = y[i] - fit[i];
        }
        return true;
    }

    private boolean initializeProcessing(IReadDataBlock data) {
        int n = data.getLength();
        y = new double[n];
        data.copyTo(y, 0);
        fit = new double[n];
        season = new double[sfilter.length][];
        for (int i = 0; i < sfilter.length; ++i) {
            season[i] = new double[n];
        }
        trend = new double[n];
        irr = new double[n];
        return true;
    }

    private static double mad(double[] r) {
        double[] sr = r.clone();
        Arrays.sort(sr);
        int n = r.length;
        int n2 = n >> 1;
        if (n % 2 != 0) {
            return 6 * sr[n2];
        } else {
            return 3 * (sr[n2 - 1] + sr[n2]);
        }
    }

    private void stlrwt(double[] fit, double[] w) {

        int n = n();
        for (int i = 0; i < n; ++i) {
            w[i] = Math.abs(y[i] - fit[i]);
        }

        double mad = mad(w);

        double c1 = wthreshold * mad;
        double c9 = (1 - wthreshold) * mad;

        for (int i = 0; i < n; ++i) {
            double r = w[i];
            if (r <= c1) {
                w[i] = 1;
            } else if (r <= c9) {
                w[i] = wfn.apply(r / mad);
            } else {
                w[i] = 0;
            }
        }

    }

    /**
     * Moving Average (aka "running mean") ave(i) := mean(x{j}, j =
     * max(1,i-k),..., min(n, i+k)) for i = 1,2,..,n
     *
     * @param len
     * @param n
     * @param x
     * @param ave
     */
    protected static void stlma(int len, int n, double[] x, double[] ave) {
        int newn = n - len + 1;
        double v = 0, flen = len;
        for (int i = 0; i < len; ++i) {
            v += x[i];
        }
        ave[0] = v / flen;
        if (newn > 1) {
            for (int i = 1, k = len, m = 0; i < newn; ++i, ++k, ++m) {
                v = v - x[m] + x[k];
                ave[i] = v / flen;
            }
        }
    }

    /**
     *
     * @param np
     * @param n
     * @param x
     * @param t
     */
    protected static void stlfts(int np, double[] x, double[] t) {
        int n = x.length;
        double[] w1 = new double[n];
        double[] w2 = new double[n];
        stlma(np, n, x, w1);
        stlma(np, n - np + 1, w1, w2);
        stlma(3, n - 2 * np + 2, w2, t);
    }

    protected double stlest(IntToDoubleFunction y, int n, int len, int degree, double xs, int nleft, int nright, IntToDoubleFunction userWeights) {
        double[] w = new double[n];
        double range = n - 1;
        double h = Math.max(xs - nleft, nright - xs);
        if (len > n) {
            h += (len - n) * .5;
        }
        double h9 = 0.999 * h;
        double h1 = 0.001 * h;
        double a = 0;
        for (int j = nleft; j <= nright; ++j) {
            double r = Math.abs(j - xs);
            if (r < h9) {
                if (r < h1) {
                    w[j] = 1;
                } else {
                    w[j] = loessfn.apply(r / h);
                }

                if (userWeights != null) {
                    w[j] *= userWeights.applyAsDouble(j);
                }
                a += w[j];
            }
        }

        if (a <= 0) {
            return Double.NaN;
        } else {
            for (int j = nleft; j <= nright; ++j) {
                w[j] /= a;
            }
            if (h > 0 && degree > 0) {
                a = 0;
                for (int j = nleft; j <= nright; ++j) {
                    a += w[j] * j;
                }
                double b = xs - a;
                double c = 0;
                for (int j = nleft; j <= nright; ++j) {
                    double ja = j - a;
                    c += w[j] * ja * ja;
                }
                if (Math.sqrt(c) > .001 * range) {
                    b /= c;

                    for (int j = nleft; j <= nright; ++j) {
                        w[j] *= b * (j - a) + 1;
                    }
                }
            }
            double ys = 0;
            for (int j = nleft; j <= nright; ++j) {
                ys += w[j] * y.applyAsDouble(j);
            }
            return ys;
        }
    }

    protected void stless(IntToDoubleFunction y, int n, int len, int degree, int njump, IntToDoubleFunction userWeights, double[] ys) {

        if (n < 2) {
            ys[0] = y.applyAsDouble(0);
            return;
        }
        int newnj = Math.min(njump, n - 1);
        int nleft = 0, nright = 0;
        if (len >= n) {
            nleft = 0;
            nright = n - 1;
            for (int i = 0; i < n; i += newnj) {
                double yscur = stlest(y, n, len, degree, i, nleft, nright, userWeights);
                if (Double.isFinite(yscur)) {
                    ys[i] = yscur;
                } else {
                    ys[i] = y.applyAsDouble(i);
                }
            }
        } else {
            if (newnj == 1) {
                int nsh = (len - 1) >> 1;
                nleft = 0;
                nright = len - 1;
                for (int i = 0; i < n; ++i) {
                    if (i > nsh && nright != n - 1) {
                        ++nleft;
                        ++nright;
                    }
                    double yscur = stlest(y, n, len, degree, i, nleft, nright, userWeights);
                    if (Double.isFinite(yscur)) {
                        ys[i] = yscur;
                    } else {
                        ys[i] = y.applyAsDouble(i);
                    }
                }
            } else {
                int nsh = (len - 1) >> 1;
                for (int i = 0; i < n; i += newnj) {
                    if (i < nsh) {
                        nleft = 0;
                        nright = len - 1;
                    } else if (i >= n - nsh) {
                        nleft = n - len;
                        nright = n - 1;
                    } else {
                        nleft = i - nsh;
                        nright = i + nsh;
                    }

                    double yscur = stlest(y, n, len, degree, i, nleft, nright, userWeights);
                    if (Double.isFinite(yscur)) {
                        ys[i] = yscur;
                    } else {
                        ys[i] = y.applyAsDouble(i);
                    }
                }
            }
            if (newnj != 1) {

                int i = 0;
                for (; i < n - newnj; i += newnj) {
                    double delta = (ys[i + newnj] - ys[i]) / newnj;
                    for (int j = i + 1; j < i + newnj; ++j) {
                        ys[j] = ys[i] + delta * (j - i);
                    }
                }

                if (i != n - 1) {
                    double yscur = stlest(y, n, len, degree, n - 1, nleft, nright, userWeights);
                    if (Double.isFinite(yscur)) {
                        ys[n - 1] = yscur;
                    } else {
                        ys[n - 1] = y.applyAsDouble(n - 1);
                    }
                    double delta = (ys[n - 1] - ys[i]) / (n - i - 1);
                    for (int j = i + 1; j < n - 1; ++j) {
                        ys[j] = ys[i] + delta * (j - i);
                    }
                }
            }
        }
    }

    protected void stlstp() {
        int n = n();
        double[] si = new double[n];
        double[] w = new double[n];
        // Step 1: SI=Y-T

        for (int j = 0; j < ni; ++j) {

            for (int i = 0; i < n; ++i) {
                si[i] = y[i] - trend[i];
            }
            // compute S
            for (int s = 0; s < sfilter.length; ++s) {
                sfilter[s].filter(IDataGetter.of(si), weights == null ? null : k -> weights[k], IDataSelector.of(season[s]));
            }
            // seasonal adjustment
            for (int i = 0; i < n; ++i) {
                w[i] = y[i];
                for (int s = 0; s < sfilter.length; ++s) {
                    w[i] -= season[s][i];
                }
            }
            // Step 6: T=smooth(sa)
            tfilter.filter(IDataSelector.of(w), weights == null ? null : k -> weights[k], IDataSelector.of(trend));
        }
    }

    /**
     * @return the y
     */
    public double[] getY() {
        return y;
    }

    /**
     * @return the season
     */
    public double[] getSeason(int i) {
        return season[i];
    }

    /**
     * @return the trend
     */
    public double[] getTrend() {
        return trend;
    }

    /**
     * @return the irr
     */
    public double[] getIrr() {
        return irr;
    }

    /**
     * @return the weights
     */
    public double[] getWeights() {
        return weights;
    }

    /**
     * @return the fit
     */
    public double[] getFit() {
        return fit;
    }

    /**
     * @return the tfilter
     */
    public LoessFilter getTfilter() {
        return tfilter;
    }

    /**
     * @return sfilter1lter
     */
    public SeasonalFilter[] getSfilter() {
        return sfilter;
    }

    /**
     * @return the ni
     */
    public int getNi() {
        return ni;
    }

    /**
     * @param ni the ni to set
     */
    public void setNi(int ni) {
        this.ni = ni;
    }

    /**
     * @return the no
     */
    public int getNo() {
        return no;
    }

    /**
     * @param no the no to set
     */
    public void setNo(int no) {
        this.no = no;
    }

    /**
     * @return the wthreshold
     */
    public double getWthreshold() {
        return wthreshold;
    }

    /**
     * @param wthreshold the wthreshold to set
     */
    public void setWthreshold(double wthreshold) {
        this.wthreshold = wthreshold;
    }
}
