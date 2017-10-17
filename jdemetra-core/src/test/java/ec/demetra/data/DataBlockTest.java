/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.data;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DataBlockTest {

    public DataBlockTest() {
    }

    @Test
    //@Ignore
    public void stressTest() {
        ec.tstoolkit.data.DataBlock xx = new ec.tstoolkit.data.DataBlock(1000);
        DataBlock xxx = DataBlock.create(1000);

        long t0 = System.currentTimeMillis();
        {
            double[] X = new double[100];
            double[] Y = new double[100];
            IArrayOfDoubles x = DataBlock.of(X).reverse();
            IArrayOfDoubles y = DataBlock.of(Y);
            x.set(j -> j * j);
            y.set(() -> 45);
            for (long i = 0; i < 100000000; ++i) {
                y.addAY(i % 2 == 0 ? 25 : -25, x);
            }
            System.out.println(y.sum());
        }
        long t1 = System.currentTimeMillis();
        System.out.println("new");
        System.out.println(t1 - t0);

        t0 = System.currentTimeMillis();
        {
            double[] X = new double[100];
            double[] Y = new double[100];
            ec.tstoolkit.data.DataBlock x = new ec.tstoolkit.data.DataBlock(X).reverse();
            ec.tstoolkit.data.DataBlock y = new ec.tstoolkit.data.DataBlock(Y);
            x.set(j -> j * j);
            y.set(45);
            for (long i = 0; i < 100000000; ++i) {
                y.addAY(i % 2 == 0 ? 25 : -25, x);
            }
            System.out.println("old");
            System.out.println(y.sum());
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
