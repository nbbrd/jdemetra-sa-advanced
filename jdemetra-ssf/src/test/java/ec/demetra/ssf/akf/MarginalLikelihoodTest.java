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
package ec.demetra.ssf.akf;

import data.Models;
import ec.demetra.ssf.ckms.CkmsToolkit;
import ec.demetra.ssf.dk.DkLikelihood;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.implementations.structural.BasicStructuralModel;
import ec.demetra.ssf.implementations.structural.Component;
import ec.demetra.ssf.implementations.structural.ModelSpecification;
import ec.demetra.ssf.implementations.structural.SeasonalModel;
import ec.demetra.ssf.implementations.structural.SsfBsm;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class MarginalLikelihoodTest {

    final BasicStructuralModel model;

    public MarginalLikelihoodTest() {
        ModelSpecification mspec = new ModelSpecification();
        mspec.setSeasonalModel(SeasonalModel.Crude);
        model = new BasicStructuralModel(mspec, 12);
    }

    @Test
    @Ignore
    public void testLikelihood() {
        for (int i = 1; i < 100; ++i) {
            model.setVariance(Component.Seasonal, .1*i);
            SsfBsm bsm = SsfBsm.create(model);
 
            DkLikelihood dkll = DkToolkit.likelihoodComputer().compute(bsm, Models.ssfX);
            double ll1 = dkll.getLogLikelihood();
            DiffuseLikelihood akfll = AkfToolkit.likelihoodComputer(false).compute(bsm, Models.ssfX);
            double ll2 = akfll.getLogLikelihood();
            MarginalLikelihood mll = AkfToolkit.marginalLikelihoodComputer().compute(bsm, Models.ssfX);
            double ll3 = mll.getMarginalLogLikelihood();
            ProfileLikelihood pll = AkfToolkit.profileLikelihoodComputer().compute(bsm, Models.ssfX);
            double ll4=pll.getLogLikelihood();
            System.out.print(ll1);
            System.out.print('\t');
            System.out.print(ll2);
            System.out.print('\t');
            System.out.print(ll3);
            System.out.print('\t');
            System.out.println(ll4);
        }
    }

}
