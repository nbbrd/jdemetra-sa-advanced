/*
 * Copyright 2016 National Bank of Belgium
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
package be.nbb.demetra.stl;

import data.Data;
import ec.tstoolkit.data.DataBlock;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class StlTest {

    public StlTest() {
    }

    @Test
    public void testDefault() {
        StlSpecification spec = StlSpecification.defaultSpec(12, 7, false);
        Stl stl = new Stl(spec);
        spec.setNo(5);
        stl.process(Data.X);
        System.out.println(new DataBlock(stl.trend));
        System.out.println(new DataBlock(stl.season));
        System.out.println(new DataBlock(stl.irr));
    }

    @Test
    public void testMul() {
        StlSpecification spec = StlSpecification.defaultSpec(12, 7, false);
        spec.setMultiplicative(true);
        Stl stl = new Stl(spec);
        spec.setNo(5);
        stl.process(Data.X);
        System.out.println(new DataBlock(stl.trend));
        System.out.println(new DataBlock(stl.season));
        System.out.println(new DataBlock(stl.irr));
    }

    @Test
    @Ignore
    public void stressTest() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            StlSpecification spec = StlSpecification.defaultSpec(12, 7, false);
            spec.setNo(5);
            Stl stl = new Stl(spec);
            stl.process(Data.X);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    @Ignore
    public void testInner() {
        StlSpecification spec = StlSpecification.defaultSpec(12, 9, true);
        Stl stl = new Stl(spec);
        stl.process(Data.X);
        System.out.println(new DataBlock(stl.trend));
        System.out.println(new DataBlock(stl.season));
        System.out.println(new DataBlock(stl.irr));
    }

//    @Test
//    public void testMA() {
//
//        double[] x = new double[20];
//        for (int i = 0; i < 20; ++i) {
//            x[i] = i + 1;
//        }
//        double[] y = new double[16];
//        StlPlus.stlma(5, x.length, x, y);
//        for (int i = 1; i < y.length; ++i) {
//            assertEquals(y[i] - y[i - 1], 1, 1e-9);
//        }
//    }
//
//    @Test
//    public void testFTS() {
//        int np = 12;
//        int n = 240;
//        SarimaModel airline = new SarimaModelBuilder().createAirlineModel(np, -.6, -.8);
//        ArimaModelBuilder builder = new ArimaModelBuilder();
//        double[] x = builder.generate(airline, n);
//        double[] y = new double[x.length - 2 * np];
//        StlPlus.stlfts(np, x.length, x, y);
////        System.out.println(new DataBlock(x));
////        System.out.println(new DataBlock(y));
//    }
//
//    @Test
//    public void testEst() {
//        int np = 12;
//        int n = 240;
//        SarimaModel airline = new SarimaModelBuilder().createAirlineModel(np, -.6, -.8);
//        ArimaModelBuilder builder = new ArimaModelBuilder();
//        double[] x = builder.generate(airline, n);
//        double ys = StlPlus.stlest(i->x[i], n, 13, 1, 65, 65-6, 65+6, null);
////        System.out.println(ys);
////        System.out.println(new DataBlock(x, 59, 72, 1));
//    }
//    
//    @Test
//    public void testEss() {
//        int np = 12;
//        int n = 120;
//        SarimaModel airline = new SarimaModelBuilder().createAirlineModel(np, -.6, -.8);
//        ArimaModelBuilder builder = new ArimaModelBuilder();
//        double[] x = builder.generate(airline, n);
//        double[] y =new double[n];
//        double[] z =new double[n];
//        StlPlus.stless(i->x[i], n, np+1, 1, 1, null, y);
//        StlPlus.stless(i->x[i], n, np+1, 1, 5, null, z);
////        System.out.println(new DataBlock(x));
////        System.out.println(new DataBlock(y));
////        System.out.println(new DataBlock(z));
//    }
//    
//    @Test
//    public void testss() {
//        int np = 12;
//        int n = 119;
//        SarimaModel airline = new SarimaModelBuilder().createAirlineModel(np, -.6, -.8);
//        ArimaModelBuilder builder = new ArimaModelBuilder();
//        double[] x = builder.generate(airline, n);
//        double[] y=new double[x.length+2*np];
//        StlPlus.stlss(i->x[i], n, 12, 5, 1, 1, null, y);
//        System.out.println(new DataBlock(y));
//        System.out.println(new DataBlock(x));
//    }
}
