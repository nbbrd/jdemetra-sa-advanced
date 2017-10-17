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
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 *
 * @author Jean Palate
 */
public class Var extends MultivariateSsf {

    public static Var of(VarDescriptor desc) {
        VarDynamics dynamics = VarDynamics.of(desc, null);
        ISsfMeasurements measurements = new Measurements(desc.getVariablesCount(), desc.getLagsCount());
        return new Var(dynamics, measurements);
    }

    public static Var of(VarDescriptor desc, int nlags) {
        int nl = Math.max(nlags, desc.getLagsCount());

        VarDynamics dynamics = VarDynamics.of(desc, nl, null);
        ISsfMeasurements measurements = new Measurements(desc.getVariablesCount(), nl);
        return new Var(dynamics, measurements);
    }

    private Var(VarDynamics dynamics, ISsfMeasurements measurements) {
        super(dynamics, measurements);
    }

    private static class Measurements implements ISsfMeasurements {

        private final int nv, nl;

        private Measurements(int nv, int nl) {
            this.nv = nv;
            this.nl = nl;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public int getCount(int pos) {
            return nv;
        }

        @Override
        public int getMaxCount() {
            return nv;
        }

        @Override
        public boolean isHomogeneous() {
            return true;
        }

        @Override
        public void Z(int pos, int var, DataBlock z) {
            z.set(var, 1);
        }

        @Override
        public boolean hasErrors() {
            return false;
        }

        @Override
        public boolean hasIndependentErrors() {
            return true;
        }

        @Override
        public boolean hasError(int pos) {
            return false;
        }

        @Override
        public void H(int pos, SubMatrix h) {
        }

        @Override
        public void R(int pos, SubMatrix r) {
        }

        @Override
        public double ZX(int pos, int var, DataBlock m) {
            return m.get(var);
        }

        @Override
        public double ZVZ(int pos, int ivar, int jvar, SubMatrix V) {
            if (ivar != jvar) {
                return 0;
            } else {
                return V.get(ivar, ivar);
            }
        }

        @Override
        public void addH(int pos, SubMatrix V) {
        }

        @Override
        public void VpZdZ(int pos, int ivar, int jvar, SubMatrix V, double d) {
            if (ivar != jvar) {
                return;
            } else {
                V.add(ivar, ivar, d);
            }
        }

        @Override
        public void XpZd(int pos, int var, DataBlock x, double d) {
            x.add(var, d);
        }

        @Override
        public int getStateDim() {
            return nl * nv;
        }

        @Override
        public boolean isValid() {
            return nv > 0;
        }

    }
}
