/*
 * Copyright 2013-2014 National Bank of Belgium
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

import data.Data;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.demetra.ssf.implementations.arima.SsfUcarima;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class OrdinarySmootherTest {

    private final static SarimaModel m1, m2;
    private final static ISsfData data;
    private final static TsData ds;

    static {
        SarimaSpecification spec1 = new SarimaSpecification(12);
        spec1.setBQ(1);
        spec1.setQ(1);

        SarimaSpecification spec2 = new SarimaSpecification(12);
        spec2.setBP(1);
        spec2.setP(3);

        m1 = new SarimaModel(spec1);
        m2 = new SarimaModel(spec2);

        ds = Data.P.drop(0, 120).delta(12);
        data = new SsfData(ds);
    }

    public OrdinarySmootherTest() {
    }

    int N = 10000;

    @Test
    @Disabled
    public void testSmoother() {

        UcarimaModel ucm = new UcarimaModel();
        ucm.addComponent(ArimaModel.create(m1));
        ucm.addComponent(ArimaModel.create(m2));
        
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            SsfUcarima ssf = SsfUcarima.create(ucm);
            OrdinaryFilter filter = new OrdinaryFilter();
            DefaultFilteringResults frslts = DefaultFilteringResults.full();
            filter.process(ssf, data, frslts);
            OrdinarySmoother smoother = new OrdinarySmoother();
            smoother.setCalcVariances(false);
            smoother.process(ssf, frslts);

        }
        long t1 = System.currentTimeMillis();
        System.out.println("New smoother");
        System.out.println(t1 - t0);
    }
}
