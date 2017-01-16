/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.maths.matrices;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SubMatrixTest {

    public SubMatrixTest() {
    }

    //@Test
    public void testOldMethod() {
        ec.tstoolkit.maths.matrices.Matrix Q = ec.tstoolkit.maths.matrices.Matrix.square(0);
        long t0 = System.currentTimeMillis();
        ec.tstoolkit.maths.matrices.Matrix M1 = ec.tstoolkit.maths.matrices.Matrix.square(300);
        ec.tstoolkit.maths.matrices.Matrix M2 = ec.tstoolkit.maths.matrices.Matrix.square(300);
        M1.randomize();
        M2.randomize();
        for (int i = 0; i < 100000; ++i) {
            M1.subMatrix(1, 299, 1, 299).add(M2.subMatrix(1, 299, 1, 299).transpose());
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Old");
        System.out.println(t1 - t0);
    }

    //@Test
    public void testNewMethod() {
        Matrix Q = Matrix.square(0);
        long t0 = System.currentTimeMillis();
        Matrix M1 = Matrix.square(300);
        Matrix M2 = Matrix.square(300);
        Random rnd = new Random(0);
        M1.all().set((r, c) -> rnd.nextDouble());
        M2.all().set((r, c) -> rnd.nextDouble());
        for (int i = 0; i < 100000; ++i) {
            M1.subMatrix(1, 299, 1, 299).add(M2.subMatrix(1, 299, 1, 299).transpose());
        }
        long t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);
    }

    @Test
    public void testProduct() {
         {
            ec.tstoolkit.maths.matrices.Matrix A = new ec.tstoolkit.maths.matrices.Matrix(100, 50);
            ec.tstoolkit.maths.matrices.Matrix B = new ec.tstoolkit.maths.matrices.Matrix(50, 70);
            ec.tstoolkit.maths.matrices.Matrix C = new ec.tstoolkit.maths.matrices.Matrix(100, 70);

            A.randomize();
            B.randomize();
            long t0 = System.currentTimeMillis();
            for (int i = 0; i < 100000; ++i) {
                C.all().product(A.all(), B.all());
            }
            long t1 = System.currentTimeMillis();
            System.out.println("Old");
            System.out.println(t1 - t0);
        }
       {
            Matrix A = Matrix.of(100, 50);
            Matrix B = Matrix.of(50, 70);
            Matrix C = Matrix.of(100, 70);

            Random rnd = new Random(0);
            A.all().set((r, c) -> rnd.nextDouble());
            B.all().set((r, c) -> rnd.nextDouble());
            long t0 = System.currentTimeMillis();
            for (int i = 0; i < 100000; ++i) {
                C.all().product(A.all(), B.all());
            }
            long t1 = System.currentTimeMillis();
            System.out.println("New");
            System.out.println(t1 - t0);
        }

    }

}
