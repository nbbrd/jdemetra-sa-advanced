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
package be.nbb.demetra.mairline;

import be.nbb.demetra.mairline.MaSpecification.EstimationMethod;
import data.Data;
import ec.tstoolkit.algorithm.CompositeResults;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class MixedAirlineProcessingFactoryTest {

    public MixedAirlineProcessingFactoryTest() {
    }

    @Test
    @Ignore
    public void testGradient() {
        MixedAirlineSpecification spec = new MixedAirlineSpecification();
        CompositeResults process = MixedAirlineProcessingFactory.process(Data.Hous_MW, spec);
        MixedAirlineResults results = process.get(MixedAirlineProcessingFactory.DECOMPOSITION, MixedAirlineResults.class);
        for (MixedAirlineMonitor.MixedEstimation me : results.getAllModels()) {
            System.out.print(me.model);
            System.out.print('\t');
            System.out.println(me.ll.getLogLikelihood());
        }
    }

    @Test
    @Ignore
    public void testIterative() {
        MixedAirlineSpecification spec = new MixedAirlineSpecification();
        spec.getDecompositionSpec().method = EstimationMethod.Iterative;
        CompositeResults process = MixedAirlineProcessingFactory.process(Data.Hous_MW, spec);
        MixedAirlineResults results = process.get(MixedAirlineProcessingFactory.DECOMPOSITION, MixedAirlineResults.class);
        for (MixedAirlineMonitor.MixedEstimation me : results.getAllModels()) {
            System.out.print(me.model);
            System.out.print('\t');
            System.out.println(me.ll.getLogLikelihood());
        }
    }
}
