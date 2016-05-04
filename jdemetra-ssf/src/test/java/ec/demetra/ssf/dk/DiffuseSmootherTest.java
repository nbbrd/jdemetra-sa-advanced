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
package ec.demetra.ssf.dk;

import data.Data;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.demetra.ssf.akf.AkfToolkit;
import ec.demetra.ssf.implementations.arima.SsfUcarima;
import ec.demetra.ssf.univariate.DefaultSmoothingResults;
import ec.demetra.ssf.univariate.SsfData;
import ec.tstoolkit.data.DataBlock;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class DiffuseSmootherTest {

    static final int N = 50;
    static final UcarimaModel ucm;
    static final SsfData data;

    static {
        TrendCycleSelector tsel = new TrendCycleSelector(.5);
        tsel.setDefaultLowFreqThreshold(12);
        SeasonalSelector ssel = new SeasonalSelector(12, 3);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);
        TsData x = Data.X.clone();
        int[] missing = new int[N];
        Random rng = new Random(0);
        for (int i = 0; i < N; ++i) {
            missing[i] = rng.nextInt(x.getLength());
        }
        SarimaModel arima = new SarimaModelBuilder().createAirlineModel(12, -.8, -.9);
        ucm = decomposer.decompose(ArimaModel.create(arima));
        ucm.setVarianceMax(-1);
        ucm.simplify();

//        for (int i = 0; i < N; ++i) {
//            x.setMissing(missing[i]);
//        }
        data = new SsfData(x);
    }

    public DiffuseSmootherTest() {
    }

    @Test
    public void testSmoothing() {


        SsfUcarima ssf = SsfUcarima.create(ucm);
        DefaultSmoothingResults srslts = DkToolkit.smooth(ssf, data, true);
        DefaultSmoothingResults srslts2 = DkToolkit.sqrtSmooth(ssf, data, true);
        DefaultSmoothingResults srslts3 = AkfToolkit.smooth(ssf, data, true);
        assertTrue(srslts.getComponent(ssf.getComponentPosition(0)).distance(srslts2.getComponent(0)) < 1e-6);
        assertTrue(srslts.getComponent(0).distance(srslts3.getComponent(0)) < 1e-6);
        System.out.println(srslts.getComponentVariance(ssf.getComponentPosition(0)));
        System.out.println(srslts.getComponentVariance(ssf.getComponentPosition(1)));
        System.out.println(srslts.getComponentVariance(ssf.getComponentPosition(2)));
        
        // old 
        ec.tstoolkit.ssf.Smoother osm=new ec.tstoolkit.ssf.Smoother();
        ec.tstoolkit.ssf.ucarima.SsfUcarima ossf=new ec.tstoolkit.ssf.ucarima.SsfUcarima(ucm);
        osm.setCalcVar(true);
        osm.setSsf(ossf);
        DataBlock d=new DataBlock(data);
        ec.tstoolkit.ssf.SsfData odata=new ec.tstoolkit.ssf.SsfData(d.getData(), null);
        ec.tstoolkit.ssf.SmoothingResults sr=new ec.tstoolkit.ssf.SmoothingResults(true, true);
        osm.process(odata, sr);
        System.out.println(new DataBlock(sr.componentStdev(0)));
        System.out.println(new DataBlock(sr.componentStdev(3)));
        System.out.println(new DataBlock(sr.componentStdev(15)));
    }

    @Test
    @Ignore
    public void stressTestSmoothing() {
        int K=5000;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            SsfUcarima ssf = SsfUcarima.create(ucm);
            DefaultSmoothingResults srslts = AkfToolkit.smooth(ssf, data, true);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Akf smoother");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            SsfUcarima ssf = SsfUcarima.create(ucm);
            DefaultSmoothingResults srslts = DkToolkit.smooth(ssf, data, true);
        }
        t1 = System.currentTimeMillis();
        System.out.println("DK smoother");
        System.out.println(t1 - t0);
        // old implementation

        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            SsfUcarima ssf = SsfUcarima.create(ucm);
            DefaultSmoothingResults srslts = DkToolkit.sqrtSmooth(ssf, data, true);
        }
        t1 = System.currentTimeMillis();
        System.out.println("SQRT smoother");
        System.out.println(t1 - t0);
    }
}
