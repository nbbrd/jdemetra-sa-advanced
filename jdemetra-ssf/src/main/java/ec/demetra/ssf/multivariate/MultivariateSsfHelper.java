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
package ec.demetra.ssf.multivariate;

import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.State;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
public class MultivariateSsfHelper {

    public static MultivariateUpdateInformation next(IMultivariateSsf ssf, int t, State state, IReadDataBlock x) {
        // use ordinary filter
        ISsfDynamics dynamics = ssf.getDynamics();
        ISsfMeasurements measurements = ssf.getMeasurements();
        SubMatrix P = state.P().all();
        DataBlock a = state.a();
        // error
        if (measurements.getCount(t) != x.getLength()) {
            return null;
        }
        int dim = ssf.getStateDim();
        int[] obs = x.search(y -> Double.isFinite(y));
        int nobs = obs.length;
        MultivariateUpdateInformation updinfo;
        if (nobs == 0) {
            updinfo = new MultivariateUpdateInformation(dim, 0);
        } else {
            updinfo = new MultivariateUpdateInformation(dim, nobs);
            Matrix L = updinfo.getCholeskyFactor();
            // K = PZ'(ZPZ'+H)^-1/2
            // computes (ZP)' in K'. Missing values are set to 0 
            // Z~v x r, P~r x r, K~r x v
            SubMatrix F = L.all(), K = updinfo.getK().all();
            ZM(t, measurements, obs, state.P().all(), K.transpose());
            // computes ZPZ'; results in pe_.L
            ZM(t, measurements, obs, K, F);
            if (measurements.hasError(t)) {
                addH(t, measurements, obs, L);
            }
            SymmetricMatrix.reinforceSymmetry(L);

            // pe_L contains the Cholesky factor !!!
            SymmetricMatrix.lcholesky(L, State.ZERO);

            // We put in K  PZ'*(ZPZ'+H)^-1/2 = PZ'* F^-1 = PZ'*(LL')^-1/2 = PZ'(L')^-1
            // K L' = PZ' or L K' = ZP
            LowerTriangularMatrix.rsolve(L, K.transpose(), State.ZERO);
            DataBlock U = updinfo.getTransformedPredictionErrors();
            for (int i = 0, j = 0; i < x.getLength(); ++i) {
                double y = x.get(i);
                if (Double.isFinite(y)) {
                    U.set(j, y - measurements.ZX(t, i, state.a()));
                    ++j;
                }
            }
            // E = e*L'^-1 or E L' = e or L*E' = e'
            LowerTriangularMatrix.rsolve(L, U, State.ZERO);
            // update
            int n = updinfo.getK().getColumnsCount();
            // P = P - (M)* F^-1 *(M)' --> Symmetric
            // PZ'(LL')^-1 ZP' =PZ'L'^-1*L^-1*ZP'
            // A = a + (M)* F^-1 * v
//        for (int i = 0; i < n; ++i) {
//            state.P().addXaXt(-1, updinfo.getK().column(i));//, state_.K.column(i));
//            state.a().addAY(updinfo.getTransformedPredictionErrors().get(i), updinfo.getK().column(i));
//        }
            for (int i = 0; i < n; ++i) {
                P.addXaXt(-1, K.column(i));//, state_.K.column(i));
                state.a().addAY(U.get(i), K.column(i));
            }
        }
        // prediction
        dynamics.TX(t, a);
        dynamics.TVT(t, P);
        dynamics.addV(t, P);

        return updinfo;
    }

    /**
     * Computes zm = Z * M
     *
     * @param M
     * @param zm
     */
    public static void ZM(int t, ISsfMeasurements measurements, int[] idx, SubMatrix M, SubMatrix zm) {
        DataBlockIterator zrows = zm.rows();
        DataBlock zr = zrows.getData();
        for (int i = 0; i < idx.length; ++i) {
            measurements.ZM(t, idx[i], M, zr);
            if (!zrows.next()) {
                return;
            }

        }
    }

    public static void addH(int t, ISsfMeasurements measurements, int[] idx, Matrix P) {
        Matrix H = Matrix.square(measurements.getStateDim());
        measurements.H(t, H.all());
        for (int i = 0; i < idx.length; ++i) {
            for (int j = 0; j < i; ++j) {
                double h = H.get(idx[i], idx[j]);
                P.add(i, j, h);
                P.add(j, i, h);
            }
            P.add(i, i, H.get(idx[i], idx[i]));
        }
    }

}
