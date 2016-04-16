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
package ec.demetra.ssf.dk.sqrt;

import ec.demetra.ssf.StateInfo;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.dk.BaseDiffuseSmoother;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.demetra.ssf.akf.AugmentedState;
import ec.demetra.ssf.univariate.ISmoothingResults;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.OrdinarySmoother;

/**
 *
 * @author Jean Palate
 */
public class DiffuseSquareRootSmoother extends BaseDiffuseSmoother{

    private AugmentedState state;
    private IDiffuseSquareRootFilteringResults frslts;

    public boolean process(final ISsf ssf, final ISsfData data, ISmoothingResults sresults) {
        IDiffuseSquareRootFilteringResults fresults = DkToolkit.sqrtFilter(ssf, data, true);
        return process(ssf, data.getLength(), fresults, sresults);
    }

    public boolean process(ISsf ssf, final int endpos, IDiffuseSquareRootFilteringResults results, ISmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        initFilter(ssf);
        initSmoother(ssf, endpos);
        ordinarySmoothing(ssf, endpos);
        int t=frslts.getEndDiffusePosition();
        while (--t >= 0) {
            loadInfo(t);
            iterate(t);
            if (srslts != null)
            srslts.save(t, state, StateInfo.Smoothed);
        }

        return true;
    }

    private void initSmoother(ISsf ssf, int end) {
        int dim = ssf.getStateDim();
        state = new AugmentedState(dim, ssf.getDynamics().getNonStationaryDim());

        Rf = new DataBlock(dim);
        C = new DataBlock(dim);
        Ri = new DataBlock(dim);
        Ci = new DataBlock(dim);

        if (calcvar) {
            tmp0 = new DataBlock(dim);
            tmp1 = new DataBlock(dim);
            N0 = Matrix.square(dim);
            N1 = Matrix.square(dim);
            N2 = Matrix.square(dim);
        }
    }

    private void loadInfo(int pos) {
        e = frslts.error(pos);
        f = frslts.errorVariance(pos);
        fi = frslts.diffuseNorm2(pos);
        C.copy(frslts.M(pos));
        if (fi != 0) {
            Ci.copy(frslts.Mi(pos));
            Ci.mul(1 / fi);
            C.addAY(-f, Ci);
            C.mul(1 / fi);
        } else {
            C.mul(1 / f);
            Ci.set(0);
        }
        missing = !Double.isFinite(e);
        state.a().copy(frslts.a(pos));
        if (calcvar) {
            state.P().all().copy(frslts.P(pos));
            SubMatrix B = frslts.B(pos);
            state.restoreB(B);
        }
    }

    // 

    @Override
    protected void updateA(int pos) {
        DataBlock a = state.a();
        if (calcvar) {
            a.addProduct(Rf, state.P().columns());
            // Pi=B*B'
            SubMatrix B = state.B();
            DataBlock tmp = new DataBlock(state.getDiffuseDim());
            tmp.product(Ri, B.columns());
            a.addProduct(tmp, B.rows());
        } else { // to avoid unnecessary copies
            a.addProduct(Rf, frslts.P(pos).columns());
            SubMatrix B = frslts.B(pos);
            DataBlock tmp = new DataBlock(B.getColumnsCount());
            tmp.product(Ri, B.columns());
            a.addProduct(tmp, B.rows());
        }
    }

    @Override
    protected void updateP(int pos) {
        Matrix P = state.P();
        Matrix PN0P = SymmetricMatrix.quadraticForm(N0, P);
        SubMatrix B = state.B();
//        Matrix Pi=Matrix.square(B.getRowsCount());
//        SymmetricMatrix.XXt(B, Pi.subMatrix());
        Matrix BN2B = SymmetricMatrix.quadraticForm(N2.all(), B);
        Matrix PN2P=SymmetricMatrix.quadraticFormT(BN2B.all(), B);
        Matrix N1B= new Matrix(N1.getRowsCount(), B.getColumnsCount());
        N1B.all().product(N1.all(), B);
        Matrix PN1B=P.times(N1B);
        Matrix PN1Pi=Matrix.square(P.getRowsCount());
        PN1Pi.all().product(PN1B.all(), B.transpose());
//        Matrix PN2P = SymmetricMatrix.quadraticForm(N2, Pi);
//        Matrix PN1 = P.times(N1);
//        Matrix PN1Pi = PN1.times(Pi);
        P.sub(PN0P);
        P.sub(PN2P);
        P.sub(PN1Pi);
        P.all().sub(PN1Pi.all().transpose());
        SymmetricMatrix.reinforceSymmetry(P);
     }

    private void ordinarySmoothing(ISsf ssf, final int endpos) {
        OrdinarySmoother smoother = new OrdinarySmoother();
        smoother.setCalcVariances(calcvar);
        smoother.process(ssf, frslts.getEndDiffusePosition(), endpos, frslts, srslts);
        // updates R, N
        Rf.copy(smoother.getFinalR());
        if (calcvar) {
            N0.copy(smoother.getFinalN());
        }
    }

    public IDiffuseSquareRootFilteringResults getFilteringResults() {
        return frslts;
      }

}
