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
package be.nbb.demetra.sssts.ssf;

import be.nbb.demetra.sts.*;
import be.nbb.demetra.sssts.SSSTSModel;
import data.Data;
import ec.demetra.ssf.dk.DkLikelihood;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.implementations.structural.Component;
import ec.demetra.ssf.implementations.structural.ModelSpecification;
import ec.demetra.ssf.implementations.structural.SeasonalModel;
import ec.demetra.ssf.univariate.SsfData;
import ec.tstoolkit.timeseries.simplets.TsData;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class SSSTSEstimationTest {

    SSSTSModel model = new SSSTSModel();
    TsData S = Data.P;

    public SSSTSEstimationTest() {
        model.setLvar(.25);
        model.setSvar(.25);
        model.setSeasvar(.25);
        model.setNvar(1);
        model.setNoisyPeriodsVariance(1);
        model.setFrequency(12);
        model.setNoisyPeriods(new int[]{6, 9});
    }

    @Test
    public void testCompute() {
        model.setNoisyComponent(Component.Slope);
        SSSTSEstimation estimation = new SSSTSEstimation();
        SSSTSModel rslt = estimation.compute(model, S);
//        System.out.println(rslt.getLvar());
//        System.out.println(rslt.getSvar());
//        System.out.println(rslt.getSeasvar());
//        System.out.println(rslt.getNvar());
//        System.out.println(rslt.getNoisyPeriodsVariance());
//        System.out.println("SSSTS-1");
        DkLikelihood ll = DkToolkit.likelihoodComputer().compute(SsfofSSSTS.of(rslt, 0), new SsfData(S));
//        System.out.println(ll.getLogLikelihood());
//        System.out.println("");
    }

    @Test
    @Ignore
    public void testCompute2() {
        model.setNoisyComponent(Component.Slope);
        SSSTSEstimation estimation = new SSSTSEstimation();
        SSSTSModel rslt = estimation.compute2(model, S);
        System.out.println(rslt.getLvar());
        System.out.println(rslt.getSvar());
        System.out.println(rslt.getSeasvar());
        System.out.println(rslt.getNvar());
        System.out.println(rslt.getNoisyPeriodsVariance());
        System.out.println("SSSTS-2");
        DkLikelihood ll = DkToolkit.likelihoodComputer().compute(SsfofSSSTS.of(rslt, 0), new SsfData(S));
        System.out.println(ll.getLogLikelihood());
        System.out.println("");
    }

    @Test
    @Ignore
    public void testCompute2bis() {
        model.setNoisyComponent(Component.Slope);
        model.setNoisyPeriods(null);
        SSSTSEstimation estimation = new SSSTSEstimation();
        SSSTSModel rslt = estimation.compute(model, S);
        DkLikelihood ll = DkToolkit.likelihoodComputer().compute(SsfofSSSTS.of(rslt, 0), new SsfData(S));
        System.out.println("SSSTS");
        System.out.println(ll.getLogLikelihood());
        System.out.println(rslt.getLvar());
        System.out.println(rslt.getSvar());
        System.out.println(rslt.getSeasvar());
        System.out.println(rslt.getNvar());
        System.out.println("");

        ModelSpecification specification = new ModelSpecification();
        specification.setSeasonalModel(SeasonalModel.HarrisonStevens);
        //specification.useSlope(ComponentUse.Fixed);
        BsmMonitor monitor = new BsmMonitor();
        monitor.setSpecification(specification);
        monitor.process(S, 12);
        System.out.println("BSM");
        System.out.println(monitor.getLikelihood().getLogLikelihood());
        System.out.println(monitor.getResult().getVariance(Component.Level));
        System.out.println(monitor.getResult().getVariance(Component.Slope));
        System.out.println(monitor.getResult().getVariance(Component.Seasonal));
        System.out.println(monitor.getResult().getVariance(Component.Noise));
        System.out.println("");
    }
}
