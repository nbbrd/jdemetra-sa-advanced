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
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.univariate.IFilteringResults;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.demetra.ssf.univariate.UpdateInformation;

/**
 * Chandrasekhar recursions
 *
 * @param <F>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FastFilter<F extends ISsf> {

    public static interface IFastInitializer<F> {

        int initialize(FastState state, F ssf, ISsfData data);
    }

    private double eps = 1e-12;
    private double neps;

    private UpdateInformation pe;

    private final IFastInitializer<F> initializer;
    private ISsfMeasurement measurement;
    private ISsfDynamics dynamics;

    private ISsfData data;

    private FastState state;
    private double[] L, K;
    private int steadypos;

    /**
     *
     */
    public FastFilter() {
        initializer = new FastInitializer();
    }

    public FastFilter(IFastInitializer<F> initializer) {
        this.initializer = initializer;
    }

    /**
     * Retrieves the final state vector(which is a(N|N-1))
     *
     * @return
     */
    public FastState getFinalState() {
        return state;
    }

    public void setEpsilon(double eps) {
        this.eps = eps;
    }

    public double getEpsilon() {
        return eps;
    }

    public int getSteadyStatePosition() {
        return steadypos;
    }

    private int initialize(F ssf) {
        steadypos = -1;
        dynamics = ssf.getDynamics();
        measurement = ssf.getMeasurement();
        state = new FastState(dynamics.getStateDim());
        
        int t=initializer.initialize(state, ssf, data);
        if (t <0) {
            return -1;
        }
        pe = new UpdateInformation(dynamics.getStateDim());
        K = state.k.getData();
        L = state.l.getData();
        neps = eps * state.f;
        return t;
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final F ssf, final ISsfData data,
            final IFilteringResults rslts) {
        this.data = data;
        int t=initialize(ssf);
        if (t<0){
            return false;
        }
        int end = this.data.getLength();
        while (t < end) {
            error(t);
            if (rslts != null) {
                rslts.save(t, pe);
            }
            update();
            next(t++);
            //
            ;
        }
        return true;
    }

    private void next(int t) {
        if (steadypos >= 0 ) {
            return;
        }
        // K(i+1) = K(i) - T L(i) * (Z*L(i))/V(i)
        // L(i+1) = T L(i) - K(i) * (Z*L(i))/V(i)
        // F(i+1) = F(i) - (Z*L(i))^2/V(i)

        // ZLi, V(i+1)
        double zl = measurement.ZX(0, state.l);

        // TL(i)
        dynamics.TX(0, state.l);

        if (Math.abs(zl) > neps) {
            // C, L
            double zlv = zl / state.f;
            state.f -= zl * zlv;
            for (int i = 0; i < L.length; ++i) {
                double tl = L[i];
                L[i] -= K[i] * zlv;
                K[i] -= tl * zlv;
            }
        } else if (state.l.nrm2() < eps) {
            steadypos = t;
        }

    }

    private void update() {
        dynamics.TX(0, state.a);
        state.a.addAY(pe.get() / pe.getVariance(), state.k);
    }

    private void error(int t) {
        double y = data.get(t);
        pe.set(y - measurement.ZX(0, state.a));
        pe.setVariance(state.f);

    }
}
