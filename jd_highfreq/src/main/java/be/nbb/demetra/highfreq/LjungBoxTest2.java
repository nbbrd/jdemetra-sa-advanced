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
package be.nbb.demetra.highfreq;

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.dstats.Chi2;
import ec.tstoolkit.dstats.TestType;
import ec.tstoolkit.stats.StatisticalTest;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LjungBoxTest2 extends StatisticalTest {

    private int[] lags;
    private int hp;
    private boolean pos;
    private double[] x, ac;

    /**
     * Creates new LjungBoxTest
     */
    public LjungBoxTest2() {
        pos = true;
    }

    /**
     *
     * @return
     */
    public int getHyperParametersCount() {
        return hp;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        if (!m_computed) {
            test();
        }
        return m_dist != null;
    }

    /**
     *
     * @param value
     */
    public void setHyperParametersCount(final int value) {
            hp = value;
            clear();
    }

    /**
     * By default, by setting a lag greater than 1, the use of positive
     * auto-correlations (ac) in enabled (negative ac are considered as 0 and no
     * further ac are taken into account). The method "usePositiveAc(true)"
     * should be called to disable this behaviour
     *
     * @param value
     */
    public void setLags(final int[] value) {
        lags = value;
        clear();
    }
    
    public int[] getLags(){
        return lags;
    }

    public IReadDataBlock getAutoCorrelations(){
        return new ReadDataBlock(ac);
    }

    public void usePositiveAc(boolean pos) {
        pos = pos;
        clear();
    }

    private void test() {
        try {
            ac=new double[lags.length];
            double var=DescriptiveStatistics.cov(0, x);
            double res = 0.0;
            int n=x.length;
            for (int i = 0; i < lags.length; i++) {
                double ai = DescriptiveStatistics.cov(lags[i], x)/var;
                ac[i]=ai;
                if (!pos || ai > 0) {
                    res += ai * ai / (n - lags[i]);
                }
            }
            m_val = res * n * (n + 2);
            Chi2 chi = new Chi2();
            chi.setDegreesofFreedom(lags.length - hp);
            m_dist = chi;
            m_type = TestType.Upper;
            m_asympt = true;

        } catch (Exception err) {
            m_dist = null;
        } finally {
            m_computed = true;
        }
    }


    /**
     *
     * @param data
     */
    public void test(IReadDataBlock data) {
        x=new double[data.getLength()];
        data.copyTo(x, 0);
        test();
    }

    /**
     * Computes the Ljung-Box statistics
     *
     * @param data The data
     * @param lag The considered lag. Usually 1 or freq
     * @param k The number of auto-correlations
     * @param pos
     * @return The Ljung-Box statistics
     */
    public static double calc(double[] data, int lag, int k, boolean pos) {
        int n = data.length;
        double res = 0.0;
        double v = DescriptiveStatistics.cov(0, data);
        for (int i = 1; i <= k; i++) {
            double ac = DescriptiveStatistics.cov(lag * i, data) / v;
            if (!pos || ac > 0) {
                res += ac * ac / (n - i * lag);
            } else if (i == 1) {
                return 0;
            }
        }
        res *= n * (n + 2);
        return res;
    }
}
