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
package be.nbb.demetra.sts;

import data.Models;
import ec.demetra.ssf.akf.AkfToolkit;
import ec.demetra.ssf.ckms.CkmsToolkit;
import ec.demetra.ssf.dk.DkLikelihood;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.implementations.CompositeDynamics;
import ec.demetra.ssf.implementations.Measurement;
import ec.demetra.ssf.implementations.structural.Component;
import ec.demetra.ssf.implementations.structural.ComponentUse;
import ec.demetra.ssf.implementations.structural.CyclicalComponent;
import ec.demetra.ssf.implementations.structural.LocalLinearTrend;
import ec.demetra.ssf.implementations.structural.SeasonalComponent;
import ec.demetra.ssf.implementations.structural.SeasonalModel;
import ec.demetra.ssf.univariate.DefaultSmoothingResults;
import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.demetra.ssf.univariate.Ssf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SsfBsmTest {

    static final int N = 100000;

    final SsfBsm bsm;

    public SsfBsmTest() {
        ModelSpecification mspec = new ModelSpecification();
        mspec.setSeasonalModel(SeasonalModel.HarrisonStevens);
        BasicStructuralModel model = new BasicStructuralModel(mspec, 12);
        bsm = SsfBsm.create(model);
    }

    @Test
    public void testBsm() {
//        System.out.println("DK");
//        System.out.println(DkToolkit.likelihoodComputer().compute(bsm, Models.ssfProd));
//        System.out.println("Fast filter");
//        System.out.println(CkmsToolkit.likelihoodComputer().compute(bsm, Models.ssfProd));
//        System.out.println("AKF");
//        System.out.println(AkfToolkit.likelihoodComputer().compute(bsm, Models.ssfProd));
        double ll1 = DkToolkit.likelihoodComputer().compute(bsm, Models.ssfProd).getLogLikelihood();
        double ll2 = AkfToolkit.likelihoodComputer().compute(bsm, Models.ssfProd).getLogLikelihood();
        double ll3 = CkmsToolkit.likelihoodComputer().compute(bsm, Models.ssfProd).getLogLikelihood();
        assertEquals(ll1, ll2, 1e-6);
        assertEquals(ll1, ll3, 1e-6);
    }

    @Test
    @Ignore
    public void stressTestBsm() {
        testBsm();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DkToolkit.likelihoodComputer().compute(bsm, Models.ssfX);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("dk filter");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            AkfToolkit.likelihoodComputer(true).compute(bsm, Models.ssfX);
        }
        t1 = System.currentTimeMillis();
        System.out.println("akf filter");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            CkmsToolkit.likelihoodComputer().compute(bsm, Models.ssfX);
        }
        t1 = System.currentTimeMillis();
        System.out.println("ckms filter");
        System.out.println(t1 - t0);
    }

    @Test
    public void testCompositeBsm() {
        ModelSpecification spec = new ModelSpecification();
        spec.useLevel(ComponentUse.Free);
        spec.useSlope(ComponentUse.Free);
        spec.useNoise(ComponentUse.Free);
        spec.useCycle(ComponentUse.Free);
        spec.setSeasonalModel(SeasonalModel.HarrisonStevens);

        BasicStructuralModel model = new BasicStructuralModel(spec, 12);
        model.setVariance(Component.Level, .1);
        model.setVariance(Component.Slope, .2);
        model.setVariance(Component.Cycle, .5);
        model.setVariance(Component.Seasonal, 2);
        model.setVariance(Component.Noise, 1);
        model.setCycle(.9, 8);
        Ssf ssf = SsfBsm.create(model);
        DkLikelihood ll = (DkLikelihood) DkToolkit.likelihoodComputer(false, true).compute(ssf, Models.ssfX);

        CompositeDynamics dyn = new CompositeDynamics(new CyclicalComponent.Dynamics(.9, 8, .5), new LocalLinearTrend.Dynamics(.1, .2),
                new SeasonalComponent.Dynamics(SeasonalModel.HarrisonStevens, 2.0, 12));
        ISsfMeasurement m = Measurement.create(dyn.getStateDim(), new int[]{0, 2, 4}, 1);
        Ssf ssf2 = new Ssf(dyn, m);
        DkLikelihood ll2 = (DkLikelihood) DkToolkit.likelihoodComputer(true, true).compute(ssf2, Models.ssfX);
        assertTrue(Math.abs(ll.getLogLikelihood() - ll2.getLogLikelihood()) < 1e-6);
    }

    @Test
    @Ignore
    public void stressTestComposite() {
        ModelSpecification spec = new ModelSpecification();
        spec.useLevel(ComponentUse.Free);
        spec.useSlope(ComponentUse.Free);
        spec.useNoise(ComponentUse.Free);
        spec.useCycle(ComponentUse.Free);
        spec.setSeasonalModel(SeasonalModel.HarrisonStevens);

        BasicStructuralModel model = new BasicStructuralModel(spec, 12);
        model.setVariance(Component.Level, .1);
        model.setVariance(Component.Slope, .2);
        model.setVariance(Component.Cycle, .5);
        model.setVariance(Component.Seasonal, 2);
        model.setVariance(Component.Noise, 1);
        model.setCycle(.9, 8);
        Ssf ssf = SsfBsm.create(model);
        Ssf ssf2 = SsfBsm2.create(model);

        CompositeDynamics dyn = new CompositeDynamics(new CyclicalComponent.Dynamics(.9, 8, .5), new LocalLinearTrend.Dynamics(.1, .2),
                new SeasonalComponent.Dynamics(SeasonalModel.HarrisonStevens, 2.0, 12));
        ISsfMeasurement m = Measurement.create(dyn.getStateDim(), new int[]{0, 2, 4}, 1);
        Ssf ssf3 = new Ssf(dyn, m);
        double l = 0;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DkLikelihood ll = (DkLikelihood) CkmsToolkit.likelihoodComputer().compute(ssf, Models.ssfProd);
            l = ll.getLogLikelihood();
        }
        long t1 = System.currentTimeMillis();
        System.out.println("bsm");
        System.out.println(t1 - t0);
        System.out.println(l);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DkLikelihood ll = (DkLikelihood) CkmsToolkit.likelihoodComputer().compute(ssf2, Models.ssfProd);
            l = ll.getLogLikelihood();
        }
        t1 = System.currentTimeMillis();
        System.out.println("Bsm2");
        System.out.println(t1 - t0);
        System.out.println(l);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DkLikelihood ll = (DkLikelihood) CkmsToolkit.likelihoodComputer().compute(ssf3, Models.ssfProd);
            l = ll.getLogLikelihood();
        }
        t1 = System.currentTimeMillis();
        System.out.println("composite");
        System.out.println(t1 - t0);
        System.out.println(l);
    }

    @Test
    public void testNoNoise() {
        ModelSpecification spec = new ModelSpecification();
        spec.useLevel(ComponentUse.Free);
        spec.useSlope(ComponentUse.Free);
        spec.useNoise(ComponentUse.Unused);
        spec.setSeasonalModel(SeasonalModel.HarrisonStevens);

        BasicStructuralModel model = new BasicStructuralModel(spec, 12);
        model.setVariance(Component.Level, .1);
        model.setVariance(Component.Slope, .2);
        model.setVariance(Component.Seasonal, 2);
        Ssf ssf = SsfBsm.create(model);
        
        DefaultSmoothingResults sr1 = DkToolkit.smooth(ssf, Models.ssfProd, false);
        System.out.println(sr1.getComponent(0));
        DefaultSmoothingResults sr2 = DkToolkit.sqrtSmooth(ssf, Models.ssfProd, false);
        System.out.println(sr2.getComponent(0));
    }
}
