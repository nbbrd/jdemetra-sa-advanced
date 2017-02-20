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

import data.WeeklyData;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class StlPlusSpecificationTest {
    
    public StlPlusSpecificationTest() {
    }

    @Test
    public void testDefault() {
        IReadDataBlock data=new DataBlock(WeeklyData.US_Claims);
        // Creates a default stl specification
        StlPlusSpecification spec = StlPlusSpecification.createDefault(52, true);
        StlPlus stl = spec.build();
        stl.process(data);
        Matrix m=new Matrix(data.getLength(), 4);
        m.column(0).copyFrom(stl.getY(), 0);
        m.column(1).copyFrom(stl.getTrend(), 0);
        m.column(2).copyFrom(stl.getSeason(0), 0);
        m.column(3).copyFrom(stl.getIrr(), 0);
//        m.all().rows().forEach(r->assertEquals(r.get(0), r.extract(1, 3).sum(), 1e-9));
        System.out.println(m);
    }
    
    @Test
    public void testDefaultMul() {
        IReadDataBlock data=new DataBlock(WeeklyData.US_Claims);
        // Creates a default stl specification
        StlPlusSpecification spec = StlPlusSpecification.createDefault(52, true);
        spec.setMultiplicative(true);
        StlPlus stl = spec.build();
        
        stl.process(data);
        Matrix m=new Matrix(data.getLength(), 4);
        m.column(0).copyFrom(stl.getY(), 0);
        m.column(1).copyFrom(stl.getTrend(), 0);
        m.column(2).copyFrom(stl.getSeason(0), 0);
        m.column(3).copyFrom(stl.getIrr(), 0);
        System.out.println(m);
    }
    
    @Test
    public void testCustom() {
        IReadDataBlock data=new DataBlock(WeeklyData.US_Claims);
        // Creates an empty robust stl specification (robust means 15 outer loops, 1 inner loop).
        StlPlusSpecification spec = new StlPlusSpecification(true);
        // We put the seasonal specification
        SeasonalSpecification sspec=new SeasonalSpecification(52, LoessSpecification.defaultSeasonal(9), LoessSpecification.defaultLowPass(52));
        spec.add(sspec);
        // Trend specification
        spec.setTrendSpec( LoessSpecification.of(105, 1, 1, null));
        spec.setMultiplicative(true);
        StlPlus stl = spec.build();
        stl.process(data);
        
        Matrix m=new Matrix(data.getLength(), 4);
        m.column(0).copyFrom(stl.getY(), 0);
        m.column(1).copyFrom(stl.getTrend(), 0);
        m.column(2).copyFrom(stl.getSeason(0), 0);
        m.column(3).copyFrom(stl.getIrr(), 0);
        System.out.println("specific processing");
        System.out.println(m);
    }

}
