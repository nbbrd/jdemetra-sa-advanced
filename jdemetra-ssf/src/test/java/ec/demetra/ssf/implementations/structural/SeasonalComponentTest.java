/*
 * Copyright 2015 National Bank of Belgium
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
/*
 */
package ec.demetra.ssf.implementations.structural;

import ec.demetra.ssf.dk.DkLikelihood;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.implementations.CompositeDynamics;
import ec.demetra.ssf.implementations.Measurement;
import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.demetra.ssf.univariate.Ssf;
import ec.demetra.ssf.univariate.SsfData;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class SeasonalComponentTest {

    private static final int N = 100000;

    public SeasonalComponentTest() {
    }

    @Test
    public void testBsm() {
        ModelSpecification spec = new ModelSpecification();
        spec.useLevel(ComponentUse.Free);
        spec.useSlope(ComponentUse.Free);
        spec.useNoise(ComponentUse.Free);
        spec.setSeasonalModel(SeasonalModel.Crude);

        BasicStructuralModel model = new BasicStructuralModel(spec, 12);
        model.setVariance(Component.Level, .1);
        model.setVariance(Component.Slope, .2);
        model.setVariance(Component.Cycle, .5);
        model.setVariance(Component.Seasonal, 2);
        model.setVariance(Component.Noise, 1);
        Ssf ssf = SsfBsm.create(model);
        SsfData ssfData = new SsfData(data.Data.X);
        DkLikelihood ll = DkToolkit.likelihoodComputer(false, true).compute(ssf, ssfData);

        CompositeDynamics dyn = new CompositeDynamics(new LocalLinearTrend.Dynamics(.1, .2),
                new SeasonalComponent.Dynamics(SeasonalModel.Crude, 2.0, 12));
        ISsfMeasurement m = Measurement.create(11, new int[]{0, 2}, 1);
        Ssf ssf2 = new Ssf(dyn, m);
        DkLikelihood ll2 = DkToolkit.likelihoodComputer(true, true).compute(ssf2, ssfData);
        assertTrue(Math.abs(ll.getLogLikelihood() - ll2.getLogLikelihood()) < 1e-6);
    }

    @Test
    @Ignore
    public void testStressLL() {
        ModelSpecification spec = new ModelSpecification();
        spec.useLevel(ComponentUse.Free);
        spec.useSlope(ComponentUse.Free);
        spec.useNoise(ComponentUse.Free);
        spec.setSeasonalModel(SeasonalModel.Crude);

        BasicStructuralModel model = new BasicStructuralModel(spec, 12);
        model.setVariance(Component.Level, .1);
        model.setVariance(Component.Slope, .2);
        model.setVariance(Component.Cycle, .5);
        model.setVariance(Component.Seasonal, 2);
        model.setVariance(Component.Noise, 1);
        Ssf ssf = SsfBsm.create(model);
        Ssf ssf2 = SsfBsm2.create(model);
        SsfData ssfData = new SsfData(data.Data.X);

        CompositeDynamics dyn = new CompositeDynamics(new LocalLinearTrend.Dynamics(.1, .2),
                new SeasonalComponent.Dynamics(SeasonalModel.Crude, 2.0, 12));
        ISsfMeasurement m = Measurement.create(11, new int[]{0, 2}, 1);
        Ssf ssf3 = new Ssf(dyn, m);
        double l = 0;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DkLikelihood ll = DkToolkit.likelihoodComputer().compute(ssf, ssfData);
            l = ll.getLogLikelihood();
        }
        long t1 = System.currentTimeMillis();
        System.out.println("bsm");
        System.out.println(t1 - t0);
        System.out.println(l);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DkLikelihood ll = DkToolkit.likelihoodComputer().compute(ssf2, ssfData);
            l = ll.getLogLikelihood();
        }
        t1 = System.currentTimeMillis();
        System.out.println("Bsm2");
        System.out.println(t1 - t0);
        System.out.println(l);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DkLikelihood ll = DkToolkit.likelihoodComputer().compute(ssf3, ssfData);
            l = ll.getLogLikelihood();
        }
        t1 = System.currentTimeMillis();
        System.out.println("composite");
        System.out.println(t1 - t0);
        System.out.println(l);
    }

}
