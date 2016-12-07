/*
* Copyright 2017 National Bank of Belgium
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
package ec.demetra.maths.matrices.implementation;

import ec.demetra.maths.matrices.ILuDecomposition;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixException;

/**
 * Represents a L-U decomposition of a matrix M = L * U where L is a lower
 * triangular matrix with 1 on the main diagonal and U is an upper triangular
 * matrix. Once a matrix has been decomposed in L * U, it can be easily used for
 * solving M x = L U x = b (solve L y = b and U x = y) or for computing the
 * determinant (product of the diagonal of U.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractLuDecomposition implements ILuDecomposition {

    double[] lu = null;
    int n, pivsign;
    int[] piv;
    double eps = EPS;

    protected double get(int r, int c) {
        return lu[c * n + r];
    }

    protected void set(int r, int c, double value) {
        lu[c * n + r] = value;
    }

    /**
     *
     */
    protected AbstractLuDecomposition() {
    }

    /**
     * @param m
     */
    void init(Matrix m) {
        if (m.getRowsCount() != m.getColumnsCount()) {
            throw new MatrixException("LU Decomposition: not squared matrix");
        }
        lu = m.internalStorage();
        n = m.getRowsCount();
        piv = new int[n];
        for (int i = 0; i < n; i++) {
            piv[i] = i;
        }
        pivsign = 1;
    }

    @Override
    public int getDimension() {
        return n;
    }

    @Override
    public Matrix lu() {
        return new Matrix(lu, n, n);
    }


    /**
     * Returns the pivoting vector.
     * @return The indexes of the rows of the initial matrix, as they are used
     * in the LU decomposition. getPivot[j] == i means : the j-th row of the LU
     * decomposition corresponds to the i-th row of the initial matrix.
     */
    public int[] getPivot() {
        return piv.clone();
    }

    //    /**
//     *
//     * @return getReversePivot[j] == i means : the i-th row of the LU
//     * decomposition corresponds to the j-th row of the initial matrix.
//    
    public int[] getReversePivot() {
        int[] rpiv = new int[n];
        for (int i = 0; i < n; ++i) {
            rpiv[piv[i]] = i;
        }
        return rpiv;

    }

    /// <summary>
    /// The method calculates the Determinant of the matrix
    /// </summary>
    /// <returns>A double value representing the determinant</returns>
    public double getDeterminant() {
        double d = (double) pivsign;
        for (int j = 0; j < n; j++) {
            d *= get(j, j);
        }
        return d;
    }

    @Override
    public void solve(DataBlock x) {
        if (n != x.getLength()) {
            throw new MatrixException("Incompatible dimensions");
        }
        // Copy right hand side with pivoting
        double[] y = new double[n];
        double[] z = new double[n];
        for (int i = 0; i < n; i++) {
            y[i] = x.get(piv[i]);
        }

        // forward substitution
        z[0] = y[0];
        for (int i = 1; i < n; i++) {
            z[i] = y[i];
            for (int j = 0; j < i; j++) {
                z[i] -= lu[j * n + i] * z[j];
            }
        }

        int n = this.n;
        for (int i = n - 1; i >= 0 && z[i] == 0.0; i--) {
            n--;
        }
        if (n == 0) {
            return;
        }

        // backward substitution
        y[n - 1] = z[n - 1] / (lu[(n - 1) * this.n + (n - 1)]);    // divided by last element
        for (int i = n - 2; i >= 0; i--) {
            double sum = z[i];
            for (int j = i + 1; j < n; j++) {
                sum -= lu[j * this.n + i] * y[j];
            }
            y[i] = sum / lu[i * this.n + i];
        }
        x.copyFrom(y, 0);
    }
}