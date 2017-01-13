/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.data;

import ec.tstoolkit.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ArrayOfDoublesTest {

    public ArrayOfDoublesTest() {
    }

    @Test
    @Ignore
    public void stressTest() {
        DataBlock xx = new DataBlock(1000);
        IArrayOfDoubles xxx = ArrayOfDoubles.create(1000);

        long t0 = System.currentTimeMillis();
        {
            double[] X = new double[100];
            double[] Y = new double[100];
            IArrayOfDoubles x = ArrayOfDoubles.of(X).reverse();
            IArrayOfDoubles y = ArrayOfDoubles.of(Y);
            x.set(j -> j * j);
            y.set(() -> 45);
            for (long i = 0; i < 100000000; ++i) {
                y.addAY(i%2 == 0 ? 25 : -25, x);
            }
            System.out.println(y.sum());
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        {
            double[] X = new double[100];
            double[] Y = new double[100];
            DataBlock x = new DataBlock(X).reverse();
            DataBlock y = new DataBlock(Y);
            x.set(j -> j * j);
            y.set(45);
            for (long i = 0; i < 100000000; ++i) {
                y.addAY(i%2 == 0 ? 25 : -25, x);
            }
            System.out.println(y.sum());
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
