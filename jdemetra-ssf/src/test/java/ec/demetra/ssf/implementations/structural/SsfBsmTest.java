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
package ec.demetra.ssf.implementations.structural;

import data.Models;
import ec.demetra.ssf.akf.AkfToolkit;
import ec.demetra.ssf.akf.DiffuseLikelihood;
import ec.demetra.ssf.ckms.CkmsToolkit;
import ec.demetra.ssf.dk.DiffusePredictionErrorDecomposition;
import ec.demetra.ssf.dk.DkLikelihood;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.univariate.PredictionErrorDecomposition;
import ec.tstoolkit.eco.ILikelihood;
import static org.junit.Assert.assertEquals;
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
        mspec.setSeasonalModel(SeasonalModel.Dummy);
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
        DkLikelihood dkll = DkToolkit.likelihoodComputer().compute(bsm, Models.ssfProd);
        double ll1 = dkll.getLogLikelihood();
        DiffuseLikelihood akfll = AkfToolkit.likelihoodComputer().compute(bsm, Models.ssfProd);
        double ll2 = akfll.getLogLikelihood();
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
        System.out.println("dk filter (sqr)");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DkToolkit.likelihoodComputer(false, false).compute(bsm, Models.ssfX);
        }
        t1 = System.currentTimeMillis();
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

}
