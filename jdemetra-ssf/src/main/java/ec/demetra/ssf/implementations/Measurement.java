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

import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.demetra.ssf.multivariate.ISsfMeasurements;

/**
 *
 * @author Jean Palate
 */
public class Measurement {

    public static ISsfMeasurement create(final int dim, final int mpos, final double var) {
        return new Measurement1(dim, mpos, var);
    }

    public static ISsfMeasurement create(final int dim, final int mpos) {
        return new Measurement1(dim, mpos, 0);
    }

    public static ISsfMeasurement createSum(final int n, final double var) {
        return new SumMeasurement(n, var);
    }

    public static ISsfMeasurement createSum(final int n) {
        return new SumMeasurement(n, 0);
    }

    public static ISsfMeasurement create(final int dim, final int[] mpos) {
        return new Measurement2(dim, mpos, 0);
    }

    public static ISsfMeasurement create(final int dim, final int[] mpos, final double var) {
        return new Measurement2(dim, mpos, var);
    }

    public static ISsfMeasurement proxy(ISsfMeasurements m) {
        if (!m.isHomogeneous() || m.getCount(0) != 1) {
            return null;
        }
        return new Proxy(m);
    }

    public static ISsfMeasurement cyclical(final int period) {
        return new CyclicalMeasurement(period);
    }

    private static class Proxy implements ISsfMeasurement {

        private final ISsfMeasurements m_;
        private final transient SubMatrix h_;

        private Proxy(ISsfMeasurements m) {
            m_ = m;
            if (m.hasErrors()) {
                h_ = new Matrix(1, 1).all();
            } else {
                h_ = null;
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return m_.isTimeInvariant();
        }

        @Override
        public void Z(int pos, DataBlock z) {
            m_.Z(pos, 0, z); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean hasErrors() {
            return h_ != null;
        }

        @Override
        public boolean hasError(int pos) {
            return m_.hasError(pos);
        }

        @Override
        public double errorVariance(int pos) {
            if (h_ == null || !m_.hasError(pos)) {
                return 0;
            }
            m_.H(pos, h_);
            return h_.get(0, 0);
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return m_.ZX(pos, 0, m);
        }

        @Override
        public double ZVZ(int pos, SubMatrix V) {
            return m_.ZVZ(pos, 0, 0, V);
        }

        @Override
        public void VpZdZ(int pos, SubMatrix V, double d) {
            m_.VpZdZ(pos, 0, 0, V, d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            m_.XpZd(pos, 0, x, d);
        }

        @Override
        public int getStateDim() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isValid() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private static class SumMeasurement implements ISsfMeasurement {

        private final int sdim;
        private final double var;

        SumMeasurement(int n, double var) {
            if (n < 2) {
                throw new java.lang.IllegalArgumentException("Sum measurement");
            }
            this.sdim = n;
            this.var = var;
        }

        @Override
        public double errorVariance(int pos) {
            return var;
        }

        @Override
        public boolean hasErrors() {
            return var != 0;
        }

        @Override
        public boolean hasError(int pos) {
            return var != 0;
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return m.sum();
        }

        @Override
        public void ZM(int pos, SubMatrix m, DataBlock zm) {
            zm.sum(m.row(0), m.row(1));
            for (int r = 2; r < m.getRowsCount(); ++r) {
                zm.add(m.row(r));
            }
        }

        @Override
        public double ZVZ(int pos, SubMatrix V) {
            return V.sum();
        }

        @Override
        public void VpZdZ(int pos, SubMatrix V, double d) {
            V.add(d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.add(d);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.set(1);
        }

        @Override
        public int getStateDim() {
            return sdim;
        }

        @Override
        public boolean isValid() {
            return sdim > 0;
        }

    }

    private static class Measurement1 implements ISsfMeasurement {

        private final int n, mpos;
        private final double var;

        Measurement1(int n, int mpos, double var) {
            this.n = n;
            this.mpos = mpos;
            this.var = var;
        }

        @Override
        public double errorVariance(int pos) {
            return var;
        }

        @Override
        public boolean hasErrors() {
            return var != 0;
        }

        @Override
        public boolean hasError(int pos) {
            return var != 0;
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return m.get(mpos);
        }

        @Override
        public void ZM(int pos, SubMatrix m, DataBlock zm) {
            zm.copy(m.row(mpos));
        }

        @Override
        public double ZVZ(int pos, SubMatrix V) {
            return V.get(mpos, mpos);
        }

        @Override
        public void VpZdZ(int pos, SubMatrix V, double d) {
            V.add(mpos, mpos, d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.add(mpos, d);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.set(mpos, 1);
        }

        @Override
        public int getStateDim() {
            return n;
        }

        @Override
        public boolean isValid() {
            return mpos < n;
        }

    }

    private static class Measurement2 implements ISsfMeasurement {

        private final int sdim;
        private final int[] mpos;
        private final double var;

        Measurement2(int n, int[] mpos, double var) {
            this.sdim = n;
            this.mpos = mpos;
            this.var = var;
        }

        @Override
        public double errorVariance(int pos) {
            return var;
        }

        @Override
        public boolean hasErrors() {
            return var != 0;
        }

        @Override
        public boolean hasError(int pos) {
            return var != 0;
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            int n = mpos.length;
            double d = m.get(mpos[0]);
            for (int i = 1; i < n; ++i) {
                d += m.get(mpos[i]);
            }
            return d;
        }

        @Override
        public void ZM(int pos, SubMatrix m, DataBlock zm) {
            zm.copy(m.row(mpos[0]));
            for (int i = 1; i < mpos.length; ++i) {
                zm.add(m.row(mpos[i]));
            }
        }

        @Override
        public double ZVZ(int pos, SubMatrix V) {
            double d = 0;
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                d += V.get(mpos[i], mpos[i]);
                for (int j = 0; j < i; ++j) {
                    d += 2 * V.get(mpos[i], mpos[j]);
                }
            }
            return d;
        }

        @Override
        public void VpZdZ(int pos, SubMatrix V, double d) {
            if (d == 0) {
                return;
            }
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    V.add(mpos[i], mpos[j], d);
                }
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            if (d == 0) {
                return;
            }
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                x.add(mpos[i], d);
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                z.set(mpos[i], 1);
            }
        }

        @Override
        public int getStateDim() {
            return sdim;
        }

        @Override
        public boolean isValid() {
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                if (mpos[i] >= sdim) {
                    return false;
                }
            }
            return true;
        }

    }

    static class CyclicalMeasurement implements ISsfMeasurement {

        private final int period;

        public CyclicalMeasurement(int period) {
            this.period = period;
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            int spos = pos % period;
            if (spos == period - 1) {
                z.set(-1);
            } else {
                z.set(spos, 1);
            }
        }

        @Override
        public boolean hasErrors() {
            return false;
        }

        @Override
        public boolean hasError(int pos) {
            return false;
        }

        @Override
        public double errorVariance(int pos) {
            return 0;
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            int spos = pos % period;
            if (spos == period - 1) {
                return -x.sum();
            } else {
                return x.get(spos);
            }
        }

        @Override
        public void ZM(int pos, SubMatrix m, DataBlock x) {
            int spos = pos % period;
            if (spos == period - 1) {
                for (int i = 0; i < x.getLength(); ++i) {
                    x.set(i, -m.column(i).sum());
                }
            } else {
                x.copy(m.row(spos));
            }
        }

        @Override
        public double ZVZ(int pos, SubMatrix vm) {
            int spos = pos % period;
            if (spos == period - 1) {
                return vm.sum();
            } else {
                return vm.get(spos, spos);
            }
        }

        @Override
        public void VpZdZ(int pos, SubMatrix vm, double d) {
            if (d == 0) {
                return;
            }
            int spos = pos % period;
            if (spos == period - 1) {
                vm.add(d);
            } else {
                vm.add(spos, spos, d);
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            if (d == 0) {
                return;
            }
            int spos = pos % period;
            if (spos == period - 1) {
                x.add(-d);
            } else {
                x.add(spos, d);
            }
        }

        @Override
        public int getStateDim() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isValid() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
