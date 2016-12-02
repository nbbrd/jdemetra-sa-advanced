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

import ec.demetra.ssf.implementations.structural.Component;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class SSSTSModel implements Cloneable {

    private double lvar, svar, nvar, seasvar; // common innovation variances (level/slope/noise)
    private int[] np;
    private double nseasvar; // seasonal specific innovation variance
    private Component noisy = Component.Noise; // true if the noise is seasonal specific, false if level is seasonal specific
    private int frequency = 12;

    public SSSTSModel clone() {
        try {
            return (SSSTSModel) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    public void setNoisyPeriods(int[] periods) {
        np = periods == null ? null : periods.clone();
    }

    public int[] getNoisyPeriods() {
        return np;
    }

    public double getNoisyPeriodsVariance() {
        return nseasvar;
    }

    public void setNoisyPeriodsVariance(double svar) {
        this.nseasvar = svar;
    }

    @Override
    public String toString() {
        if (np.length == 0) {
            return "sslltm";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("het-sslltm");
        builder.append('[');
        builder.append(np[0] + 1);
        for (int i = 1; i < np.length; ++i) {
            builder.append("  ").append(np[i] + 1);
        }
        builder.append(']');
        return builder.toString();
    }

    /**
     * @return the lvar
     */
    public double getLvar() {
        return lvar;
    }

    /**
     * @param lvar the lvar to set
     */
    public void setLvar(double lvar) {
        this.lvar = lvar;
    }

    /**
     * @return the svar
     */
    public double getSvar() {
        return svar;
    }

    /**
     * @param svar the svar to set
     */
    public void setSvar(double svar) {
        this.svar = svar;
    }

    /**
     * @return the nvar
     */
    public double getNvar() {
        return nvar;
    }

    /**
     * @param evar the nvar to set
     */
    public void setNvar(double evar) {
        this.nvar = evar;
    }

    /**
     * @return the noisy component
     */
    public Component getNoisyComponent() {
        return noisy;
    }

    /**
     * @param noisy the noisy component to set
     */
    public void setNoisyComponent(Component noisy) {
        this.noisy = noisy;
    }

    /**
     * @return the frequency
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * @param frequency the frequency to set
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    /**
     * @return the seasvar
     */
    public double getSeasvar() {
        return seasvar;
    }

    /**
     * @param seasvar the seasvar to set
     */
    public void setSeasvar(double seasvar) {
        this.seasvar = seasvar;
    }

    /**
     * set the greatest variance (excluding the additional noise) equal to 1
     */
    public void rescaleVariances() {
        double max = lvar;
        if (svar > max) {
            max = svar;
        }
        if (seasvar > max) {
            max = seasvar;
        }
        if (nvar > max) {
            max = nvar;
        }
        if (max == 1 || max == 0) {
            return;
        }
        lvar /= max;
        svar /= max;
        seasvar /= max;
        nvar /= max;
        nseasvar /= max;
    }
}
