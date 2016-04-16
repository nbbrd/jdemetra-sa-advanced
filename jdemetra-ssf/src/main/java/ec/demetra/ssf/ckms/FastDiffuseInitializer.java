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

import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.SsfException;
import ec.demetra.ssf.State;
import ec.demetra.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.demetra.ssf.univariate.OrdinaryFilter;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 * Automatic initialization of diffuse time invariant models. The algorithm
 * computes the state at the end of the diffuse initialization and applies then
 * the same decomposition as in the stationary case: P(ndiffuse)-P(ndiffuse-1) =
 * - 1/f* L*L' The theoritical fundation of this approach should still be
 * developed.
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Alpha)
public class FastDiffuseInitializer<S extends ISsf> implements FastFilter.IFastInitializer<S> {

    private final OrdinaryFilter.Initializer initializer;

    public FastDiffuseInitializer() {
        initializer = null;
    }

    /**
     *
     * @param initializer
     */
    public FastDiffuseInitializer(OrdinaryFilter.Initializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public int initialize(FastState fstate, S ssf, ISsfData data) {
        State state = new State(ssf.getStateDim());
        int t=0;
        if (initializer != null) {
            t =initializer.initialize(state, ssf, data);
            if (t<0) {
                return -1;
            }
        } else {
            t=new DiffuseSquareRootInitializer().initialize(state, ssf, data);
            if (t < 0) {
                return -1;
            }
        }
        int dim = ssf.getStateDim();

        fstate.a().copy(state.a());
        ISsfDynamics dynamics = ssf.getDynamics();
        ISsfMeasurement measurement = ssf.getMeasurement();
        SubMatrix P = state.P().all();
        DataBlock k = fstate.k();
        fstate.f = measurement.ZVZ(0, P) + measurement.errorVariance(0);
        // K0 = TPZ' / var
        measurement.ZM(0, P, k);
        dynamics.TX(0, fstate.k());

        // L0: computes next iteration. TVT'-KK'*var + Q -V = - L(var)^-1 L'
        SubMatrix TVT = new Matrix(P).all();
        dynamics.TVT(0, TVT);
        dynamics.addV(0, TVT);
        TVT.sub(P);
        TVT.addXaXt(-1 / fstate.f, k);
        TVT.mul(-fstate.f);
        int imax = 0;
        double lmax = TVT.get(0, 0);
        for (int i = 1; i < dim; ++i) {
            double lcur = TVT.get(i, i);
            if (lcur > lmax) {
                imax = i;
                lmax = lcur;
            }
        }
        DataBlock l = fstate.l();
        if (lmax > 0) {
            l.copy(TVT.column(imax));
            l.mul(Math.sqrt(1 / lmax));
        } else if (!TVT.isZero(1e-6)) {
            throw new SsfException(SsfException.FASTFILTER);
        } else {
            l.set(0);
        }
        return t;
    }

}
