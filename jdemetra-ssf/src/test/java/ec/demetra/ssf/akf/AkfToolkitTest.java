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
package ec.demetra.ssf.akf;

import ec.demetra.eco.ILikelihood;
import ec.demetra.ssf.implementations.arima.SsfArima;
import ec.tstoolkit.arima.ArimaModelBuilder;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.demetra.ssf.univariate.SsfData;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class AkfToolkitTest {
    
    private static final int N = 100000, M = 50000;
    static final SarimaModel model;
    static final double[] data;

    static {
        SarimaModelBuilder builder = new SarimaModelBuilder();
        model = builder.createArimaModel(12, 3, 1, 1, 0, 1, 1);
        model.setBTheta(1, -.99);
        //model = builder.createAirlineModel(12, -.6, -.8);
        ArimaModelBuilder gbuilder = new ArimaModelBuilder();
        data = gbuilder.generate(model, 200);
        data[5] = Double.NaN;
        data[12] = Double.NaN;
        data[21] = Double.NaN;
    }
    
    public AkfToolkitTest() {
    }

    @Test
    public void testLikelihood() {
        SsfArima ssf = SsfArima.create(model);
        SsfData ssfData = new SsfData(data);
        DiffuseLikelihood ll = (DiffuseLikelihood) AkfToolkit.likelihoodComputer(false).compute(ssf, ssfData);
     }

    @Test
    public void testCollapsing() {
        SsfArima ssf = SsfArima.create(model);
        SsfData ssfData = new SsfData(data);
        DiffuseLikelihood ll = AkfToolkit.likelihoodComputer(true).compute(ssf, ssfData);
    }
    
    @Ignore
    @Test
    public void testStressLikelihood() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < M; ++i) {
            SsfArima ssf = SsfArima.create(model);
            ILikelihood ll = AkfToolkit.likelihoodComputer(false).compute(ssf, new SsfData(data));
        }
        long t1 = System.currentTimeMillis();
        System.out.println("AKF");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < M; ++i) {
            SsfArima ssf = SsfArima.create(model);
            ILikelihood ll = AkfToolkit.likelihoodComputer(true).compute(ssf, new SsfData(data));
        }
        t1 = System.currentTimeMillis();
        System.out.println("AKF with collapsing");
        System.out.println(t1 - t0);
    }

}
