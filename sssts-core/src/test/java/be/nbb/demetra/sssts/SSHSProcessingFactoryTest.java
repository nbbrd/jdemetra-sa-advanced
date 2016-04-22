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
package be.nbb.demetra.sssts;

import data.Data;
import be.nbb.demetra.sssts.SeasonalSpecification.EstimationMethod;
import ec.demetra.ssf.implementations.structural.Component;
import ec.demetra.ssf.implementations.structural.SeasonalModel;
import ec.tstoolkit.algorithm.CompositeResults;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class SSHSProcessingFactoryTest {

    public SSHSProcessingFactoryTest() {
    }

    @Test
    @Ignore
    public void testGradient() {
        SSHSSpecification spec = new SSHSSpecification();
        spec.getDecompositionSpec().noisyComponent = Component.Seasonal;
        spec.getModelSpecification().setSeasonalModel(SeasonalModel.HarrisonStevens);
        CompositeResults process = SSHSProcessingFactory.process(Data.Hous_MW, spec);
        SSHSResults results = process.get(SSHSProcessingFactory.DECOMPOSITION, SSHSResults.class);
        for (SSHSMonitor.MixedEstimation me : results.getAllModels()) {
            System.out.print(me.model);
            System.out.print('\t');
            System.out.print(me.model.getNoisyPeriodsVariance());
            System.out.print('\t');
            System.out.println(me.ll.getLogLikelihood());
        }
    }

    @Test
    @Ignore
    public void testIterative() {
        SSHSSpecification spec = new SSHSSpecification();
        spec.getModelSpecification().setSeasonalModel(SeasonalModel.HarrisonStevens);
        spec.getDecompositionSpec().noisyComponent = Component.Seasonal;
        spec.getDecompositionSpec().method = EstimationMethod.Iterative;
        CompositeResults process = SSHSProcessingFactory.process(Data.Hous_MW, spec);
        SSHSResults results = process.get(SSHSProcessingFactory.DECOMPOSITION, SSHSResults.class);
        for (SSHSMonitor.MixedEstimation me : results.getAllModels()) {
            System.out.print(me.model);
            System.out.print('\t');
            System.out.print(me.model.getNoisyPeriodsVariance());
            System.out.print('\t');
            System.out.println(me.ll.getLogLikelihood());
        }
    }
}
