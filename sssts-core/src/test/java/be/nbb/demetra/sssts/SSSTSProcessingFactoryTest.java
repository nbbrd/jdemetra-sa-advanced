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
import ec.demetra.ssf.implementations.structural.Component;
import ec.demetra.ssf.implementations.structural.SeasonalModel;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.modelling.arima.Method;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.simplets.TsData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;

/**
 *
 * @author Jean Palate
 */
public class SSSTSProcessingFactoryTest {
    
    private TsData s;

    public SSSTSProcessingFactoryTest() {
       TsPeriodSelector sel=new TsPeriodSelector();
       sel.to(new Day(1984, Month.December, 30));
       s=Data.Hous_MW.select(sel);
    }

    private SSSTSSpecification spec() {
        SSSTSSpecification spec = new SSSTSSpecification();
        spec.getDecompositionSpec().noisyComponent = Component.Noise;
        spec.getPreprocessingSpec().ao = false;
        spec.getPreprocessingSpec().ls = false;
        spec.getPreprocessingSpec().tc = false;
        spec.getPreprocessingSpec().dtype = TradingDaysType.WorkingDays;
        return spec;
    }

    @Test
    //@Ignore
    public void testGradient() {
        System.out.println("Gradient");
        SSSTSSpecification spec = spec();
        spec.getDecompositionSpec().method = SeasonalSpecification.EstimationMethod.LikelihoodGradient;
        CompositeResults process = SSSTSProcessingFactory.process(s, spec);
        SSSTSResults results = process.get(SSSTSProcessingFactory.DECOMPOSITION, SSSTSResults.class);
//        for (SSSTSMonitor.MixedEstimation me : results.getAllModels()) {
//            System.out.print(me.model);
//            System.out.print('\t');
//            System.out.print(me.model.getNoisyPeriodsVariance());
//            System.out.print('\t');
//            System.out.println(me.ll.getLogLikelihood());
//        }
    }

    @Test
    //@Ignore
    public void testIterative() {
        System.out.println("Iterative");
        SSSTSSpecification spec = spec();
        spec.getDecompositionSpec().method = SeasonalSpecification.EstimationMethod.Iterative;
        CompositeResults process = SSSTSProcessingFactory.process(s, spec);
        SSSTSResults results = process.get(SSSTSProcessingFactory.DECOMPOSITION, SSSTSResults.class);
//        for (SSSTSMonitor.MixedEstimation me : results.getAllModels()) {
//            System.out.print(me.model);
//            System.out.print('\t');
//            System.out.print(me.model.getNoisyPeriodsVariance());
//            System.out.print('\t');
//            System.out.println(me.ll.getLogLikelihood());
//        }
    }
}
