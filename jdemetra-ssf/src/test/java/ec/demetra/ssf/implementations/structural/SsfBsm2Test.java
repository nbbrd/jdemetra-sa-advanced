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
import ec.demetra.ssf.ckms.CkmsToolkit;
import ec.demetra.ssf.dk.DkToolkit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SsfBsm2Test {

    static final int N = 50000;

    final SsfBsm2 bsm;

    public SsfBsm2Test() {
        ModelSpecification mspec = new ModelSpecification();
        BasicStructuralModel model = new BasicStructuralModel(mspec, 12);
        bsm = SsfBsm2.create(model);
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
    @Disabled
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
            AkfToolkit.likelihoodComputer().compute(bsm, Models.ssfX);
        }
        t1 = System.currentTimeMillis();
        System.out.println("akf filter");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            CkmsToolkit.likelihoodComputer().compute(bsm, Models.ssfX);
        }
        t1 = System.currentTimeMillis();
        System.out.println("fast filter");
        System.out.println(t1 - t0);
    }

}
