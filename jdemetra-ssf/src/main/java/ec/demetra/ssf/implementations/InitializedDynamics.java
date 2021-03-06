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
package ec.demetra.ssf.implementations;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.State;

/**
 *
 * @author Jean Palate
 */
public final class InitializedDynamics implements ISsfDynamics {

    private final ISsfDynamics dyn;
    private final State start;
    private final int startpos;

    public InitializedDynamics(ISsfDynamics cur, State start, int startpos) {
        this.dyn = cur;
        this.start = start;
        this.startpos = startpos;
    }

    @Override
    public int getStateDim() {
        return dyn.getStateDim();
    }

    @Override
    public boolean isTimeInvariant() {
        return dyn.isTimeInvariant();
    }

    @Override
    public boolean isValid() {
        return dyn.isValid();
    }

    @Override
    public int getInnovationsDim() {
        return dyn.getInnovationsDim();
    }

    @Override
    public void V(int pos, SubMatrix qm) {
        dyn.V(pos + startpos, qm);
    }

    @Override
    public boolean hasInnovations(int pos) {
        return dyn.hasInnovations(pos + startpos);
    }

    @Override
    public void S(int pos, SubMatrix sm) {
        dyn.S(pos + startpos, sm);
    }

    @Override
    public void XS(int pos, DataBlock x, DataBlock sx) {
        dyn.XS(pos+ startpos, x, sx);
    }

    @Override
    public void addSU(int pos, DataBlock x, DataBlock u) {
        dyn.addSU(pos+ startpos, x, u);
    }

    @Override
    public void T(int pos, SubMatrix tr) {
        dyn.T(pos + startpos, tr);
    }

    @Override
    public boolean isDiffuse() {
        return false;
    }

    @Override
    public int getNonStationaryDim() {
        return 0;
    }

    @Override
    public void diffuseConstraints(SubMatrix b) {
    }

    @Override
    public boolean a0(DataBlock a0) {
        a0.copy(start.a());
        return true;
    }

    @Override
    public boolean Pf0(SubMatrix pf0) {
        pf0.copy(start.P().all());
        return true;
    }

    @Override
    public void TX(int pos, DataBlock x) {
        dyn.TX(pos + startpos, x);
    }

    @Override
    public void XT(int pos, DataBlock x) {
        dyn.XT(pos + startpos, x);
    }

    @Override
    public void addV(int pos, SubMatrix p) {
        dyn.addV(pos + startpos, p);
    }
}
