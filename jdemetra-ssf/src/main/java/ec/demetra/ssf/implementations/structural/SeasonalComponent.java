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
package ec.demetra.ssf.implementations.structural;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.implementations.Measurement;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.Ssf;
import ec.tstoolkit.maths.matrices.Householder;

/**
 *
 * @author Jean Palate
 */
public class SeasonalComponent {

    private static Matrix tsvar(int freq) {
        int n = freq - 1;
        Matrix M = new Matrix(n, freq);
        M.diagonal().set(1);
        M.column(n).set(-1);
        Matrix O = SymmetricMatrix.XXt(M);
        Householder qr = new Householder(false);
        qr.decompose(O);
        Matrix Q = qr.solve(M);
        Matrix H = new Matrix(freq, n);
        // should be improved
        for (int i = 0; i < freq; ++i) {
            double z = 2 * Math.PI * (i + 1) / freq;
            for (int j = 0; j < n / 2; ++j) {
                H.set(i, 2 * j, Math.cos((j + 1) * z));
                H.set(i, 2 * j + 1, Math.sin((j + 1) * z));
            }
            if (n % 2 == 1) {
                H.set(i, n - 1, Math.cos((freq / 2) * z));
            }
        }
        Matrix QH = Q.times(H);
        Matrix Z = SymmetricMatrix.XXt(QH);
        Z.smooth(1e-12);

        return Z;
    }

    /**
     *
     * @param freq
     * @return
     */
    private static synchronized Matrix tsVar(int freq) {
        switch (freq) {
            case 12:
                if (VTS12 == null) {
                    VTS12 = tsvar(12);
                }
                return VTS12.clone();
            case 4:
                if (VTS4 == null) {
                    VTS4 = tsvar(4);
                }
                return VTS4.clone();
            case 2:
                if (VTS2 == null) {
                    VTS2 = tsvar(2);
                }
                return VTS2.clone();
            case 3:
                if (VTS3 == null) {
                    VTS3 = tsvar(3);
                }
                return VTS3.clone();
            case 6:
                if (VTS6 == null) {
                    VTS6 = tsvar(6);
                }
                return VTS6.clone();
            default:
                return tsvar(freq);
        }
    }

    private static Matrix hsvar(int freq) {
        Matrix m = Matrix.square(freq - 1);
        m.set(-1.0 / freq);
        m.diagonal().add(1);
        return m;
    }

    private static synchronized Matrix hslVar(int freq) {
        switch (freq) {
            case 12:
                if (LHS12 == null) {
                    LHS12 = hsvar(12);
                    SymmetricMatrix.lcholesky(LHS12);
                }
                return LHS12.clone();
            case 4:
                if (LHS4 == null) {
                    LHS4 = hsvar(4);
                    SymmetricMatrix.lcholesky(LHS4);
                }
                return LHS4.clone();
            case 2:
                if (LHS2 == null) {
                    LHS2 = hsvar(2);
                    SymmetricMatrix.lcholesky(LHS2);
                }
                return LHS2.clone();
            case 3:
                if (LHS3 == null) {
                    LHS3 = hsvar(3);
                    SymmetricMatrix.lcholesky(LHS3);
                }
                return LHS3.clone();
            case 6:
                if (LHS6 == null) {
                    LHS6 = hsvar(6);
                    SymmetricMatrix.lcholesky(LHS6);
                }
                return LHS6.clone();
            default:
                Matrix lhs = hsvar(freq);
                SymmetricMatrix.lcholesky(lhs);
                return lhs;
        }
    }

    public static Matrix tsVar(SeasonalModel seasModel, final int freq) {
        if (seasModel == SeasonalModel.Trigonometric) {
            return tsVar(freq);
        } else {
            int n = freq - 1;
            Matrix Q = Matrix.square(n);
            if (null != seasModel) // Dummy
            {
                switch (seasModel) {
                    case Dummy:
                        Q.set(n - 1, n - 1, 1);
                        break;
                    case Crude:
                        Q.set(1);
                        //Q.set(0, 0, freq * var);
                        break;
                    case HarrisonStevens:
                        double v = 1.0 / freq;
                        Q.set(-v);
                        Q.diagonal().add(1);
                        break;
                    default:
                        break;
                }
            }
            return Q;
        }
    }

    private static synchronized Matrix tslVar(int freq) {
        switch (freq) {
            case 12:
                if (LVTS12 == null) {
                    LVTS12 = tsvar(12);
                    SymmetricMatrix.lcholesky(LVTS12);
                    LVTS12.smooth(1e-12);
                }
                return LVTS12.clone();
            case 4:
                if (LVTS4 == null) {
                    LVTS4 = tsvar(4);
                    LVTS4.smooth(1e-12);
                    SymmetricMatrix.lcholesky(LVTS4);
                }
                return LVTS4.clone();
            case 2:
                if (LVTS2 == null) {
                    LVTS2 = tsvar(2);
                    LVTS2.smooth(1e-12);
                    SymmetricMatrix.lcholesky(LVTS2);
                }
                return LVTS2.clone();
            case 3:
                if (LVTS3 == null) {
                    LVTS3 = tsvar(3);
                    SymmetricMatrix.lcholesky(LVTS3);
                    LVTS3.smooth(1e-12);
                }
                return LVTS3.clone();
            case 6:
                if (LVTS6 == null) {
                    LVTS6 = tsvar(6);
                    SymmetricMatrix.lcholesky(LVTS6);
                    LVTS6.smooth(1e-12);
                }
                return LVTS6.clone();
            default:
                Matrix var = tsvar(freq);
                SymmetricMatrix.lcholesky(var);
                var.smooth(1e-12);
                return var;
        }
    }

    public static Matrix tslVar(SeasonalModel seasModel, final int freq) {
        if (seasModel == SeasonalModel.Trigonometric) {
            return tslVar(freq);
        } else if (seasModel == SeasonalModel.HarrisonStevens) {
            return hslVar(freq);
        } else {
            int n = freq - 1;
            Matrix Q = Matrix.square(n);
            if (null != seasModel) // Dummy
            {
                switch (seasModel) {
                    case Dummy:
                        Q.set(n - 1, n - 1, 1);
                        break;
                    case Crude:
                        Q.set(1);
                        //Q.set(0, 0, freq * var);
                        break;
                    default:
                        break;
                }
            }
            return Q;
        }
    }

    private static Matrix VTS2, VTS3, VTS4, VTS6, VTS12;
    private static Matrix LVTS2, LVTS3, LVTS4, LVTS6, LVTS12, LHS2, LHS3, LHS4, LHS6, LHS12;

    public static ISsf create(final SeasonalModel model, final double seasVar, final int period) {
        return new Ssf(new Dynamics(model, seasVar, period), Measurement.create(period - 1, 1));
    }

    public static ISsf harrisonStevens(final int period, final double v) {
        return new Ssf(new HarrisonStevensDynamics(period, v), Measurement.cyclical(period));
    }

    public static ISsf harrisonStevens(final double[] var) {
        return new Ssf(new HarrisonStevensDynamics(var), Measurement.cyclical(var.length));
    }

    public static class Dynamics implements ISsfDynamics {

        private final SeasonalModel seasModel;
        private final double seasVar;
        private final int freq;
        private final SubMatrix tsvar, lvar;

        public Dynamics(final SeasonalModel model, final double seasVar, final int freq) {
            this.seasVar = seasVar;
            this.seasModel = model;
            this.freq = freq;
            if (seasVar > 0) {
                tsvar = tsVar(seasModel, freq).all();
                tsvar.mul(seasVar);
                if (model != SeasonalModel.Crude && model != SeasonalModel.Dummy) {
                    lvar = tslVar(seasModel, freq).all();
                    lvar.mul(std());
                } else {
                    lvar = null;
                }
            } else {
                tsvar = null;
                lvar = null;
            }
        }

        private double std() {
            if (seasVar == 0 || seasVar == 1) {
                return seasVar;
            } else {
                return Math.sqrt(seasVar);
            }
        }

        @Override
        public int getStateDim() {
            return freq - 1;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            if (seasVar > 0) {
                if (seasModel == SeasonalModel.Dummy || seasModel == SeasonalModel.Crude) {
                    return 1;
                } else {
                    return freq - 1;
                }
            } else {
                return 0;
            }
        }

        @Override
        public void V(int pos, SubMatrix v) {
            if (seasVar > 0) {
                if (seasModel == SeasonalModel.Dummy) {
                    v.set(0, 0, seasVar);
                } else {
                    v.copy(tsvar);
                }
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, SubMatrix s) {
            if (seasModel == SeasonalModel.Crude) {
                s.set(std());
            } else if (seasModel == SeasonalModel.Dummy) {
                s.set(freq - 1, 0, std());
            } else {
                s.copy(lvar);
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            if (seasModel == SeasonalModel.Crude) {
                x.add(std() * u.get(0));
            } else if (seasModel == SeasonalModel.Dummy) {
                x.add(freq - 1, std() * u.get(0));
            } else {
                x.addProduct(lvar.rows(), u);
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            if (seasModel == SeasonalModel.Crude) {
                xs.set(0, std() * x.sum());
            } else if (seasModel == SeasonalModel.Dummy) {
                xs.set(0, std() * x.get(freq - 1));
            } else {
                xs.product(x, lvar.columns());
            }
        }

//        @Override
//        public void addSX(int pos, DataBlock x, DataBlock y) {
//            y.add(x);
//        }
//
        @Override
        public void T(int pos, SubMatrix tr) {
            if (seasVar >= 0) {
                tr.row(freq - 2).set(-1);
                tr.subDiagonal(1).set(1);
            }
        }

        @Override
        public boolean isDiffuse() {
            return seasVar >= 0;
        }

        @Override
        public int getNonStationaryDim() {
            return freq - 1;
        }

        @Override
        public void diffuseConstraints(SubMatrix b) {
            b.diagonal().set(1);
        }

        @Override
        public void Pi0(SubMatrix p) {
            p.diagonal().set(1);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(SubMatrix p) {
            return true;
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.bshift(DataBlock.ShiftOption.NegSum);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            int imax = freq - 2;
            double xs = x.get(imax);
            for (int i = imax; i > 0; --i) {
                x.set(i, x.get(i - 1) - xs);
            }
            x.set(0, -xs);

        }

        @Override
        public void addV(int pos, SubMatrix p) {
            if (seasModel == SeasonalModel.Dummy) {
                p.add(0, 0, seasVar);
            } else {
                p.add(tsvar);
            }
        }

    }

    public static class HarrisonStevensDynamics implements ISsfDynamics {

        private final int period;
        private final double[] var;
        private final Matrix V;

        public HarrisonStevensDynamics(final int period, final double v) {
            this.period = period;
            var = null;
            V = Matrix.square(period - 1);
            V.set(-1.0 / period);
            V.diagonal().add(1);
            V.mul(v);
        }

        public HarrisonStevensDynamics(final double[] var) {
            period = var.length;
            this.var = var.clone();
            Matrix C = new Matrix(period - 1, period);
            C.set(-1.0 / period);
            C.diagonal().add(1);
            Matrix D = Matrix.diagonal(var);
            V = SymmetricMatrix.quadraticFormT(D, C);
        }

        public double[] getVariances() {
            return var;
        }

        @Override
        public int getStateDim() {
            return period - 1;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean isValid() {
            return period > 1;
        }

        @Override
        public int getInnovationsDim() {
            return period - 1;
        }

        @Override
        public void V(int pos, SubMatrix qm) {
            qm.copy(V.all());
        }

//        @Override
//        public boolean hasS() {
//            return false;
//        }
//
        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

//        @Override
//        public void Q(int pos, SubMatrix qm) {
//            qm.copy(V.subMatrix());
//        }
//
//        @Override
//        public void S(int pos, SubMatrix sm) {
//        }
//
        @Override
        public void S(int pos, SubMatrix s) {
            //TODO
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            //TODO
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            //TODO
        }
//        @Override
//        public void addSX(int pos, DataBlock x, DataBlock y) {
//            y.add(x);
//        }
//

        @Override
        public void T(int pos, SubMatrix tr) {
            tr.diagonal().set(1);
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getNonStationaryDim() {
            return period - 1;
        }

        @Override
        public void diffuseConstraints(SubMatrix b) {
            b.diagonal().set(1);
        }

        @Override
        public void Pi0(SubMatrix b) {
            b.diagonal().set(1);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(SubMatrix pf0) {
            return true;
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void addV(int pos, SubMatrix p) {
            p.add(V.all());
        }

    }
}
