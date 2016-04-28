/*
 * Copyright 2016-2017 National Bank of Belgium
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
package ec.demetra.ssf.implementations.var;

import ec.demetra.ssf.implementations.Measurements;
import ec.demetra.ssf.multivariate.ISsfMeasurements;
import ec.demetra.ssf.multivariate.MultivariateSsf;

/**
 *
 * @author Jean Palate
 */
public class Var extends MultivariateSsf {

    public static Var of(VarDescriptor desc) {
        int size = desc.getVariablesCount() * desc.getLagsCount();
        
        VarDynamics dynamics = VarDynamics.of(desc, desc.getInnovationsVariance());
        int[] mpos = new int[desc.getVariablesCount()];
        for (int i = 0; i < mpos.length; ++i) {
            mpos[i] = i * desc.getLagsCount();
        }
        ISsfMeasurements measurements = Measurements.create(size, mpos);
        return new Var(dynamics, measurements);
    }

    private Var(VarDynamics dynamics, ISsfMeasurements measurements) {
        super(dynamics, measurements);
    }

}
