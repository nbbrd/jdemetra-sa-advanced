/*
 * Copyright 2013-2014 National Bank of Belgium
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
package ec.demetra.ssf.univariate;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.State;
import ec.demetra.ssf.StateInfo;

/**
 * Ordinary Kalman filter for univariate time series
 *
 * @author Jean Palate
 */
public class OrdinaryFilter {

    public static interface Initializer {

        int initialize(State state, ISsf ssf, ISsfData data);
    }

    private final Initializer initializer;
    private State state;
    private UpdateInformation updinfo;
    private ISsfMeasurement measurement;
    private ISsfDynamics dynamics;
    private boolean missing;

    /**
     *
     * @param initializer
     */
    public OrdinaryFilter(Initializer initializer) {
        this.initializer = initializer;
    }

    public OrdinaryFilter() {
        this.initializer = null;
    }

    /**
     * Computes a(t+1|t), P(t+1|t) from a(t|t), P(t|t)
     */
    protected void pred(int t) {
        SubMatrix P = state.P().all();
        DataBlock a = state.a();
        dynamics.TX(t, a);
        dynamics.TVT(t, P);
        dynamics.addV(t, P);
    }

    protected boolean error(int t, ISsfData data) {
        missing = data.isMissing(t);
        if (missing) {
            // pe_ = null;
            updinfo.setMissing();
            return false;
        } else {
            // pe_ = new UpdateInformation(ssf_.getStateDim(), 1);
            // K = PZ'/f
            // computes (ZP)' in K'. Missing values are set to 0 
            // Z~v x r, P~r x r, K~r x v
            DataBlock C = updinfo.M();
            // computes ZPZ'; results in pe_.L
            //measurement.ZVZ(pos_, state_.P.subMatrix(), F);
            measurement.ZM(t, state.P().all(), C);
            double v = measurement.ZX(t, C);
            if (measurement.hasErrors()) {
                v += measurement.errorVariance(t);
            }
            updinfo.setVariance(v);
            // We put in K  PZ'*(ZPZ'+H)^-1 = PZ'* F^-1 = PZ'*(LL')^-1/2 = PZ'(L')^-1
            // K L' = PZ' or L K' = ZP

            double y = data.get(t);
            updinfo.set(y - measurement.ZX(t, state.a()));
            return true;
        }
    }

    protected void update() {
        if (updinfo == null) {
            return;
        }
        double e = updinfo.get();
        double v = updinfo.getVariance();
        DataBlock C = updinfo.M();

        // P = P - (M)* F^-1 *(M)' --> Symmetric
        // PZ'(LL')^-1 ZP' =PZ'L'^-1*L^-1*ZP'
        // A = a + (M)* F^-1 * v
        state.a().addAY(e / v, C);
        update(state.P(), v, C);//, state_.K.column(i));
    }

    /**
     * Retrieves the final state (which is a(N|N-1))
     *
     * @return
     */
    public State getFinalState() {
        return state;
    }

    private int initialize(ISsf ssf, ISsfData data) {
        measurement = ssf.getMeasurement();
        dynamics = ssf.getDynamics();
        updinfo = new UpdateInformation(dynamics.getStateDim());
        if (initializer == null) {
            state = State.of(dynamics);
            return state == null ? -1 : 0;
        } else {
            state = new State(dynamics.getStateDim());
            return initializer.initialize(state, ssf, data);
        }
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsf ssf, final ISsfData data, final IFilteringResults rslts) {
        // intialize the state with a(0|-1)
        int t=initialize(ssf, data);
        if (t<0)
            return false;
        int end = data.getLength();
        while (t < end) {
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Forecast);
            }
            if (error(t, data)) {
                if (rslts != null) {
                    rslts.save(t, updinfo);
                }
                update();
            } else if (rslts != null) {
                rslts.save(t, updinfo);
            }
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Concurrent);
            }
            pred(t++);
        }
        return true;
    }

    // P -= c*r
    private void update(Matrix P, double v, DataBlock C) {//, DataBlock r) {
        SymmetricMatrix.addXaXt(P, -1 / v, C);
    }

}
