/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.ssf.univariate;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.ResultsRange;

/**
 *
 * @author Jean Palate
 */
public class DisturbanceSmoother {

    private ISsfDynamics dynamics;
    private ISsfMeasurement measurement;
    private IDisturbanceSmoothingResults srslts;
    private IFilteringResults frslts;

    private double err, errVariance, esm, esmVariance, h;
    private DataBlock K, R, U;
    private Matrix N, UVar, S;
    private boolean missing, res, calcvar = true;
    private int pos, stop;
    // temporary
    private DataBlock tmp;
    private double c, v;

    public boolean process(ISsf ssf, ISsfData data) {
        if (ssf.getDynamics().isDiffuse()) {
            return false;
        }
        OrdinaryFilter filter = new OrdinaryFilter();
        DefaultFilteringResults fresults = DefaultFilteringResults.light();
        if (!filter.process(ssf, data, fresults)) {
            return false;
        }
        return process(ssf, 0, data.getLength(), fresults);
    }

    public boolean process(ISsf ssf, DefaultFilteringResults results) {
        if (ssf.getDynamics().isDiffuse()) {
            return false;
        }
        ResultsRange range = results.getRange();
        return process(ssf, range.getStart(), range.getEnd(), results);
    }

    public boolean process(ISsf ssf, int start, int end, IFilteringResults results) {
        IDisturbanceSmoothingResults sresults;
        boolean hasErrors = ssf.getMeasurement().hasErrors();
        if (calcvar) {
            sresults = DefaultDisturbanceSmoothingResults.full(hasErrors);
        } else {
            sresults = DefaultDisturbanceSmoothingResults.light(hasErrors);
        }

        return process(ssf, start, end, results, sresults);
    }

    public boolean process(ISsf ssf, ISsfData data, IDisturbanceSmoothingResults sresults, final int stop) {
        OrdinaryFilter filter = new OrdinaryFilter();
        DefaultFilteringResults fresults = DefaultFilteringResults.light();
        if (!filter.process(ssf, data, fresults)) {
            return false;
        }
        return process(ssf, stop, data.getLength(), fresults);
    }

    public boolean process(ISsf ssf, final int start, final int end, IFilteringResults results, IDisturbanceSmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        stop = start;
        pos = end;
        initFilter(ssf);
        initSmoother(ssf);
        while (--pos >= stop) {
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

    public boolean resume(final int start) {
        stop = start;
        while (pos >= stop) {
            loadInfo();
            if (iterate()) {
                srslts.saveSmoothedTransitionDisturbances(pos, U, UVar.all());
                if (res) {
                    srslts.saveSmoothedMeasurementDisturbance(pos, esm, esmVariance);
                }
            }
            pos--;
        }
        return true;
    }

    public IDisturbanceSmoothingResults getResults() {
        return srslts;
    }

    public DataBlock getFinalR() {
        return R;
    }

    public Matrix getFinalN() {
        return N;
    }

    private void initSmoother(ISsf ssf) {
        int dim = ssf.getStateDim();
        int resdim = dynamics.getInnovationsDim();

        R = new DataBlock(dim);
        K = new DataBlock(dim);
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
        err = frslts.error(pos);
        missing = !Double.isFinite(err);
        if (!missing) {
            errVariance = frslts.errorVariance(pos);
            K.setAY(1 / errVariance, frslts.M(pos));
            dynamics.TX(pos, K);
        }
        if (!dynamics.isTimeInvariant()) {
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
            esm = c * h;
        }
        dynamics.XS(pos, R, U);
        if (calcvar) {
            if (res) {
                esmVariance = h - h * h * v;
            }
            // v(U) = I-S'NS
            SymmetricMatrix.quadraticForm(N.all(), S.all(), UVar.all());
            UVar.chs();
            UVar.diagonal().add(1);
        }
        return true;
    }
    // 

    /**
     *
     */
    private void iterateN() {
        if (!missing && errVariance != 0) {
            // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
            // = Z'(t)*Z(t)/f(t) + (T' - Z'K')N(T - KZ)
            // =  Z'(t)*Z(t)(1/f(t) + K'NK) + T'NT - <T'NKZ>
            // 1. NK 
            tmp.product(N.rows(), K);
            // 2. v
            v = 1 / errVariance + tmp.dot(K);
            // 3. T'NK
            dynamics.XT(pos, tmp);
            // TNT
            tvt(N);
            measurement.VpZdZ(pos, N.all(), v);
            subZ(N.rows(), tmp);
            subZ(N.columns(), tmp);
        } else {
            tvt(N);
        }
        SymmetricMatrix.reinforceSymmetry(N);
    }

    /**
     *
     */
    private void iterateR() {
        // R(t-1)=(v/f + R(t)*K)Z + R(t)*T
        // R(t-1)=esm*Z +  R(t)*T
        if (!missing && errVariance != 0) {
            // RT
            c = (err / errVariance - R.dot(K));
            dynamics.XT(pos, R);
            measurement.XpZd(pos, R, c);
        } else {
            dynamics.XT(pos, R);
            c = Double.NaN;
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

    public DataBlock firstSmoothedState() {
        int n = dynamics.getStateDim();
        // initial state
        DataBlock a = new DataBlock(n);
        Matrix Pf0 = Matrix.square(n);
        dynamics.a0(a);
        dynamics.Pf0(Pf0.all());
        // stationary initialization
        a.addProduct(R, Pf0.columns());
        return a;
    }
}
