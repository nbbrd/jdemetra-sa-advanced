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

import static ec.demetra.ucarima.HPDecomposerTest.ucmAirline;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
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
public class TrendCycleDecomposerTest {
    
    public TrendCycleDecomposerTest() {
    }

    @Test
    public void testAirline() {
        // compute airline decomposition
        UcarimaModel ucm=ucmAirline(-.6, -.8);
        TrendCycleDecomposer decomposer=new TrendCycleDecomposer();
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
        TrendCycleDecomposer decomposer=new TrendCycleDecomposer();
        decomposer.setLambda(1.2e9);
        ArimaModel tc = ucm.getComponent(0);
        decomposer.decompose(tc);
        assertTrue(tc.minus(decomposer.getTrend().plus(decomposer.getCycle())).isNull());
//        System.out.println(decomposer.getTrend());
//        System.out.println(decomposer.getCycle());
    }

    @Test
    public void testFullDifferencing() {
        UcarimaModel ucm=ucmD2(-.8, -.5);
        TrendCycleDecomposer decomposer=new TrendCycleDecomposer();
        ArimaModel tc = ucm.getComponent(0);
        decomposer.decompose(tc);
        assertTrue(tc.minus(decomposer.getTrend().plus(decomposer.getCycle())).isNull());
        System.out.println(decomposer.getTrend());
        System.out.println(decomposer.getCycle());
    }
    
    static UcarimaModel ucmD2(double th, double bth) {
        SarimaSpecification spec=new SarimaSpecification(12);
        spec.airline();
        spec.setD(2);
        SarimaModel sarima=new SarimaModel(spec);
        sarima.setTheta(1, th);
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
    
    @Test
    public void testParameters(){
        TrendCycleDecomposer decomposer=new TrendCycleDecomposer();
        decomposer.setDifferencing(2);
        double lambda=1600;
        decomposer.setLambda(lambda);
        double tau=decomposer.getTau();
        decomposer.setTau(tau);
        assertEquals(lambda, decomposer.getLambda(), lambda*1e-6);
    }
}
