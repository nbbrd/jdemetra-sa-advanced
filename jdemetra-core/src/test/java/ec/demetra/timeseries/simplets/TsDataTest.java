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
package ec.demetra.timeseries.simplets;

import data.Data;
import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class TsDataTest {
    
    public TsDataTest() {
    }

    @Test
    @Ignore
    public void testDisplay() {
        for (TsObservation obs:Data.P){
            System.out.print(obs.getPeriod().firstDay());
            System.out.print('\t');
            System.out.print(obs.getPeriod().lastDay());
            System.out.print('\t');
            System.out.println(obs.getValue());
        }
    }
    
    @Test
    public void testFunctional(){
        TsData s=new TsData(TsFrequency.Monthly, 1990, 0, 20);
        s.set(i->1-.4*i+.04*i*i);
        Optional<TsObservation> min = s.stream().min((x,y)->Double.compare(x.getValue(), y.getValue()));
        assertTrue(min.get().getPeriod().minus(s.getStart())==5);
        assertTrue(min.get().getValue()==0);
    }
   
}
