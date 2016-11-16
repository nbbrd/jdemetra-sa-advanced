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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class StlPlusTest {
    
    public StlPlusTest() {
    }

    @Test
    public void testMA() {
        
        double[] x=new double[20];
        for (int i=0; i<20; ++i)
            x[i]=i+1;
        double[] y=new double[16];
        StlPlus.movingAverage(x, 5, y);
        for (int i=1; i<y.length; ++i){
            assertEquals(y[i]-y[i-1],1, 1e-9);
        }
    }
    
}
