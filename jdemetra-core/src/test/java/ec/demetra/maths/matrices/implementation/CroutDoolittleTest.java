package ec.demetra.maths.matrices.implementation;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CroutDoolittleTest {
    @SuppressWarnings("SuspiciousNameCombination")
    @Test
    public void decompose() throws Exception {
       CroutDoolittle cd=new CroutDoolittle();
       Gauss gauss=new Gauss();
        int n=10;
        Matrix M = Matrix.square(10);
        M.randomize(0);
        Matrix N=M.clone();
        cd.decompose(M);
        gauss.decompose(N);
        DataBlock x=new DataBlock(n);
        x.randomize();
        DataBlock y=x.deepClone();
        gauss.solve(x);
        cd.solve(y);
        assertTrue(y.distance(x)<1e-9);
//        System.out.println(x);
//        System.out.println(y);
    }
}