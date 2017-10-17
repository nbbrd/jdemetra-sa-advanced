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
package ec.demetra.ssf.multivariate;

import ec.demetra.ssf.ISsfBasic;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 *
 * @author Jean Palate
 */
public interface ISsfMeasurements extends ISsfBasic{

//<editor-fold defaultstate="collapsed" desc="description">
    /**
     * Is this object time invariant
     *
     * @return
     */
    boolean isTimeInvariant();

    /**
     * Gets the number of measurements at a given position
     *
     * @param pos The position
     * @return The number of measurements. May be 0
     */
    int getCount(int pos);

    /**
     * Gets the number of measurements at a given position
     *
     * @return The maximum number of measurements by period.
     */
    int getMaxCount();

    /**
     *
     * @return True if the number of measurements doesn't depend on the position
     */
    boolean isHomogeneous();

    /**
     * Gets a given measurement equation at a given position
     *
     * @param pos Position of the measurement. Must be greater or equal than 0
     * @param var
     * @param z The buffer that will contain the measurement coefficients. Its
     * size must be equal to the state dimension
     */
    void Z(int pos, int var, DataBlock z);

    default void Z(int pos, SubMatrix z) {
        DataBlockIterator rows = z.rows();
        DataBlock row = rows.getData();
        int v = 0;
        do {
            Z(pos, v++, row);
        } while (rows.next());
    }

    /**
     * Are there stochastic errors in the measurement
     *
     * @return False if the variance of the errors is always 0. True otherwise.
     */
    boolean hasErrors();
    
    /**
     * 
     * @return True if there is no error term or if the covariance matrix 
     * of the errors is diagonal (and of course if the model is univariate)
     */
    boolean hasIndependentErrors();

    /**
     * Is there a stochastic error in the measurement at the given position
     *
     * @param pos
     * @return False if the error variance is 0. True otherwise.
     */
    boolean hasError(int pos);

    /**
     * Gets the variance of the measurements error at a given position
     *
     * @param pos
     * @param h The matrix that will contain the variance. Should be 0 on
     * entry. The matrix must have the size of the measurements
     * (=getCount(pos)).
     */

    void H(int pos, SubMatrix h);
    /**
     * Gets the Cholesky factor of the variance of the measurements error at a given position
     *
     * @param pos
     * @param r The matrix that will contain the cholesky factor. Should be 0 on
     * entry. The matrix must have the size of the measurements
     * (=getCount(pos)).
     */
    void R(int pos, SubMatrix r);
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="forward operations">
    /**
     *
     * @param pos
     * @param var
     * @param m
     * @return
     */
    double ZX(int pos, int var, DataBlock m);

    default void ZM(int pos, SubMatrix m, SubMatrix zm) {
        int ivar = 0;
        DataBlockIterator rows = zm.rows();
        DataBlock row = rows.getData();
        do {
            ZM(pos, ivar, m, row);
            ++ivar;
        } while (rows.next());
    }

    default void ZM(int pos, int var, SubMatrix m, DataBlock zm) {
        int ipos = 0;
        DataBlockIterator cols = m.columns();
        DataBlock col = cols.getData();
        do {
            zm.set(ipos, ZX(pos, var, col));
            ++ipos;
        } while (cols.next());
    }

    /**
     * Computes Z(pos, i) * V * Z'(pos, j)
     *
     * @param pos
     * @param V Matrix (statedim x statedim)
     * @param ivar
     * @param jvar
     * @return
     */
    double ZVZ(int pos, int ivar, int jvar, SubMatrix V);

    /**
     * Computes Z(pos) * V * Z'(pos)
     *
     * @param pos
     * @param V Matrix (statedim x statedim)
     * @param zVz Matrix (count x count)
     */
    default void ZVZ(int pos, SubMatrix V, SubMatrix zVz) {
        int n = zVz.getRowsCount();
        for (int r = 0; r < n; ++r) {
            double vrr = ZVZ(pos, r, r, V);
            zVz.set(r, r, vrr);
            for (int c = 0; c < r; ++c) {
                double vrc = ZVZ(pos, r, c, V);
                zVz.set(r, c, vrc);
                zVz.set(c, r, vrc);
            }
        }
    }

    /**
     * Computes V = V + H
     *
     * @param pos
     * @param V
     */
    void addH(int pos, SubMatrix V);
//</editor-fold>    

//<editor-fold defaultstate="collapsed" desc="backward operations">
    /**
     *
     * @param pos
     * @param V
     * @param ivar
     * @param jvar
     * @param d
     */
    void VpZdZ(int pos, int ivar, int jvar, SubMatrix V, double d);

    /**
     * Computes V = V + Z'(pos) * D * Z(pos)
     *
     * @param pos
     * @param V Matrix (statedim x statedim)
     * @param D Matrix (count x count). Should be symmetric.
     */
    default void VpZdZ(int pos, SubMatrix V, SubMatrix D) {
        int n = D.getRowsCount();
        for (int r = 0; r < n; ++r) {
            VpZdZ(pos, r, r, V, D.get(r, r));
            for (int c = 0; c < r; ++c) {
                VpZdZ(pos, r, c, V, D.get(r, c));
                VpZdZ(pos, c, r, V, D.get(c, r));
            }
        }
    }

    /**
     * Computes x = x + Z * D
     *
     * @param pos
     * @param x DataBlock of size statedim
     * @param ivar
     * @param d
     */
    void XpZd(int pos, int ivar, DataBlock x, double d);

    /**
     * Computes x = x + Z * D
     *
     * @param pos
     * @param x DataBlock of size statedim
     * @param d DataBlock of size count
     */
    default void XpZd(int pos, DataBlock x, DataBlock d) {
        int n = d.getLength();
        for (int r = 0; r < n; ++r) {
            XpZd(pos, r, x, d.get(r));

        }
    }
//</editor-fold>

}
