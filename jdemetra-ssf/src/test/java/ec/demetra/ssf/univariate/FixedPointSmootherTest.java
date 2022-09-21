/*
 * Copyright 2016-2017 National Bank of Belgium
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
package ec.demetra.ssf.univariate;

import data.Models;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.demetra.ssf.implementations.arima.SsfUcarima;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FixedPointSmootherTest {
    
    public FixedPointSmootherTest() {
    }

    @Test
    @Disabled
    public void testUcarima() {
        long t0=System.currentTimeMillis();
        FixedPointSmoother smoother=new FixedPointSmoother(data.Models.ssfUcarima, 60);
        for (int i=0; i<1000; ++i){
        smoother.process(new SsfData(data.Data.P));
        }
        long t1=System.currentTimeMillis();
        System.out.println(t1-t0);
//        System.out.println(smoother.getResults().getComponentVariance(0));
//        System.out.println(smoother.getResults().getComponent(0));
    }
    
    @Test
    public void testVariance(){
        UcarimaModel ucm = Models.ucmAirline(-.6, -.4);
        SsfUcarima ssf=SsfUcarima.create(ucm);
         FixedPointSmoother smoother=new FixedPointSmoother(ssf, 120);
        smoother.process(new SsfData(data.Data.P));
//        System.out.println(smoother.getResults().getComponentVariance(ssf.getComponentPosition(0)));
//        System.out.println(smoother.getResults().getComponentVariance(ssf.getComponentPosition(1)));
//        System.out.println(smoother.getResults().getComponentVariance(ssf.getComponentPosition(2)));
    }
    
}
