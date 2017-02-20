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
import ec.demetra.timeseries.simplets.TsData;
import java.util.Random;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class StlPlusTest {

    public StlPlusTest() {
    }

    @Test
//    @Ignore
    public void testDefault() {

        StlPlus stl = new StlPlus(12, 7);
        stl.process(Data.X);
//        System.out.println(new DataBlock(stl.trend));
//        System.out.println(new DataBlock(stl.season[0]));
//        System.out.println(new DataBlock(stl.irr));
    }

    @Test
//    @Ignore
    public void testLargeFilter() {

        StlPlus stl = new StlPlus(12, 21);
        stl.process(Data.X);
//        System.out.println(new DataBlock(stl.trend));
//        System.out.println(new DataBlock(stl.season[0]));
//        System.out.println(new DataBlock(stl.irr));
    }


    @Test
    //@Ignore
    public void testSpec() {

        StlPlusSpecification spec = StlPlusSpecification.createDefault(12, false);
        StlPlus stl = spec.build();
        stl.process(Data.X);
//        System.out.println(new DataBlock(stl.trend));
//        System.out.println(new DataBlock(stl.season[0]));
//        System.out.println(new DataBlock(stl.irr));
    }

    @Test
//    @Ignore
    public void testMul() {

        StlPlusSpecification spec = StlPlusSpecification.createDefault(12, false);
        spec.setMultiplicative(true);
        spec.setNumberOfOuterIterations(5);
        StlPlus stl = spec.build();
        stl.process(Data.X);
//        System.out.println(new DataBlock(stl.trend));
//        System.out.println(new DataBlock(stl.season[0]));
//        System.out.println(new DataBlock(stl.irr));
    }

    @Test
//    @Ignore
    public void testMissing() {

        StlPlusSpecification spec = StlPlusSpecification.createDefault(12, false);
        spec.setMultiplicative(true);
        spec.setNumberOfOuterIterations(5);
        StlPlus stl = spec.build();
        TsData s = Data.X.clone();
        Random rnd = new Random();
        for (int i = 0; i < 30; ++i) {
            s.set(rnd.nextInt(s.getLength()), Double.NaN);
        }
        stl.process(s);
//        System.out.println(new DataBlock(stl.trend));
//        System.out.println(new DataBlock(stl.season[0]));
//        System.out.println(new DataBlock(stl.irr));
    }

    @Test
      @Ignore
    public void stressTest() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
//            StlPlus stl = new StlPlus(12, 7);
            StlPlusSpecification spec = StlPlusSpecification.createDefault(12, 7, false);
            spec.setNumberOfOuterIterations(5);
            StlPlus stl = spec.build();
            stl.process(Data.X);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
