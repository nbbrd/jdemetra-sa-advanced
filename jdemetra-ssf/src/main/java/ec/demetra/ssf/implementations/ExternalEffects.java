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

import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
public class ExternalEffects implements ISsfMeasurement {

    public static ISsfMeasurement extend(ISsfMeasurement m, final SubMatrix data) {
        return new ExternalEffects(m, data);
    }

    private final ISsfMeasurement m;
    private final SubMatrix data;
    private final int nm, nx;
    private final DataBlock tmp;

    private ExternalEffects(final ISsfMeasurement m, final SubMatrix data) {
        this.data = data;
        this.m = m;
        nm = m.getStateDim();
        nx = data.getColumnsCount();
        tmp = new DataBlock(nx);
    }

    @Override
    public boolean isTimeInvariant() {
        return false;
    }

    @Override
    public void Z(int pos, DataBlock z) {
        DataBlock range = z.range(0, nm);
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
        DataBlock range = x.range(0, nm);
        double r = m.ZX(pos, range);
        range.next(nx);
        return r + range.dot(data.row(pos));
    }

    @Override
    public double ZVZ(int pos, SubMatrix V) {
        SubMatrix v = V.topLeft(nm, nm);
        double v00 = m.ZVZ(pos, v);
        v.vnext(nx);
        tmp.set(0);
        double v01 = tmp.dot(data.row(pos));
        m.ZM(pos, v, tmp);
        v.hnext(nx);
        double v11 = SymmetricMatrix.quadraticForm(v, data.row(pos));
        return v00 + 2 * v01 + v11;
    }

    @Override
    public void VpZdZ(int pos, SubMatrix V, double d) {
        SubMatrix v = V.topLeft(nm, nm);
        m.VpZdZ(pos, v, d);
        SubMatrix vtmp = v.clone();
        vtmp.hnext(nx);
        v.vnext(nx);
        DataBlockIterator rows=v.rows();
        DataBlock row=rows.getData();
        DataBlock xrow=data.row(pos);
        do{
            m.XpZd(pos, row, d*xrow.get(rows.getPosition()));
        }while (rows.next());
        vtmp.copy(v.transpose());
        v.hnext(nx);
        v.addXaXt(d, xrow);
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        DataBlock range = x.range(0, nm);
        m.XpZd(pos, range, d);
        range.next(nx);
        x.addAY(d, data.row(pos));
    }

    @Override
    public int getStateDim() {
        return nx + nm;
    }

    @Override
    public boolean isValid() {
        return nx > 0;
    }

}
