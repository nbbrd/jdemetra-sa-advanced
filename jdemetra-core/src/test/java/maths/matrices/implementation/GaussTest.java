package ec.demetra.maths.matrices.implementation;

import ec.demetra.maths.matrices.ILinearSystemSolver;
import ec.demetra.maths.matrices.ILuDecomposition;
import ec.tstoolkit.maths.matrices.Matrix;
import org.junit.Test;

import static org.junit.Assert.*;

public class GaussTest {

    @Test
    public void decompose() throws Exception {
        ILuDecomposition.setRobustImplementation(() -> new Gauss());
        Matrix M = Matrix.square(10);
        M.randomize(0);
        ILuDecomposition.robust().decompose(M);
    }
}