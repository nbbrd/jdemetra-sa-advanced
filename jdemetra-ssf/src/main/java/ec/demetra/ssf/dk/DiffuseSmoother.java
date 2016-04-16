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
package ec.demetra.ssf.dk;

import ec.demetra.ssf.StateInfo;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.demetra.ssf.univariate.ISmoothingResults;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.OrdinarySmoother;

/**
 *
 * @author Jean Palate
 */
public class DiffuseSmoother extends BaseDiffuseSmoother{

    private DiffuseState state;
    private IDiffuseFilteringResults frslts;

    public boolean process(final ISsf ssf, final ISsfData data, ISmoothingResults sresults) {
        IDiffuseFilteringResults fresults = DkToolkit.filter(ssf, data, true);
        return process(ssf, data.getLength(), fresults, sresults);
    }

    public boolean process(ISsf ssf, final int endpos, IDiffuseFilteringResults results, ISmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        initFilter(ssf);
        initSmoother(ssf, endpos);
        ordinarySmoothing(ssf, endpos);
        int t=frslts.getEndDiffusePosition();
        while (--t >= 0) {
            loadInfo(t);
            iterate(t);
            if (hasinfo) {
                srslts.save(t, state, StateInfo.Smoothed);
            }
        }
        return true;
    }

    private void initSmoother(ISsf ssf, int endpos) {
        int dim = ssf.getStateDim();
        state = new DiffuseState(dim);
 
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
        DataBlock fa = frslts.a(pos);
        hasinfo = fa != null;
        if (!hasinfo) {
            return;
        }
        state.a().copy(fa);
        if (calcvar) {
            state.P().all().copy(frslts.P(pos));
            state.Pi().all().copy(frslts.Pi(pos));
        }
    }


    @Override
    protected void updateA(int pos) {
        DataBlock a = state.a();
        if (calcvar) {
            a.addProduct(Rf, state.P().columns());
            a.addProduct(Ri, state.Pi().columns());
        } else { // to avoid unnecessary copies
            a.addProduct(Rf, frslts.P(pos).columns());
            a.addProduct(Ri, frslts.Pi(pos).columns());
        }
    }

    @Override
    protected void updateP(int pos) {
        Matrix P = state.P();
        Matrix PN0P = SymmetricMatrix.quadraticForm(N0, P);
        Matrix Pi = state.Pi();
        Matrix PN2P = SymmetricMatrix.quadraticForm(N2, Pi);
        Matrix PN1 = P.times(N1);
        Matrix PN1Pi = PN1.times(Pi);
        P.sub(PN0P);
        P.sub(PN2P);
        P.sub(PN1Pi);
        P.all().sub(PN1Pi.all().transpose());
        SymmetricMatrix.reinforceSymmetry(P);

    }

    @Override
    protected void initFilter(ISsf ssf) {
        dynamics = ssf.getDynamics();
        measurement = ssf.getMeasurement();
    }

    private void ordinarySmoothing(ISsf ssf, final int end) {
        OrdinarySmoother smoother = new OrdinarySmoother();
        smoother.setCalcVariances(calcvar);
        int beg=frslts.getEndDiffusePosition();
        smoother.process(ssf, beg, end, frslts, srslts);
        // updates R, N
        Rf.copy(smoother.getFinalR());
        if (calcvar) {
            N0.copy(smoother.getFinalN());
        }
    }

        public IDiffuseFilteringResults getFilteringResults() {
        return frslts;
      }

}
