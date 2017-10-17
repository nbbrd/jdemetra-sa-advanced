/*
 * Copyright 2015 National Bank of Belgium
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
package ec.demetra.ssf.univariate;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.demetra.ssf.ISsfDynamics;
import ec.tstoolkit.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
public interface ISsf {

    ISsfMeasurement getMeasurement();

    ISsfDynamics getDynamics();

    int getStateDim();

    boolean isTimeInvariant();

//<editor-fold defaultstate="collapsed" desc="auxiliary operations">
    /**
     * Computes X*L, where L = T(I - m/f * z)
     *
     * @param pos
     * @param x The row array being modified
     * @param m A Colunm array (usually P*Z')
     * @param f The divisor of m (usually ZPZ'+ H)
     */
    default void XL(int pos, DataBlock x, DataBlock m, double f) {
        // XT - [(XT)*m]/f * z 
        getDynamics().XT(pos, x);
        getMeasurement().XpZd(pos, x, -x.dot(m) / f);
    }

    default void ML(int pos, SubMatrix M, DataBlock m, double f) {
        // MT - [(MT)*m]/f * z
        ISsfDynamics dynamics = getDynamics();
        ISsfMeasurement measurement = getMeasurement();
        // Apply XL on each row of M
        DataBlockIterator rows = M.rows();
        DataBlock row = rows.getData();
        do {
            dynamics.XT(pos, row);
            measurement.XpZd(pos, row, -row.dot(m) / f);
        } while (rows.next());
    }

    /**
     *
     * @param pos
     * @param x The column array being modified
     * @param m A Colunm array (usually P*Z')
     * @param f The divisor of m (usually ZPZ'+ H)
     */
    default void LX(int pos, DataBlock x, DataBlock m, double f) {
        // TX - T*m/f * z * X
        // TX - T * m * (zX)/f)
        // T (X - m*(zX/f))
        x.addAY(-getMeasurement().ZX(pos, x) / f, m);
        getDynamics().XT(pos, x);
    }

    default void LM(int pos, SubMatrix M, DataBlock m, double f) {
        // TX - T*m/f * z * X
        // TX - T * m * (zX)/f)
        // T (X - m*(zX/f))
        ISsfDynamics dynamics = getDynamics();
        ISsfMeasurement measurement = getMeasurement();
        // Apply LX on each column of M
        DataBlockIterator cols = M.columns();
        DataBlock col = cols.getData();
        do {
            col.addAY(-measurement.ZX(pos, col) / f, m);
            dynamics.XT(pos, col);
        } while (cols.next());
    }
    
    default boolean diffuseEffects(SubMatrix effects){
        ISsfDynamics dynamics = getDynamics();
        ISsfMeasurement measurement = getMeasurement();
        int n=dynamics.getStateDim();
        int d=dynamics.getNonStationaryDim();
        if (d == 0 || d != effects.getColumnsCount())
            return false;
        SubMatrix matrix=new Matrix(n, d).all();
        // initialization
        dynamics.diffuseConstraints(matrix);
        DataBlockIterator rows = effects.rows();
        DataBlock row = rows.getData();
        int pos=0;
        do {
            // Apply T on matrix and Copy Z*matrix in the current row
            measurement.ZM(pos, matrix, row);
            dynamics.TM(pos++, matrix);
        } while (rows.next());
        return true;
    }
//</editor-fold>
}
