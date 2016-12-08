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
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.univariate.ISmoothingResults;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfMeasurement;

/**
 *
 * @author Jean Palate
 */
public abstract class BaseDiffuseSmoother {

    protected ISsfDynamics dynamics;
    protected ISsfMeasurement measurement;
    protected ISmoothingResults srslts;

    protected double e, f, fi;
    protected DataBlock C, Ci, Rf, Ri, tmp0, tmp1;
    protected Matrix N0, N1, N2;
    protected boolean missing, hasinfo, calcvar = true;

    public ISmoothingResults getResults() {
        return srslts;
    }

    protected void iterate(int pos) {
        iterateR(pos);
        updateA(pos);
        if (calcvar) {
            // P = P-PNP
            iterateN(pos);
            updateP(pos);
        }
    }
    // 

    protected abstract void updateA(int pos);

    protected abstract void updateP(int pos);

    /**
     * Computes in place x = x-c/f*z
     *
     * @param x
     * @param k
     */
    private void xQ(int pos, DataBlock x) {
        measurement.XpZd(pos, x, -x.dot(C));
    }

    private void XQ(int pos, DataBlockIterator X) {
        DataBlock x = X.getData();
        do {
            xQ(pos, x);
        } while (X.next());
    }

    private void xQi(int pos, DataBlock x) {
        measurement.XpZd(pos, x, -x.dot(Ci));
    }

    private void XQi(int pos, DataBlockIterator X) {
        DataBlock x = X.getData();
        do {
            xQi(pos, x);
        } while (X.next());
    }

    /**
     *
     */
    private void iterateN(int pos) {
        if (missing || (f == 0 && fi == 0)) {
            iterateMissingN(pos);
        } else if (fi == 0) {
            iterateRegularN(pos);
        } else {
            iterateDiffuseN(pos);
        }
    }

    private void iterateMissingN(int pos) {
        tvt(pos, N0);
        tvt(pos, N1);
        tvt(pos, N2);
        // reinforceSymmetry();
    }

    private void iterateRegularN(int pos) {
        // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
        tvt(pos, N0);
        XQ(pos, N0.rows());
        XQ(pos, N0.columns());
        measurement.VpZdZ(pos, N0.all(), 1 / f);
        tvt(pos, N1);
        XQ(pos, N1.columns());
        tvt(pos, N2);
    }

    private void iterateDiffuseN(int pos) {
        // Nf = Li'*Nf*Li
        // N1 = Z'Z/Fi + Li'*N1*Li - < Z'Kf'*Nf'*Li >
        // N2 = Z'Z * c + Li'*N2*Li - < Z'Kf'*N1'*Li >, c= Kf'*Nf*Kf-Ff/(Fi*Fi)
        // compute first N2 then N1 and finally Nf
        tvt(pos, N0);
        tvt(pos, N1);
        tvt(pos, N2);

        tmp0.product(C, N0.columns());
        tmp1.product(C, N1.columns());

        double kn0k = tmp0.dot(C);

        XQi(pos, N0.rows());
        XQi(pos, N0.columns());
        XQi(pos, N1.rows());
        XQi(pos, N2.columns());
        XQi(pos, N2.rows());
        XQi(pos, N1.columns());
        xQi(pos, tmp0);
        xQi(pos, tmp1);

        measurement.VpZdZ(pos, N1.all(), 1 / fi); //
        measurement.VpZdZ(pos, N2.all(), kn0k - f / (fi * fi));

        subZ(pos, N1.rows(), tmp0);
        subZ(pos, N1.columns(), tmp0);
        subZ(pos, N2.rows(), tmp1);
        subZ(pos, N2.columns(), tmp1);
    }

    private void tvt(int pos, Matrix N) {
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

    private void subZ(int pos, DataBlockIterator rows, DataBlock b) {
        DataBlock row = rows.getData();
        do {
            double cur = b.get(rows.getPosition());
            if (cur != 0) {
                measurement.XpZd(pos, row, -cur);
            }
        } while (rows.next());
    }

    /**
     *
     */
    private void iterateR(int pos) {
        if (fi == 0) {
            iterateRegularR(pos);
        } else {
            iterateDiffuseR(pos);
        }
    }

    private void iterateRegularR(int pos) {
        // R(t-1)=v(t)/f(t)Z(t)+R(t)L(t)
        //   = v/f*Z + R*(T-TC/f*Z)
        //  = (v - RT*C)/f*Z + RT
        dynamics.XT(pos, Rf);
        dynamics.XT(pos, Ri);
        if (!missing && f != 0) {
            // RT
            double c = e / f - Rf.dot(C);
            measurement.XpZd(pos, Rf, c);
        }
    }

    private void iterateDiffuseR(int pos) {
        dynamics.XT(pos, Rf);
        dynamics.XT(pos, Ri);
        if (!missing /*&& f != 0*/) {
            // Ri(t-1)=c*Z(t) +Ri(t)*T(t)
            // c = e/fi-(Ri(t)*T(t)*Ci(t))/fi-(Rf(t)*T(t)*Cf(t))/f
            double ci = e / fi - Ri.dot(Ci) - Rf.dot(C);
            measurement.XpZd(pos, Ri, ci);
            // Rf(t-1)=c*Z(t)+Rf(t)*T(t)
            // c =  - Rf(t)T(t)*Ci/fi
            double cf = -Rf.dot(Ci);
            measurement.XpZd(pos, Rf, cf);
        }
    }

    protected void initFilter(ISsf ssf) {
        dynamics = ssf.getDynamics();
        measurement = ssf.getMeasurement();
    }

    public void setCalcVariances(boolean b) {
        calcvar = b;
    }

    public boolean isCalcVariances() {
        return calcvar;
    }

}
