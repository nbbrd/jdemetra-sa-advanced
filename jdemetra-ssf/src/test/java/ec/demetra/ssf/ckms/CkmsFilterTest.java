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
package ec.demetra.ssf.ckms;

import data.Data;
import data.Models;
import ec.demetra.ssf.dk.DiffusePredictionErrorDecomposition;
import ec.demetra.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import ec.demetra.ssf.implementations.arima.SsfUcarima;
import ec.demetra.ssf.univariate.SsfData;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class CkmsFilterTest {

    public CkmsFilterTest() {
    }

    @Test
    public void test1() {
        SsfUcarima ssf = SsfUcarima.create(Models.ucmAirline(-.6, -.4));
        SsfData y = new SsfData(Data.P);
        DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(true);
        pe.prepare(ssf, y.getLength());
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(pe);
        CkmsDiffuseInitializer ff = new CkmsDiffuseInitializer(initializer);
        CkmsFilter ffilter = new CkmsFilter(ff);
        ffilter.process(ssf, y, pe);
        System.out.println(pe.likelihood().getLogLikelihood());
        System.out.println(pe.errors(true, false));
    }


    @Test
    public void stressTest() {
        SsfUcarima ssf = SsfUcarima.create(Models.ucmAirline(-.6, -.4));
        SsfData y = new SsfData(Data.P);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000; ++i) {
            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(true);
            pe.prepare(ssf, y.getLength());
            DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(pe);
            CkmsDiffuseInitializer ff = new CkmsDiffuseInitializer(initializer);
            CkmsFilter ffilter = new CkmsFilter(ff);
            ffilter.process(ssf, y, pe);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
     }

}
