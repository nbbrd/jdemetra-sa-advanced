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

import data.Data;
import ec.demetra.ssf.dk.DkConcentratedLikelihood;
import ec.demetra.ssf.implementations.structural.BasicStructuralModel;
import ec.demetra.ssf.implementations.structural.ComponentUse;
import ec.demetra.ssf.implementations.structural.ModelSpecification;
import ec.demetra.ssf.implementations.structural.SeasonalModel;
import ec.tstoolkit.algorithm.CompositeResults;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class StsProcessingFactoryTest {
    
    public StsProcessingFactoryTest() {
    }

    @Test
    public void testDefault() {
        CompositeResults rslt = StsProcessingFactory.process(Data.P, new StsSpecification());
        assertTrue(rslt != null);
    }
    
    @Test
    public void testCli() {
            StsSpecification spec = new StsSpecification();
            BsmSpecification dspec = spec.getDecompositionSpec();
            ModelSpecification mspec = new ModelSpecification();
            mspec.useLevel(ComponentUse.Free);
            mspec.useSlope(ComponentUse.Free);
            mspec.useNoise(ComponentUse.Free);
            mspec.useCycle(ComponentUse.Unused);
            mspec.setSeasonalModel(SeasonalModel.Crude);
            dspec.setModelSpecification(mspec);

            CompositeResults rslt = StsProcessingFactory.process(Data.P, spec);
            StsEstimation estimation = rslt.get(StsProcessingFactory.ESTIMATION, StsEstimation.class);
            BasicStructuralModel model = estimation.getModel();
            DkConcentratedLikelihood likelihood = estimation.getLikelihood();
            assertTrue(dspec != null);
    }
}
