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

import ec.demetra.ssf.implementations.structural.BasicStructuralModel;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class SSHSModel {

    private BasicStructuralModel bsm;
    private int[] np;
    private double svar;

    public void setNoisyPeriods(int[] periods) {
        np = periods.clone();
    }

    public int[] getNoisyPeriods() {
        return np;
    }

    public BasicStructuralModel getBasicStructuralModel() {
        return bsm;
    }

    /**
     * Copy the parameters of a given airline model. The model must be an
     * airline model (not checked by the current implementation !).
     * @param model
     */
    public void setBasicStructuralMode(BasicStructuralModel model) {
        bsm=model;
    }

    public double getNoisyPeriodsVariance() {
        return svar;
    }

    public void setNoisyPeriodsVariance(double svar) {
        this.svar = svar;
    }

    @Override
    public String toString() {
        if (np.length == 0) {
            return "bsm";
        }
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        builder.append(np[0] + 1);
        for (int i = 1; i < np.length; ++i) {
            builder.append("  ").append(np[i] + 1);
        }
        builder.append(']');
        return builder.toString();
    }
}
