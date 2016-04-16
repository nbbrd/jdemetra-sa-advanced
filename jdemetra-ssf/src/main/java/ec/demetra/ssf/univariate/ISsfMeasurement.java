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

import ec.demetra.ssf.ISsfBasic;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 *
 * @author Jean Palate
 */
public interface ISsfMeasurement extends ISsfBasic{
    

//<editor-fold defaultstate="collapsed" desc="description">

    /**
     * Gets a given measurement equation at a given position
     *
     * @param pos Position of the measurement. Must be greater or equal than 0
     * @param z The buffer that will contain the measurement coefficients. Its
     * size must be equal to the state dimension
     */
    void Z(int pos, DataBlock z);

    /**
     * Are there stochastic errors in the measurement
     *
     * @return False if the variance of the error is always 0. True otherwise.
     */
    boolean hasErrors();
    
    /**
     * Is there a stochastic error in the measurement at the given position
     *
     * @param pos
     * @return False if the error variance is 0. True otherwise.
     */
    boolean hasError(int pos);

    /**
     * Gets the variance of the measurement error at a given position
     *
     * @param pos
     * @return The variance of the measurement error at the given position. 
     * May be 0
     */
    double errorVariance(int pos);
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="forward operations">
    /**
     *
     * @param pos
     * @param m
     * @return
     */
    double ZX(int pos, DataBlock m);

    default void ZM(int pos, SubMatrix m, DataBlock zm) {
        int ipos = 0;
        DataBlockIterator cols = m.columns();
        DataBlock col = cols.getData();
        do {
            zm.set(ipos, ZX(pos, col));
            ++ipos;
        } while (cols.next());
    }

    /**
     * Computes Z(pos) * V * Z'(pos)
     *
     * @param pos
     * @param V Matrix (statedim x statedim)
     * @return
     */
    double ZVZ(int pos, SubMatrix V);

//</editor-fold>    

//<editor-fold defaultstate="collapsed" desc="backward operations">
    /**
     *
     * @param pos
     * @param V
     * @param d
     */
    void VpZdZ(int pos, SubMatrix V, double d);


    /**
     * Computes x = x + Z * D
     *
     * @param pos
     * @param x DataBlock of size statedim
     * @param d
     */
    void XpZd(int pos, DataBlock x, double d);

//</editor-fold>

}
