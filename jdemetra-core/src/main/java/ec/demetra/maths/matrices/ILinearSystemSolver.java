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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.ServiceDefinition;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixException;
import ec.tstoolkit.maths.matrices.SubMatrix;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Defines algorithms to solve linear system
 * The system contains n equations with n unknowns
 * It is defined by Ax=B
 * A unique solution exists iff A is invertible
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@ServiceDefinition
public interface ILinearSystemSolver {

    static final double EPS = Math.pow(2.0, -52.0);

    static AtomicReference<Supplier<ILuDecomposition>> FACTORY = new AtomicReference<>();

    /**
     * @param m
     */
    void decompose(Matrix m);

    /**
     * @param m
     */
    default void decompose(SubMatrix m){
        decompose(new Matrix(m));
    }
    /**
     * @return n, the dimension of the system
     */
    int getDimension();

    /**
     * Solves Ax=b
     *
     * @param z On entry, the datablock contains the right terms of the system (b).
     *          On exit, it contains the results of the system (x).
     * @throws MatrixException
     */
    void solve(DataBlock z) throws MatrixException;

    /**
     * Solves AX=B
     *
     * @param m On entry, the sub-matrix contains the right terms of the system (B).
     *          On exit, it contains the results of the system (X).
     */
    default void solve(SubMatrix m) {
        DataBlockIterator columns = m.columns();
        DataBlock data = columns.getData();
        do {
            solve(data);
        } while (columns.next());
    }
}