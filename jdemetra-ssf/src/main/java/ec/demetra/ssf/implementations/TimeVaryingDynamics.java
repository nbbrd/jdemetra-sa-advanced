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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.demetra.ssf.ISsfDynamics;

/**
 *
 * @author Jean Palate
 */
public class TimeVaryingDynamics {

    public static ISsfDynamics create(IReadDataBlock dvar) {
        return new TimeVaryingDiag(dvar);
    }

    public static ISsfDynamics create(SubMatrix var) {
        return new TimeVaryingFull(var);
    }

    static class TimeVaryingDiag implements ISsfDynamics {

        private final DataBlock var, std;

        TimeVaryingDiag(final double[] var) {
            this.var = new DataBlock(var);
            this.std = new DataBlock(var);
            std.sqrt();
        }

        TimeVaryingDiag(final IReadDataBlock var) {
            this.var = new DataBlock(var);
            this.std = new DataBlock(var);
            std.sqrt();
        }

        @Override
        public int getStateDim() {
            return var.getLength();
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
            return var.getLength();
        }

        @Override
        public void V(int pos, SubMatrix qm) {
            qm.diagonal().copy(var);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, SubMatrix sm) {
            sm.diagonal().copy(std);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            int n=x.getLength();
            for (int i=0; i<n; ++i){
                x.add(i, u.get(i)*std.get(i));
            }
        }

        @Override

        public void XS(int pos, DataBlock x, DataBlock xs) {
            int n=x.getLength();
            for (int i=0; i<n; ++i){
                xs.set(i, x.get(i)*std.get(i));
            }
        }
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
            return var.getLength();
        }

        @Override
        public void diffuseConstraints(SubMatrix b) {
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
        public void Pi0(SubMatrix p) {
            p.diagonal().set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void TVT(int pos, SubMatrix v) {
        }

        @Override
        public void addV(int pos, SubMatrix p) {
            p.diagonal().add(var);
        }
    }

    static class TimeVaryingFull implements ISsfDynamics {

        private final SubMatrix var, s;

        TimeVaryingFull(final SubMatrix var) {
            this.var = var;
            Matrix S=new Matrix(var);
            SymmetricMatrix.lcholesky(S, 1e-9);
            s=S.all();
        }

        TimeVaryingFull(final SubMatrix var, final SubMatrix s) {
            this.var = var;
            this.s=s;
        }

        @Override
        public int getStateDim() {
            return var.getColumnsCount();
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
            return var.getColumnsCount();
        }

        @Override
        public void V(int pos, SubMatrix qm) {
            qm.copy(var);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, SubMatrix sm) {
            sm.copy(s);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.addProduct(s.rows(), u);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.product(x, s.columns());
        }

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
            return var.getColumnsCount();
        }

        @Override
        public void diffuseConstraints(SubMatrix b) {
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
        public void Pi0(SubMatrix p) {
            p.diagonal().set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void TVT(int pos, SubMatrix v) {
        }

        @Override
        public void addV(int pos, SubMatrix p) {
            p.add(var);
        }
    }
}
