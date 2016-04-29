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
package be.nbb.demetra.bvar.ssf;

import ec.demetra.ssf.implementations.var.VarDescriptor;
import ec.demetra.ssf.implementations.var.VarDynamics;
import ec.demetra.ssf.multivariate.ISsfMeasurements;
import ec.demetra.ssf.multivariate.MultivariateSsf;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 *
 * @author Jean Palate
 */
public class MixedFrequencySsf extends MultivariateSsf {

    /**
     *
     * @param desc Description of the dynamics
     * @param c Aggregation rule for the different variables if c[i]=n, the
     * measurement equation for the variable[i] is 0 ... 0 1 ... 1 0 ... 0 (with
     * n x 1) The ssf can be extended to deal with aggregations demanding more
     * lags than what is specified in the descriptor.
     * @return
     */
    public static MixedFrequencySsf of(VarDescriptor desc, int[] c) {

        if (c.length != desc.getVariablesCount()) {
            return null;
        }
        int cmax = 0;
        for (int i = 0; i < c.length; ++i) {
            if (c[i] > cmax) {
                cmax = c[i];
            }
        }
        int nl = Math.max(cmax, desc.getLagsCount());
        int size = desc.getVariablesCount() * desc.getLagsCount();

        VarDynamics dynamics = VarDynamics.of(desc, nl, null);
        return new MixedFrequencySsf(dynamics, new Measurements(c, nl));
    }

    private MixedFrequencySsf(VarDynamics dynamics, ISsfMeasurements measurements) {
        super(dynamics, measurements);
    }

    private static class Measurements implements ISsfMeasurements {

        private final int[] c;
        private final int nl;

        private Measurements(int[] c, int nl) {
            this.c = c;
            this.nl = nl;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public int getCount(int pos) {
            return c.length;
        }

        @Override
        public int getMaxCount() {
            return c.length;
        }

        @Override
        public boolean isHomogeneous() {
            return true;
        }

        @Override
        public void Z(int pos, int var, DataBlock z) {
            int n = c[var];
            if (n == 1) {
                z.set(var, 1);
            } else {
                z.extract(var, n, c.length).set(1);
            }
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
            int n = c[var];
            if (n == 1) {
                return m.get(var);
            } else {
                return m.extract(var, n, c.length).sum();
            }
        }

        @Override
        public double ZVZ(int pos, int ivar, int jvar, SubMatrix V) {
            if (ivar != jvar) {
                return 0;
            }
            int n = c[ivar];
            if (n == 1) {
                return V.get(ivar, ivar);
            } else {
                return V.extract(ivar, ivar, n, n, c.length, c.length).sum();
            }
        }

        @Override
        public void addH(int pos, SubMatrix V) {
        }

        @Override
        public void VpZdZ(int pos, int ivar, int jvar, SubMatrix V, double d) {
            if (ivar != jvar) {
                return;
            }
            int n = c[ivar];
            if (n == 1) {
                V.add(ivar, ivar, d);
            } else {
                V.extract(ivar, ivar, n, n, c.length, c.length).add(d);
            }
        }

        @Override
        public void XpZd(int pos, int var, DataBlock x, double d) {
            int n = c[var];
            if (n == 1) {
                x.add(var, d);
            } else {
                x.extract(var, n, c.length).add(d);
            }
        }

        @Override
        public int getStateDim() {
            return nl * c.length;
        }

        @Override
        public boolean isValid() {
            return c != null;
        }

    }

}
