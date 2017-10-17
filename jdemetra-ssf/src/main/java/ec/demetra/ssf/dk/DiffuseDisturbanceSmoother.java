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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.univariate.DisturbanceSmoother;
import ec.demetra.ssf.univariate.IDisturbanceSmoothingResults;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.ISsfMeasurement;

/**
 *
 * @author Jean Palate
 */
public class DiffuseDisturbanceSmoother {

    private ISsfDynamics dynamics;
    private ISsfMeasurement measurement;
    private IDisturbanceSmoothingResults srslts;
    private IBaseDiffuseFilteringResults frslts;

    private double e, f, esm, esmVariance, h, fi;
    private DataBlock C, Ci, R, Ri, U;
    private Matrix N, UVar, S;
    private boolean missing, res, calcvar = true;
    private int pos;
    // temporary
    private DataBlock tmp;
    private double c, v;

    public boolean process(final ISsf ssf, final ISsfData data, IDisturbanceSmoothingResults sresults) {
        IBaseDiffuseFilteringResults fresults = DkToolkit.sqrtFilter(ssf, data, false);
        // rescale the variances
        return process(ssf, data.getLength(), fresults, sresults);
    }

    public boolean process(ISsf ssf, final int endpos, IBaseDiffuseFilteringResults results, IDisturbanceSmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        initFilter(ssf);
        initSmoother(ssf);
        ordinarySmoothing(ssf, endpos);
        pos = frslts.getEndDiffusePosition();
        while (--pos >= 0) {
            loadInfo();
            if (iterate()) {
                srslts.saveSmoothedTransitionDisturbances(pos, U, UVar == null ? null : UVar.all());
                if (res) {
                    srslts.saveSmoothedMeasurementDisturbance(pos, esm, esmVariance);
                }
            }
        }
        return true;
    }

    private void initSmoother(ISsf ssf) {
        int dim = ssf.getStateDim();
        int resdim = dynamics.getInnovationsDim();

        R = new DataBlock(dim);
        C = new DataBlock(dim);
        Ri = new DataBlock(dim);
        Ci = new DataBlock(dim);
        U = new DataBlock(resdim);
        S = new Matrix(dim, resdim);
        if (calcvar) {
            N = Matrix.square(dim);
            tmp = new DataBlock(dim);
            UVar = Matrix.square(resdim);
            if (measurement.isTimeInvariant()) {
                h = measurement.errorVariance(0);
            }
        }
        if (dynamics.isTimeInvariant()) {
            dynamics.S(0, S.all());
        }
    }

    private void loadInfo() {
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
        if (!dynamics.isTimeInvariant()) {
            S.clear();
            SubMatrix sm = S.all();
            dynamics.S(pos, sm);
        }
        if (!measurement.isTimeInvariant()) {
            h = measurement.errorVariance(pos);
        }
    }

    private boolean iterate() {
        iterateR();
        if (calcvar) {
            iterateN();
        }
        // updates the smoothed disturbances
        if (res) {
            if (!missing) {
                esm = c * h;
            } else {
                esm = Double.NaN;
            }
        }
        dynamics.XS(pos, R, U);
        if (calcvar) {
            if (res) {
                if (!missing) {
                    esmVariance = h - h * h * v;
                } else {
                    esmVariance = Double.NaN;
                }
            }
            // v(U) = I-S'NS
            UVar.clear();
            SymmetricMatrix.quadraticForm(N.all(), S.all(), UVar.all());
            UVar.chs();
            UVar.diagonal().add(1);
        }
        return true;
    }

    /**
     *
     */
    private void iterateN() {
        if (missing || (f == 0 && fi == 0)) {
            iterateMissingN();
        } else if (fi == 0) {
            iterateRegularN();
        } else {
            iterateDiffuseN();
        }
        SymmetricMatrix.reinforceSymmetry(N);
    }

    private void iterateMissingN() {
        tvt(N);
    }

    private void iterateRegularN() {
        // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
        tvt(N);
        tmp.product(C, N.columns());
        // 2. v
        v = 1 / f + tmp.dot(C);
        measurement.VpZdZ(pos, N.all(), v);
        subZ(N.rows(), tmp);
        subZ(N.columns(), tmp);
    }

    private void iterateDiffuseN() {
        tvt(N);
        tmp.product(Ci, N.columns());
        // 2. v
        v = tmp.dot(Ci);
        measurement.VpZdZ(pos, N.all(), v);
        subZ(N.rows(), tmp);
        subZ(N.columns(), tmp);
    }

    /**
     *
     */
    private void iterateR() {
        if (fi == 0) {
            iterateRegularR();
        } else {
            iterateDiffuseR();
        }
    }

    private void iterateRegularR() {
        // R(t-1)=v(t)/f(t)Z(t)+R(t)L(t)
        //   = v/f*Z + R*(T-TC/f*Z)
        //  = (v - RT*C)/f*Z + RT
        dynamics.XT(pos, R);
        dynamics.XT(pos, Ri);
        if (!missing && f != 0) {
            // RT
            c = e / f - R.dot(C);
            measurement.XpZd(pos, R, c);
        }
    }

    private void iterateDiffuseR() {
        dynamics.XT(pos, R);
        dynamics.XT(pos, Ri);
        if (!missing && f != 0) {
            c = -Ri.dot(Ci);
            // Ri(t-1)=c*Z(t) +Ri(t)*T(t)
            // c = e/fi-(Ri(t)*T(t)*Ci(t))/fi-(Rf(t)*T(t)*Cf(t))/f
            double ci = e / fi + c - R.dot(C);
            measurement.XpZd(pos, Ri, ci);
            // Rf(t-1)=c*Z(t)+Rf(t)*T(t)
            // c =  - Rf(t)T(t)*Ci/fi
            double cf = -R.dot(Ci);
            measurement.XpZd(pos, R, cf);
        }
    }

    private void initFilter(ISsf ssf) {
        dynamics = ssf.getDynamics();
        measurement = ssf.getMeasurement();
        res = measurement.hasErrors();
    }

    public void setCalcVariances(boolean b) {
        calcvar = b;
    }

    public boolean isCalcVariances() {
        return calcvar;
    }

    private void tvt(Matrix N) {
        DataBlockIterator columns = N.columns();
        DataBlock col = columns.getData();
        do {
            dynamics.XT(pos, col);
        } while (columns.next());
        DataBlockIterator rows = N.rows();
        DataBlock row = rows.getData();
        do {
            dynamics.XT(pos, row);
        } while (rows.next());

    }

    private void subZ(DataBlockIterator rows, DataBlock b) {
        DataBlock row = rows.getData();
        do {
            double cur = b.get(rows.getPosition());
            if (cur != 0) {
                measurement.XpZd(pos, row, -cur);
            }
        } while (rows.next());
    }

    private void ordinarySmoothing(ISsf ssf, final int endpos) {
        DisturbanceSmoother smoother = new DisturbanceSmoother();
        smoother.setCalcVariances(calcvar);
        smoother.process(ssf, frslts.getEndDiffusePosition(), endpos, frslts, srslts);
        // updates R, N
        R.copy(smoother.getFinalR());
        if (calcvar) {
            N.copy(smoother.getFinalN());
        }
    }

    public IBaseDiffuseFilteringResults getFilteringResults() {
        return frslts;
    }

    public DataBlock getFinalR() {
        return R;
    }

    public DataBlock getFinalRi() {
        return Ri;
    }

    public Matrix getFinalN() {
        return N;
    }

    public DataBlock firstSmoothedState() {

        int n = dynamics.getStateDim();
        // initial state
        DataBlock a = new DataBlock(n);
        Matrix Pf0 = Matrix.square(n);
        dynamics.a0(a);
        dynamics.Pf0(Pf0.all());
        // stationary initialization
        a.addProduct(R, Pf0.columns());

        // non stationary initialisation
        Matrix Pi0 = Matrix.square(n);
        dynamics.Pi0(Pi0.all());
        a.addProduct(Ri, Pi0.columns());
        return a;
    }
}
