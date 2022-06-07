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
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.simplets.TsData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class SSHSProcessingFactoryTest {

    private TsData s;

    public SSHSProcessingFactoryTest() {
        TsPeriodSelector sel = new TsPeriodSelector();
        sel.to(new Day(1984, Month.December, 30));
        s = Data.Hous_MW.select(sel);
    }

    private SSHSSpecification spec() {
        SSHSSpecification spec = new SSHSSpecification();
        spec.getDecompositionSpec().noisyComponent = Component.Seasonal;
        spec.getModelSpecification().setSeasonalModel(SeasonalModel.HarrisonStevens);
        spec.getPreprocessingSpec().ao = false;
        spec.getPreprocessingSpec().ls = false;
        spec.getPreprocessingSpec().tc = false;
        spec.getPreprocessingSpec().dtype = TradingDaysType.WorkingDays;
        return spec;
    }

    @Test
    @Disabled
    public void testGradient() {
        SSHSSpecification spec = spec();
        spec.getDecompositionSpec().method = EstimationMethod.LikelihoodGradient;
        CompositeResults process = SSHSProcessingFactory.process(s, spec);
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
    @Disabled
    public void testIterative() {
        SSHSSpecification spec = spec();
        spec.getDecompositionSpec().method = EstimationMethod.Iterative;
        CompositeResults process = SSHSProcessingFactory.process(s, spec);
        SSHSResults results = process.get(SSHSProcessingFactory.DECOMPOSITION, SSHSResults.class);
        for (SSHSMonitor.MixedEstimation me : results.getAllModels()) {
            System.out.print(me.model);
            System.out.print('\t');
            System.out.print(me.model.getBasicStructuralModel().getVariance(Component.Level));
            System.out.print('\t');
            System.out.print(me.model.getBasicStructuralModel().getVariance(Component.Noise));
            System.out.print('\t');
            System.out.print(me.model.getNoisyPeriodsVariance());
            System.out.print('\t');
            System.out.println(me.ll.getLogLikelihood());
        }
    }
}
