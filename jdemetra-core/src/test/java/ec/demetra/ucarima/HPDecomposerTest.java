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
package ec.demetra.ucarima;

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.ssf.ucarima.SsfUcarima;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class HPDecomposerTest {
    
    public HPDecomposerTest() {
    }

    @Test
    public void testAirline() {
        // compute airline decomposition
        UcarimaModel ucm=ucmAirline(-.6, -.8);
        HPDecomposer decomposer=new HPDecomposer();
        ArimaModel tc = ucm.getComponent(0);
        decomposer.decompose(tc);
        assertTrue(tc.minus(decomposer.getTrend().plus(decomposer.getCycle())).isNull());
//        System.out.println(decomposer.getTrend());
//        System.out.println(decomposer.getCycle());
    }
    
    @Test
    public void testHighLambda() {
        // compute airline decomposition
        UcarimaModel ucm=ucmAirline(-.6, -.8);
        HPDecomposer decomposer=new HPDecomposer();
        decomposer.setLambda(1.2e9);
        ArimaModel tc = ucm.getComponent(0);
        decomposer.decompose(tc);
        assertTrue(tc.minus(decomposer.getTrend().plus(decomposer.getCycle())).isNull());
//        System.out.println(decomposer.getTrend());
//        System.out.println(decomposer.getCycle());
    }

    @Test
    public void testSmallDifferencing() {
        UcarimaModel ucm=ucmAR(-.8, -.5);
        HPDecomposer decomposer=new HPDecomposer();
        ArimaModel tc = ucm.getComponent(0);
        decomposer.decompose(tc);
        assertTrue(tc.minus(decomposer.getTrend().plus(decomposer.getCycle())).isNull());
//        System.out.println(decomposer.getTrend());
//        System.out.println(decomposer.getCycle());
    }
    
    @Test
    public void testParameters(){
        HPDecomposer decomposer=new HPDecomposer();
        double lambda=1600;
        decomposer.setLambda(lambda);
        double tau=decomposer.getTau();
        decomposer.setTau(tau);
        assertEquals(lambda, decomposer.getLambda(), lambda*1e-6);
    }
    
    static UcarimaModel ucmAirline(double th, double bth) {
        SarimaModel sarima = new SarimaModelBuilder().createAirlineModel(12, th, bth);
        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(12);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(ArimaModel.create(sarima));
        double var = ucm.setVarianceMax(-1, false);
        return ucm;
    }

    static UcarimaModel ucmAR(double ar, double bth) {
        SarimaSpecification spec=new SarimaSpecification(12);
        spec.setBD(1);
        spec.setP(1);
        spec.setBQ(1);
        SarimaModel sarima=new SarimaModel(spec);
        sarima.setPhi(1, ar);
        sarima.setBTheta(1, bth);
        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(12);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(ArimaModel.create(sarima));
        double var = ucm.setVarianceMax(-1, false);
        return ucm;
    }
}
