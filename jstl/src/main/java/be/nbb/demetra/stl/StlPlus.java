/*
 * Copyright 2016 National Bank of Belgium
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
package be.nbb.demetra.stl;

import java.util.Arrays;

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

    private StlSpecification spec;
    private double[] y, seas, trend, irr;

    public boolean process(double[] data) {
        initialize(data);

        return true;
    }
    
    

    /**
     *
     * @param m
     * @param fits
     * @param slopes
     * @param at
     * @return
     */
    protected static double[] interp(double[] m, double[] fits, double[] slopes, double[] at) {

        int n_at = at.length;
        double[] ans = new double[n_at];

        int j = 0; // index of leftmost vertex
        for (int i = 0; i < n_at; ++i) {
            if (at[i] > m[j + 1]) {
                j++;
            }
            double h = (m[j + 1] - m[j]);
            double u = (at[i] - m[j]) / h;
            double u2 = u * u;
            double u3 = u2 * u;
            ans[i] = (2 * u3 - 3 * u2 + 1) * fits[j]
                    + (3 * u2 - 2 * u3) * fits[j + 1]
                    + (u3 - 2 * u2 + u) * slopes[j] * h
                    + (u3 - u2) * slopes[j + 1] * h;
        }
        return ans;
    }

    private void initialize(double[] data) {
        y = data;
        seas = new double[y.length];
        trend = new double[y.length];
    }

    protected static double[] movingAverage(double[] x, int n_p) {
        int i;
        int n = x.length;
        int nn = n - n_p * 2;
        int nn_p = n_p;
        double ma_tmp = 0;

        double[] ans = new double[n - 2 * nn_p];
        double[] ma = new double[nn + nn_p + 1];
        double[] ma2 = new double[nn + 2];

        for (i = 0; i < nn_p; ++i) {
            ma_tmp = ma_tmp + x[i];
        }
        ma[0] = ma_tmp / nn_p;
        for (i = nn_p; i < nn + 2 * nn_p; ++i) {
            ma_tmp = ma_tmp - x[i - nn_p] + x[i];
            ma[i - nn_p + 1] = ma_tmp / nn_p;
        }

        ma_tmp = 0;
        for (i = 0; i < nn_p; ++i) {
            ma_tmp = ma_tmp + ma[i];
        }
        ma2[0] = ma_tmp / nn_p;

        for (i = nn_p; i < nn + nn_p + 1; ++i) {
            ma_tmp = ma_tmp - ma[i - nn_p] + ma[i];
            ma2[i - nn_p + 1] = ma_tmp / nn_p;
        }

        ma_tmp = 0;

        for (i = 0; i < 3; ++i) {
            ma_tmp = ma_tmp + ma2[i];
        }
        ans[0] = ma_tmp / 3;

        for (i = 3; i < nn + 2; ++i) {
            ma_tmp = ma_tmp - ma2[i - 3] + ma2[i];
            ans[i - 2] = ma_tmp / 3;
        }

        return ans;
    }

    
    /**
     * See stlrw Robustness Weights rw_i := B( |y_i - fit_i| / (6 M) ), i =
     * 1,2,...,n where B(u) = (1 - u^2)^2 * 1[|u| < 1] {Tukey's biweight} and M
     * := median{ |y_i - fit_i| } @param fit @param rw
     */
    private void robustnessWeights(double[] fit, double[] rw) {
        int n = y.length;
        for (int i = 0; i < n; ++i) {
            rw[i] = Math.abs(y[i] - fit[i]);
        }
        double cmad = cmad(rw);
        double cu = .999 * cmad;
        double cl = .001 * cmad;

        for (int i = 0; i < n; ++n) {
            double u = Math.abs(y[i] - fit[i]);
            if (u <= cl) {
                rw[i] = 1;
            } else if (u < cu) {
                double tmp = u / cmad;
                tmp = 1 - tmp * tmp;
                rw[i] = tmp * tmp;
            } else {
                rw[i] = 1;
            }
        }
    }

    private double cmad(double[] w) {
        // computes 6*the median. We could use the partial sorting of the original code,
        // but the default routine is probably faster. To be tested !
        Arrays.sort(w);
        int n = w.length, m = n >> 1;
        if (n % 2 != 0) {
            return 6 * w[m];
        } else {
            return 3 * (w[m] + w[m - 1]);
        }
    }

    /**
     * See stlma Moving Average ("running mean") ave(i) := mean(x{j}, j =
     * max(1,i-k),..., min(n, i+k)) for i = 1,2,..,n
     *
     * @param x input (n data)
     * @param len length of the moving average
     * @param ave output (n - 2*(len-1))
     */
    protected static void movingAverage(double[] x, int len, double[] ave) {
        // optimized running mean. Stability could be an issue on very long series
        // and/or on a series with very erratic values
        double m = 0;
        for (int i = 0; i < len; ++i) {
            m += x[i];
        }
        ave[0] = m / len;
        for (int i = len, j = 0; i < x.length; ++i, ++j) {
            m += x[i] - x[j];
            ave[j + 1] = m / len;
        }
    }

    /**
     * @return the spec
     */
    public StlSpecification getSpecification() {
        return spec;
    }

    /**
     * @param spec the spec to set
     */
    public void setSpecification(StlSpecification spec) {
        this.spec = spec;
    }

    private static double[][] loess(
            double[] xx, // time values - should be 1:n unless there are some NAs
            double[] yy, // the corresponding y values
            int degree, // degree of smoothing
            int span, // span of smoothing
            double[] ww, // weights
            int[] m, // points at which to evaluate the smooth
            int[] l_idx, // index of left starting points
            double[] max_dist // distance between nn bounds for each point
    ) {

        int n = xx.length;
        int n_m = m.length;

        double[] x = new double[span];
        double[] w = new double[span];
        double[] xw = new double[span];
        double[] x2w = new double[span];
        double[] x3w = new double[span];

        double[] result = new double[n_m];
        double[] slopes = new double[n_m];

        if (span > n) {
            span = n;
        }

        // loop through all values of m
        for (int i = 0; i < n_m; i++) {
            double a = 0.0;

            // get weights, x, and a
            for (int j = 0; j < span; j++) {
                w[j] = 0.0;
                x[j] = xx[l_idx[i] + j] - (double) m[i];

                // r = std::fabs(x[j]);
                double r = (x[j] > 0) ? x[j] : -x[j];
                // tricube
                double tmp1 = r / max_dist[i];
                // manual multiplication is much faster than pow()
                double tmp2 = 1.0 - tmp1 * tmp1 * tmp1;
                w[j] = tmp2 * tmp2 * tmp2;

                // scale by user-defined weights
                w[j] = w[j] * ww[l_idx[i] + j];

                a = a + w[j];
            }

            if (degree == 0) {
                // TODO: make sure denominator is not 0
                double a1 = 1 / a;
                for (int j = 0; j < span; j++) {
                    // l_i[j] = w[j] * a1;
                    result[i] = result[i] + w[j] * a1 * yy[l_idx[i] + j];
                }
            } else {
                // get xw, x2w, b, c for degree 1 or 2
                double b = 0.0;
                double c = 0.0;
                for (int j = 0; j < span; j++) {
                    xw[j] = x[j] * w[j];
                    x2w[j] = x[j] * xw[j];
                    b = b + xw[j];
                    c = c + x2w[j];
                }
                if (degree == 1) {
                    // TODO: make sure denominator is not 0
                    double det = 1 / (a * c - b * b);
                    double a1 = c * det;
                    double b1 = -b * det;
                    double c1 = a * det;
                    for (int j = 0; j < span; j++) {
                        result[i] = result[i] + (w[j] * a1 + xw[j] * b1) * yy[l_idx[i] + j];
                        slopes[i] = slopes[i] + (w[j] * b1 + xw[j] * c1) * yy[l_idx[i] + j];
                    }
                } else {
                    // TODO: make sure degree > 2 cannot be specified (and < 0 for that matter)
                    // get x3w, d, and e for degree 2
                    double d = 0.0;
                    double e = 0.0;
                    for (int j = 0; j < span; j++) {
                        x3w[j] = x[j] * x2w[j];
                        d = d + x3w[j];
                        e = e + x3w[j] * x[j];
                    }
                    double a1 = e * c - d * d;
                    double b1 = c * d - e * b;
                    double c1 = b * d - c * c;
                    double a2 = c * d - e * b;
                    double b2 = e * a - c * c;
                    double c2 = b * c - d * a;
                    // TODO: make sure denominator is not 0
                    double det = 1 / (a * a1 + b * b1 + c * c1);
                    a1 = a1 * det;
                    b1 = b1 * det;
                    c1 = c1 * det;
                    a2 = a2 * det;
                    b2 = b2 * det;
                    c2 = c2 * det;
                    for (int j = 0; j < span; j++) {
                        result[i] = result[i] + (w[j] * a1 + xw[j] * b1 + x2w[j] * c1) * yy[l_idx[i] + j];
                        slopes[i] = slopes[i] + (w[j] * a2 + xw[j] * b2 + x2w[j] * c2) * yy[l_idx[i] + j];
                    }
                }
            }
        }
        return new double[][]{result, slopes};
    }

    protected static double[] c_ma(double[] x, int n_p) {
        int i;
        int n = x.length;
        int nn = n - n_p * 2;
        int nn_p = n_p;
        double ma_tmp = 0;

        double[] ans = new double[n - 2 * nn_p];
        double[] ma = new double[nn + nn_p + 1];
        double[] ma2 = new double[nn + 2];
        double[] ma3 = new double[nn];

        for (i = 0; i < nn_p; ++i) {
            ma_tmp = ma_tmp + x[i];
        }
        ma[0] = ma_tmp / nn_p;
        for (i = nn_p; i < nn + 2 * nn_p; ++i) {
            ma_tmp = ma_tmp - x[i - nn_p] + x[i];
            ma[i - nn_p + 1] = ma_tmp / nn_p;
        }

        ma_tmp = 0;
        for (i = 0; i < nn_p; ++i) {
            ma_tmp = ma_tmp + ma[i];
        }
        ma2[0] = ma_tmp / nn_p;

        for (i = nn_p; i < nn + nn_p + 1; ++i) {
            ma_tmp = ma_tmp - ma[i - nn_p] + ma[i];
            ma2[i - nn_p + 1] = ma_tmp / nn_p;
        }

        ma_tmp = 0;

        for (i = 0; i < 3; ++i) {
            ma_tmp = ma_tmp + ma2[i];
        }
        ans[0] = ma_tmp / 3;

        for (i = 3; i < nn + 2; ++i) {
            ma_tmp = ma_tmp - ma2[i - 3] + ma2[i];
            ans[i - 2] = ma_tmp / 3;
        }

        return ans;
    }
}
