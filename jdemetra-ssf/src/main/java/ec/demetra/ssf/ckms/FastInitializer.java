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
package ec.demetra.ssf.ckms;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.ISsfMeasurement;

/**
 *
 * @param <S>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FastInitializer<S extends ISsf> implements FastFilter.IFastInitializer<S> {

    /**
     * K = TPZ', L=K, F=ZPZ'+H
     *
     * @param ssf
     * @param fstate
     * @param data
     * @return
     */
    @Override
    public int initialize(final FastState fstate, final S ssf, ISsfData data) {
        if (!ssf.isTimeInvariant()) {
            return -1;
        }
        ISsfDynamics dynamics = ssf.getDynamics();
        if (dynamics.isDiffuse()) {
            return initializeDiffuse(fstate, ssf, data);
        } else {
            return initializeStationary(fstate, ssf, data);
        }
    }

    public int initializeStationary(final FastState fstate, final S ssf, ISsfData data) {
        ISsfDynamics dynamics = ssf.getDynamics();
        ISsfMeasurement measurement = ssf.getMeasurement();
        SubMatrix P0 = Matrix.square(dynamics.getStateDim()).all();
        dynamics.Pf0(P0);
        measurement.ZM(0, P0, fstate.l);
        dynamics.TX(0, fstate.l());
        fstate.l.copy(fstate.k);
        fstate.f = measurement.ZX(0, fstate.k)+ measurement.errorVariance(0);
        return 0;
    }
    
    public int initializeDiffuse(final FastState fstate, final S ssf, ISsfData data) {
        FastDiffuseInitializer initializer=new FastDiffuseInitializer();
        return initializer.initialize(fstate, ssf, data);
    }
}
