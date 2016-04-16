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
package ec.demetra.ssf.multivariate;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
public class MultivariateUpdateInformation  {

    /**
     * U is the transformed prediction error (=L^-1)*(y(t)-Z(t)A(t)))
     * U is 1 x nvars
     */
    private final DataBlock U;

    /**
     * =(ZPZ'+H)^1/2 Cholesky factor of the variance/covariance matrix of the
     * prediction errors (lower triangular).
     * nvars x nvars
     */
    private final Matrix L;

    /**
     * K = P Z' L'^-1
     * dim x nvars
     */
    private final Matrix K;

    /**
     *
     * @param dim
     * @param nvars
     */
    public MultivariateUpdateInformation(final int dim, final int nvars) {
        U = new DataBlock(nvars);
        L = new Matrix(nvars, nvars);
        K = new Matrix(dim, nvars);
    }
    
    public DataBlock getTransformedPredictionErrors() {
        return U;
    }
    
    public Matrix getPredictionErrorCovariance() {
        if (L.getRowsCount() == 1) {
            double l = L.get(0, 0);
            return new Matrix(new double[]{l * l}, 1, 1);
        } else {
            return SymmetricMatrix.LLt(L);
        }
    }
   
    public Matrix getCholeskyFactor() {
        return L;
    }

    /**
     * @return the K
     */
    public Matrix getK() {
        return K;
    }
    
}
