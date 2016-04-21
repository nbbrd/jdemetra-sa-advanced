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
package ec.demetra.ssf.implementations.arima;

import ec.tstoolkit.arima.AutoCovarianceFunction;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.RationalFunction;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.SsfException;
import ec.demetra.ssf.State;
import ec.demetra.ssf.ckms.CkmsDiffuseInitializer;
import ec.demetra.ssf.ckms.CkmsFilter;
import ec.demetra.ssf.ckms.CkmsState;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.demetra.ssf.univariate.OrdinaryFilter;
import ec.demetra.ssf.univariate.Ssf;
import ec.demetra.ssf.implementations.Measurement;
import ec.demetra.ssf.univariate.UpdateInformation;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfArima extends Ssf {

//    public static IParametricMapping<SsfArima> mapping(final SarimaSpecification spec) {
//        return new Mapping(spec);
//    }
    public static CkmsFilter.IFastInitializer<SsfArima> fastInitializer() {
        return (CkmsState state, UpdateInformation upd, SsfArima ssf, ISsfData data) -> {
            if (ssf.model.isStationary()) {
                return stInitialize(state, upd, ssf, data);
            } else {
                return dInitialize(state, upd, ssf, data);
            }
        };
    }

    private static int stInitialize(CkmsState state, UpdateInformation upd, SsfArima ssf1, ISsfData data) {
        int n = ssf1.getStateDim();
        double[] values = ssf1.model.getAutoCovarianceFunction().values(n);
        DataBlock M=upd.M(), L=state.l();
        upd.M().copyFrom(values, 0);
        L.copy(M);
        ssf1.dynamics.TX(0, L);
        upd.setVariance(values[0]);
        return 0;

    }

    private static int dInitialize(CkmsState state, UpdateInformation upd, SsfArima ssf1, ISsfData data) {
        return new CkmsDiffuseInitializer<SsfArima>(diffuseInitializer()).initialize(state, upd, ssf1, data);
    }

    public static OrdinaryFilter.Initializer diffuseInitializer() {
        return (State state, ISsf ssf, ISsfData data) -> {
            SsfArima ssfArima = (SsfArima) ssf;
            SsfArimaDynamics dyn = (SsfArimaDynamics) ssfArima.getDynamics();
            ISsfMeasurement m = ssf.getMeasurement();
            int nr = ssf.getStateDim(), nd = dyn.getNonStationaryDim();
            Matrix A = new Matrix(nr + nd, nd);
            for (int j = 0; j < nd; ++j) {
                A.set(j, j, 1);
                double[] dif = ssfArima.model.getNonStationaryAR().getCoefficients();
                for (int i = nd; i < nd + nr; ++i) {
                    double c = 0;
                    for (int k = 1; k <= nd; ++k) {
                        c -= dif[k] * A.get(i - k, j);
                    }
                    A.set(i, j, c);
                }
            }

            for (int i = 0; i < nr; ++i) {
                double c = 0;
                for (int j = 0; j < nd; ++j) {
                    c += A.get(i + nd, j) * data.get(j);
                }
                state.a().set(i, c);
            }
            Matrix stV = Matrix.square(nr);
            SsfArimaDynamics.stVar(stV.all(), dyn.stpsi_, dyn.stacgf_, dyn.var);
            Matrix K = Matrix.square(nr);
            SsfArimaDynamics.Ksi(K.all(), dyn.dif_);
            SymmetricMatrix.quadraticFormT(stV.all(), K.all(), state.P().all());
            return nd;
        };
    }

    private final IArimaModel model;

    /**
     *
     * @param arima
     */
    private SsfArima(final IArimaModel arima, final ISsfDynamics dynamics, ISsfMeasurement measurement) {
        super(dynamics, measurement);
        model = arima;
    }

    /**
     *
     * @return
     */
    public IArimaModel getModel() {
        return model;
    }

    public static SsfArima create(IArimaModel arima) {
        if (arima.isStationary()) {
            return createStationary(arima);
        } else {
            return createNonStationary(arima);
        }
    }

    private static SsfArima createStationary(IArimaModel arima) {
        double var = arima.getInnovationVariance();
        if (var == 0) {
            throw new SsfException(SsfException.STOCH);
        }
        ISsfDynamics dynamics = new StDynamics(arima);
        ISsfMeasurement measurement = Measurement.create(dynamics.getStateDim(), 0);
        return new SsfArima(arima, dynamics, measurement);
    }

    private static SsfArima createNonStationary(IArimaModel arima) {
        double var = arima.getInnovationVariance();
        if (var == 0) {
            throw new SsfException(SsfException.STOCH);
        }
        ISsfDynamics dynamics = new SsfArimaDynamics(arima);
        ISsfMeasurement measurement = Measurement.create(dynamics.getStateDim(), 0);
        return new SsfArima(arima, dynamics, measurement);
    }

    public static class StDynamics implements ISsfDynamics {

        private final int dim_;
        private final double var_;
        private final double[] phi_, acgf_, tmp_, psi_;
        private transient Matrix V;
        private transient Matrix P0;

        public StDynamics(IArimaModel arima) {
            var_ = arima.getInnovationVariance();
            Polynomial phi = arima.getAR().getPolynomial();
            Polynomial theta = arima.getMA().getPolynomial();
            double[] cphi = phi.getCoefficients();
            int p = cphi.length - 1;
            phi_ = new double[p]; // Phi in reverse order, without the first term ! 
            for (int i = 0; i < p; ++i) {
                phi_[i] = cphi[p - i];
            }
            dim_ = Math.max(p, theta.getDegree() + 1);
            psi_ = new RationalFunction(theta, phi).coefficients(dim_);
            acgf_ = arima.getAutoCovarianceFunction().values(dim_);
            tmp_ = new double[dim_];
        }

        private void init() {
            P0 = p0(var_, acgf_, psi_);
            V = v(var_, psi_);
        }

        private static Matrix v(double var, double[] psi) {
            Matrix M = Matrix.square(psi.length);
            DataBlockIterator cols = M.columns();
            DataBlock col = cols.getData();
            DataBlock W = new DataBlock(psi);
            int pos = 0;
            do {
                double c = var * psi[pos];
                if (pos > 0) {
                    col.drop(pos, 0).setAY(c, W.drop(pos, 0));
                } else {
                    col.setAY(c, W);
                }
                ++pos;
            } while (cols.next());
            SymmetricMatrix.fromLower(M);
            return M;

        }

        private static Matrix p0(double var, double[] acgf, double[] psi) {
            int dim = acgf.length;
            Matrix P = Matrix.square(dim);
            for (int j = 0; j < dim; ++j) {
                P.set(j, 0, acgf[j]);
            }
            for (int j = 0; j < dim - 1; ++j) {
                P.set(j + 1, j + 1, P.get(j, j) - psi[j] * psi[j] * var);
                for (int k = 0; k < j; ++k) {
                    P.set(j + 1, k + 1, P.get(j, k) - psi[j] * psi[k] * var);
                }
            }
            SymmetricMatrix.fromLower(P);
            return P;
        }

        /**
         *
         * @param pos
         * @param tr
         */
        @Override
        public void T(final int pos, final SubMatrix tr) {
            T(tr);
        }

        /**
         *
         * @param tr
         */
        public void T(final SubMatrix tr) {
            tr.subDiagonal(1).set(1);
            for (int i = 0, j = dim_ - phi_.length; i < phi_.length; ++i, ++j) {
                tr.set(dim_ - 1, j, -phi_[i]);
            }
        }

        /**
         *
         * @param pos
         * @param vm
         */
        @Override
        public void TVT(final int pos, final SubMatrix vm) {
            DataBlock tmp = new DataBlock(tmp_);
            tmp.set(0);
            DataBlockIterator cols = vm.columns();
            DataBlock col = cols.getData();
            cols.end();
            for (int p = phi_.length - 1; p >= 0; --p) {
                tmp.addAY(-phi_[p], col);
                cols.previous();
            }
            double tlast = -tmp.reverseDot(phi_);
            vm.shift(-1);
            tmp.bshift(DataBlock.ShiftOption.None);
            tmp_[dim_ - 1] = tlast;
            vm.column(dim_ - 1).copy(tmp);
            vm.row(dim_ - 1).copy(tmp);
        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void TX(final int pos, final DataBlock x) {
//            double[] px = x.getData();
//            int beg = x.getStartPosition(), inc = x.getIncrement(), end = x.getEndPosition();
//            double last = 0;
//            int j0 = dim_ - phi_.length;
//            if (inc == 1) {
//                for (int i = 0, j = beg + dim_ - phi_.length; i < phi_.length; ++i, ++j) {
//                    last -= phi_[i] * px[j];
//                }
//                int ilast=end-1;
//                for (int i=beg; i<ilast; ++i){
//                    px[i]=px[i+1];
//                }
//                px[ilast]=last;
//            }else{
//                for (int i = 0, j = beg + inc * (dim_ - phi_.length); i < phi_.length; ++i, j += inc) {
//                    last += phi_[i] * px[j];
//                }
//                int ilast=end-inc;
//                for (int i=beg; i!=ilast; i+=inc){
//                    px[i]=px[i+inc];
//                }
//                px[ilast]=last;
//            }

            double last = x.reverseDot(phi_);
            x.bshift(DataBlock.ShiftOption.None);
            x.set(dim_ - 1, -last);
        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void XT(final int pos, final DataBlock x) {
            double last = -x.get(dim_ - 1);
            x.fshift(DataBlock.ShiftOption.None);
            x.set(0, 0);
            if (last != 0) {
                for (int i = 0, j = dim_ - phi_.length; i < phi_.length; ++i, ++j) {
                    if (phi_[i] != 0) {
                        x.add(j, last * phi_[i]);
                    }
                }
            }
        }

        @Override
        public boolean isValid() {
            return true;
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
            return true;
        }

        @Override
        public boolean Pf0(SubMatrix pf0) {
            if (P0 == null) {
                init();
            }
            pf0.copy(P0.all());
            return true;
        }

        @Override
        public void Pi0(SubMatrix pi0) {
        }

        @Override
        public int getStateDim() {
            return dim_;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, SubMatrix qm) {
            if (V == null) {
                init();
            }
            qm.copy(V.all());
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, SubMatrix sm) {
            if (psi_ == null) {
                init();
            }
            sm.column(0).copyFrom(psi_, 0);
            if (var_ != 1) {
                sm.mul(Math.sqrt(var_));
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            if (psi_ == null) {
                init();
            }
            double a = x.dot(psi_);
            if (var_ != 1) {
                a *= Math.sqrt(var_);
            }
            xs.set(0, a);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            if (psi_ == null) {
                init();
            }
            double a = u.get(0);
            if (var_ != 1) {
                a *= Math.sqrt(var_);
            }
            x.addAY(a, new DataBlock(psi_));
        }

        @Override
        public void addV(int pos, SubMatrix p) {
            if (V == null) {
                init();
            }
            p.add(V.all());
        }
    }

    public static class SsfArimaDynamics implements ISsfDynamics {

        private final int dim_;
        private final double var, se;
        private final double[] phi_, tmp_, psi_;
        private final DataBlock Phi_;
        private final Matrix V;
        private final Matrix P0;
        private final double[] dif_;
        private final double[] stacgf_, stpsi_;

        static void B0(final SubMatrix b, final double[] d) {
            int nd = d.length - 1;
            if (nd == 0) {
                return;
            }
//            int nr = b.getRowsCount();
//            for (int i = 0; i < nd; ++i) {
//                b.set(i, i, 1);
//                for (int k = nd; k < nr; ++k) {
//                    double w = 0;
//                    for (int l = 1; l <= nd; ++l) {
//                        w -= d[l] * b.get(k - l, i);
//                    }
//                    b.set(k, i, w);
//                }
//            }
            int nr = b.getRowsCount();
            DataBlock D = new DataBlock(d, d.length - 1, 0, -1);
            b.diagonal().set(1);
            for (int i = 0; i < nd; ++i) {
                DataBlock C = b.column(i);
                DataBlock R = C.range(0, nd);
                for (int k = nd; k < nr; ++k) {
                    C.set(k, -R.dot(D));
                    R.move(1);
                }
            }
        }

        /**
         *
         * @param X
         * @param dif
         */
        static void Ksi(final SubMatrix X, final double[] dif) {
            int n = X.getRowsCount();
            double[] ksi = new RationalFunction(Polynomial.ONE, Polynomial.of(dif)).coefficients(n);

            for (int j = 0; j < n; ++j) {
                for (int k = 0; k <= j; ++k) {
                    X.set(j, k, ksi[j - k]);
                }
            }
        }

        /**
         *
         * @param stV
         * @param stpsi
         * @param stacgf
         * @param var
         */
        static void stVar(final SubMatrix stV, final double[] stpsi,
                final double[] stacgf, final double var) {
            int n = stV.getRowsCount();

            for (int j = 0; j < n; ++j) {
                stV.set(j, 0, stacgf[j]);
            }

            for (int j = 0; j < n - 1; ++j) {
                stV.set(j + 1, j + 1, stV.get(j, j) - stpsi[j] * stpsi[j] * var);
                for (int k = 0; k < j; ++k) {
                    stV
                            .set(j + 1, k + 1, stV.get(j, k) - stpsi[j] * stpsi[k]
                                    * var);
                }
            }

            SymmetricMatrix.fromLower(stV);
        }

        public SsfArimaDynamics(IArimaModel arima) {
            var = arima.getInnovationVariance();
            // BFilter ur = new BFilter(0);
            // IArimaModel stmodel = m_model.DoStationary(ur);
            BackFilter ur = arima.getNonStationaryAR();
            dif_ = ur.getCoefficients();
            Polynomial phi = arima.getAR().getPolynomial();
            phi_ = phi.getCoefficients();
            if (phi_.length == 1) {
                Phi_ = DataBlock.EMPTY;
            } else {
                Phi_ = new DataBlock(phi_, 1, phi_.length, 1);
            }
            Polynomial theta = arima.getMA().getPolynomial();
            dim_ = Math.max(phi.getDegree(), theta.getDegree() + 1);
            psi_ = new RationalFunction(theta, phi).coefficients(dim_);

            Polynomial stphi = arima.getStationaryAR().getPolynomial();
            stacgf_ = new AutoCovarianceFunction(theta, stphi, var).values(dim_);
            stpsi_ = new RationalFunction(theta, stphi).coefficients(dim_);
            tmp_ = new double[dim_];
            Matrix stvar = StDynamics.p0(var, stacgf_, stpsi_);
            Matrix K = new Matrix(dim_, dim_);
            Ksi(K.all(), dif_);
            P0 = SymmetricMatrix.quadraticFormT(stvar, K);
            V = StDynamics.v(var, psi_);
            se = Math.sqrt(var);
        }

        /**
         *
         * @param pos
         * @param tr
         */
        @Override
        public void T(final int pos, final SubMatrix tr) {
            T(tr);
        }

        /**
         *
         * @param tr
         */
        public void T(final SubMatrix tr) {
            tr.set(0);
            for (int i = 1; i < dim_; ++i) {
                tr.set(i - 1, i, 1);
            }
            for (int i = 1; i < phi_.length; ++i) {
                tr.set(dim_ - 1, dim_ - i, -phi_[i]);
            }
        }

        /**
         *
         * @param pos
         * @param vm
         */
        @Override
        public void TVT(final int pos, final SubMatrix vm) {
            DataBlock tmp = new DataBlock(tmp_);
            tmp.set(0);
            DataBlockIterator cols = vm.columns();
            DataBlock col = cols.getData();
            cols.end();
            for (int p = 1; p < phi_.length; ++p) {
                tmp.addAY(-phi_[p], col);
                cols.previous();
            }

            double tlast = -Phi_.dotReverse(tmp);

            vm.shift(-1);
            tmp.bshift(DataBlock.ShiftOption.None);
            tmp_[dim_ - 1] = tlast;
            vm.column(dim_ - 1).copy(tmp);
            vm.row(dim_ - 1).copy(tmp);
        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void TX(final int pos, final DataBlock x) {
            double last = Phi_.dotReverse(x);
            x.bshift(DataBlock.ShiftOption.None);
            x.set(dim_ - 1, -last);
        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void XT(final int pos, final DataBlock x) {
            double last = -x.get(dim_ - 1);
            x.fshift(DataBlock.ShiftOption.None);
            x.set(0, 0);
            if (last != 0) {
                for (int i = 1; i < phi_.length; ++i) {
                    if (phi_[i] != 0) {
                        x.add(dim_ - i, last * phi_[i]);
                    }
                }
            }
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isDiffuse() {
            return dif_.length > 1;
        }

        @Override
        public int getNonStationaryDim() {
            return dif_.length - 1;
        }

        @Override
        public void diffuseConstraints(SubMatrix b) {
            int d = dif_.length - 1;
            if (d == 0) {
                return;
            }
            B0(b, dif_);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(SubMatrix pf0) {
            pf0.copy(P0.all());
            return true;
        }

        @Override
        public void Pi0(SubMatrix pi0) {
            Matrix B = new Matrix(dim_, dif_.length - 1);
            B0(B.all(), dif_);
            SymmetricMatrix.XXt(B.all(), pi0);
        }

        @Override
        public int getStateDim() {
            return dim_;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, SubMatrix qm) {
            qm.copy(V.all());
        }

        @Override
        public void S(int pos, SubMatrix sm) {
            sm.column(0).copyFrom(psi_, 0);
            if (se != 1) {
                sm.mul(se);
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void addV(int pos, SubMatrix p) {
            p.add(V.all());
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock sx) {
            double a = x.dot(psi_) * se;
            sx.set(0, a);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            double a = u.get(0) * se;
            x.addAY(a, new DataBlock(psi_));
        }

    }

//    static class Mapping implements IParametricMapping<SsfArima> {
//
//        private final SarimaMapping core;
//
//        Mapping(SarimaSpecification spec) {
//            core = new SarimaMapping(spec, true);
//        }
//
//        @Override
//        public SsfArima map(IReadDataBlock p) {
//            return SsfArima.create(core.map(p)); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public IReadDataBlock map(SsfArima t) {
//            SarimaModel arima = (SarimaModel) t.model;
//            return core.map(arima);
//        }
//
//        @Override
//        public boolean checkBoundaries(IReadDataBlock inparams) {
//            return core.checkBoundaries(inparams);
//        }
//
//        @Override
//        public double epsilon(IReadDataBlock inparams, int idx) {
//            return core.epsilon(inparams, idx);
//        }
//
//        @Override
//        public int getDim() {
//            return core.getDim();
//        }
//
//        @Override
//        public double lbound(int idx) {
//            return core.lbound(idx);
//        }
//
//        @Override
//        public double ubound(int idx) {
//            return core.ubound(idx);
//        }
//
//        @Override
//        public ParamValidation validate(IDataBlock ioparams) {
//            return core.validate(ioparams);
//        }
//
//        @Override
//        public String getDescription(int idx) {
//            return core.getDescription(idx);
//        }
//    }
}
