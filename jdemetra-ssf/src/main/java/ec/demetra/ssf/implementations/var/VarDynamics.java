/*
 * Copyright 2013-2014 National Bank of Belgium
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
package ec.demetra.ssf.implementations.var;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.HouseholderR;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.demetra.ssf.ISsfDynamics;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;

/**
 *
 * @author Jean Palate
 */
public class VarDynamics implements ISsfDynamics {

    private final VarDescriptor desc;
    private final Matrix V0, T;
    private final int nvars, nl, nlx;
    private final DataBlock ttmp, xtmp;
    private Matrix L;

    public static VarDynamics of(final VarDescriptor desc, final Matrix V0) {
        if (V0.getColumnsCount() != desc.getVariablesCount()) {
            return null;
        }
        int nlx = desc.getVarMatrix().getColumnsCount();
        if (nlx % desc.getLagsCount() != 0) {
            return null;
        }
        nlx /= desc.getVariablesCount();
        if (nlx < desc.getLagsCount()) {
            return null;
        }
        return new VarDynamics(desc, nlx, V0);
    }

    private VarDynamics(final VarDescriptor desc, final int nlx, final Matrix V0) {
        this.desc = desc;
        nl = desc.getLagsCount();
        this.nlx = nlx;
        nvars = desc.getVariablesCount();
        this.V0 = V0;
        ttmp = new DataBlock(nvars);
        xtmp = new DataBlock(nvars * nl);
        T = desc.getVarMatrix();
    }

    public VarDescriptor getDescriptor() {
        return desc;
    }

    public int getLagsCount() {
        return nlx;
    }

    private Matrix L() {
        if (L == null) {
            Matrix v = desc.getInnovationsVariance();
            L = v.clone();
            SymmetricMatrix.lcholesky(L, 1e-9);
        }
        return L;
    }

    @Override
    public int getStateDim() {
        return nvars * nlx;
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
        return nvars;
    }

    @Override
    public void V(int pos, SubMatrix qm) {
        Matrix v = desc.getInnovationsVariance();
        qm.topLeft(nvars, nvars).copy(v.all());
    }

    @Override
    public boolean hasInnovations(int pos) {
        return true;
    }

//    @Override
//    public void Q(int pos, SubMatrix qm) {
//        qm.copy(desc.getInnovationsVariance().subMatrix());
//    }
//
    @Override
    public void S(int pos, SubMatrix sm) {
        sm.topLeft(nvars, nvars).copy(L().all());
    }

    @Override
    public void addSU(int pos, DataBlock x, DataBlock u) {
        DataBlock v = u.deepClone();
        LowerTriangularMatrix.rmul(L(), v);
        x.add(v);
    }

    @Override
    public void XS(int pos, DataBlock x, DataBlock xs) {
        xs.copy(x.range(0, nvars));
        LowerTriangularMatrix.lmul(L(), xs);
    }

//    @Override
//    public void addSX(int pos, DataBlock x, DataBlock y) {
//        for (int i = 0, r = 0; i < neq; ++i, r += nlx) {
//            y.add(r, x.get(i));
//        }
//    }
//    
    @Override
    public void T(int pos, SubMatrix tr) {
        Matrix v = desc.getVarMatrix();
        tr.top(nvars).copy(v.all());
        tr.subDiagonal(-nvars).set(1);
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
        pf0.topLeft(V0.getRowsCount(), V0.getColumnsCount()).copy(V0.all());
        return true;
    }

    @Override
    public void TX(int pos, DataBlock x) {
        TX(x);
    }

    private void TX(DataBlock x) {
        for (int i = 0; i < nvars; ++i) {
            ttmp.set(i, T.row(i).dot(x));
        }
        x.fshift(nvars);
        x.extract(0, nvars).copy(ttmp);
    }


    @Override
    public void XT(int pos, DataBlock x) {
        Matrix v = desc.getVarMatrix();
        xtmp.set(0);
        xtmp.product(x.range(0, nvars), v.columns());
        x.bshift(nvars);
        x.range((nlx-1)*nvars, x.getLength()).set(0);
        x.range(0, nl*nvars).copy(xtmp);
    }

    @Override
    public void addV(final int pos, final SubMatrix v) {
        v.topLeft(nvars, nvars).copy(desc.getInnovationsVariance().all());
    }

}
