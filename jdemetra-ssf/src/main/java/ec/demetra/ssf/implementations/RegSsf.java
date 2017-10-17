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

import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.demetra.ssf.univariate.Ssf;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
public class RegSsf extends Ssf {

    public static RegSsf create(ISsf model, SubMatrix X) {
        Xdynamics xdyn=new Xdynamics(model.getDynamics(), X.getColumnsCount());
        Xmeasurement xm=new Xmeasurement(model.getMeasurement(), X);
        return new RegSsf(xdyn, xm);
    }

    private RegSsf(ISsfDynamics dyn, ISsfMeasurement m) {
        super(dyn, m);
    }

    static class Xdynamics implements ISsfDynamics {

        private final int n, nx;
        private final ISsfDynamics dyn;

        Xdynamics(ISsfDynamics dyn, int nx) {
            this.dyn = dyn;
            n = dyn.getStateDim();
            this.nx = nx;
        }

        @Override
        public int getInnovationsDim() {
            return dyn.getInnovationsDim();
        }

        @Override
        public void V(int pos, SubMatrix qm) {
            dyn.V(pos, qm.topLeft(n, n));
        }

        @Override
        public void S(int pos, SubMatrix cm) {
            dyn.S(pos, cm.top(n));
        }

        @Override
        public boolean hasInnovations(int pos) {
            return dyn.hasInnovations(pos);
        }

        @Override
        public void T(int pos, SubMatrix tr) {
            dyn.T(pos, tr.topLeft(n, n));
            tr.diagonal().drop(n, 0).set(1);
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getNonStationaryDim() {
            return nx + dyn.getNonStationaryDim();
        }

        @Override
        public void diffuseConstraints(SubMatrix b) {
            int nd = dyn.getNonStationaryDim();
            SubMatrix tmp = b.topLeft(n, nd);
            if (nd > 0) {
                dyn.diffuseConstraints(tmp);
            }
            tmp.next(nx, nx);
            tmp.diagonal().set(1);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return dyn.a0(a0.range(0, n));
        }

        @Override
        public boolean Pf0(SubMatrix pf0) {
            return dyn.Pf0(pf0.topLeft(n, n));
        }

        @Override
        public void Pi0(SubMatrix pi0) {
            SubMatrix tmp=pi0.topLeft(n, n);
            dyn.Pi0(tmp);
            tmp.next(nx, nx);
            tmp.diagonal().set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            dyn.TX(pos, x.range(0, n));
        }

        @Override
        public void TM(int pos, SubMatrix m) {
            dyn.TM(pos, m.top(n));
        }

        @Override
        public void TVT(int pos, SubMatrix m) {
            SubMatrix z = m.topLeft(n, n);
            dyn.TVT(pos, z);
            SubMatrix zc = z.clone();
            z.hnext(nx);
            dyn.TM(pos, z);
            zc.vnext(nx);
            zc.copy(z.transpose());
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void XT(int pos, DataBlock x) {
            dyn.XT(pos, x.range(0, n));
        }

        @Override
        public void addV(int pos, SubMatrix p) {
            dyn.addV(pos, p.topLeft(n, n));
        }

        @Override
        public int getStateDim() {
            return nx + n;
        }

        @Override
        public boolean isTimeInvariant() {
            return dyn.isTimeInvariant();
        }

        @Override
        public boolean isValid() {
            return nx > 0;
        }

    }

    static class Xmeasurement implements ISsfMeasurement {

        private final ISsfMeasurement m;
        private final SubMatrix data;
        private final int n, nx;
        private final DataBlock tmp;

        private Xmeasurement(final ISsfMeasurement m, final SubMatrix data) {
            this.data = data;
            this.m = m;
            n = m.getStateDim();
            nx = data.getColumnsCount();
            tmp = new DataBlock(nx);
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            DataBlock range = z.range(0, n);
            m.Z(pos, range);
            range.next(nx);
            range.copy(data.row(pos));
        }

        @Override
        public boolean hasErrors() {
            return m.hasErrors();
        }

        @Override
        public boolean hasError(int pos) {
            return m.hasError(pos);
        }

        @Override
        public double errorVariance(int pos) {
            return m.errorVariance(pos);
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            DataBlock range = x.range(0, n);
            double r = m.ZX(pos, range);
            range.next(nx);
            return r + range.dot(data.row(pos));
        }

        @Override
        public double ZVZ(int pos, SubMatrix V) {
            SubMatrix v = V.topLeft(n, n);
            double v00 = m.ZVZ(pos, v);
            v.hnext(nx);
            tmp.set(0);
            m.ZM(pos, v, tmp);
            double v01 = tmp.dot(data.row(pos));
            v.vnext(nx);
            double v11 = SymmetricMatrix.quadraticForm(v, data.row(pos));
            return v00 + 2 * v01 + v11;
        }

        @Override
        public void VpZdZ(int pos, SubMatrix V, double d) {
            SubMatrix v = V.topLeft(n, n);
            m.VpZdZ(pos, v, d);
            SubMatrix vtmp = v.clone();
            vtmp.hnext(nx);
            v.vnext(nx);
            DataBlockIterator rows = v.rows();
            DataBlock row = rows.getData();
            DataBlock xrow = data.row(pos);
            do {
                m.XpZd(pos, row, d * xrow.get(rows.getPosition()));
            } while (rows.next());
            vtmp.copy(v.transpose());
            v.hnext(nx);
            v.addXaXt(d, xrow);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            DataBlock range = x.range(0, n);
            m.XpZd(pos, range, d);
            range.next(nx);
            range.addAY(d, data.row(pos));
        }

        @Override
        public int getStateDim() {
            return nx + n;
        }

        @Override
        public boolean isValid() {
            return nx > 0;
        }

    }

}
