/*
* Copyright 2013 National Bank of Belgium
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

package ec.demetra.maths.matrices;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import lombok.NonNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Computes the L-U decomposition of a matrix
 * M = L * U where L is a lower triangular matrix with 1 on the main diagonal
 * and U is an upper triangular matrix.
 * Once a matrix has been decomposed in L * U, 
 * it can be easily used for solving 
 * M x = L U x = b
 * (solve L y = b and U x = y)
 * or for computing the determinant (product of the diagonal of U.
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ILuDecomposition extends ILinearSystemSolver {

    static void setFastImplementation(@NonNull Supplier<ILuDecomposition> impl) {
        ILuDecomposition_Factories.FAST_FACTORY.set(impl);
    }

    static void setRobustImplementation(@NonNull Supplier<ILuDecomposition> impl) {
        ILuDecomposition_Factories.ROBUST_FACTORY.set(impl);

    }

    static ILuDecomposition getDefault(){
        Supplier<ILuDecomposition> factory = ILuDecomposition_Factories.ROBUST_FACTORY.get();
        if (factory != null)
            return factory.get();
        else return fast();
    }


    static ILuDecomposition fast() {
        return ILuDecomposition_Factories.FAST_FACTORY.get().get();
    }

    static ILuDecomposition robust() {
        return ILuDecomposition_Factories.ROBUST_FACTORY.get().get();
    }


    /**
     * Gets the LU decomposition.
     * @return The returned matrix contains in its Upper part the U matrix and in its lower part the L matrix (whose main diagonal is 1)
     */
    Matrix lu();
}

 class ILuDecomposition_Factories
 {
     static AtomicReference<Supplier<ILuDecomposition>> FAST_FACTORY=new AtomicReference<>();
     static AtomicReference<Supplier<ILuDecomposition>> ROBUST_FACTORY=new AtomicReference<>();

 }

