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

import be.nbb.demetra.sssts.SeasonalSpecification.EstimationMethod;
import data.Data;
import ec.demetra.ssf.implementations.structural.Component;
import ec.demetra.ssf.implementations.structural.ModelSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SSSTSMonitorTest {
    
    private final TsData series=Data.X;
    private final SeasonalSpecification sspec=new SeasonalSpecification();
    private final ModelSpecification mspec=new ModelSpecification();
    
    public SSSTSMonitorTest() {
    }

    @Test
    public void testNoiseIterative() {
        sspec.method=EstimationMethod.Iterative;
        sspec.noisyComponent=Component.Noise;
        SSSTSMonitor monitor=new SSSTSMonitor();
        boolean ok = monitor.process(series, mspec, sspec);
        System.out.println("Noise");
        System.out.println("Iterative");
        System.out.println(monitor.getBestModel().model.toString());
        System.out.println(monitor.getBestModel().ll.getLogLikelihood());
        System.out.println(monitor.getAllResults().get(0).ll.getLogLikelihood());
    }
    
    @Test
    public void testNoiseGrad() {
        sspec.method=EstimationMethod.LikelihoodGradient;
        sspec.noisyComponent=Component.Noise;
        SSSTSMonitor monitor=new SSSTSMonitor();
        boolean ok = monitor.process(series, mspec, sspec);
        System.out.println("Noise");
        System.out.println("Gradient");
        System.out.println(monitor.getBestModel().model.toString());
        System.out.println(monitor.getBestModel().ll.getLogLikelihood());
    }

    @Test
    public void testLevelIterative() {
        sspec.noisyComponent=Component.Level;
        sspec.method=EstimationMethod.Iterative;
        SSSTSMonitor monitor=new SSSTSMonitor();
        boolean ok = monitor.process(series, mspec, sspec);
        System.out.println("Level");
        System.out.println("Iterative");
        System.out.println(monitor.getBestModel().model.toString());
        System.out.println(monitor.getBestModel().ll.getLogLikelihood());
        System.out.println(monitor.getAllResults().get(0).ll.getLogLikelihood());
    }
    
    @Test
    public void testLevelGrad() {
        sspec.noisyComponent=Component.Level;
        sspec.method=EstimationMethod.LikelihoodGradient;
        SSSTSMonitor monitor=new SSSTSMonitor();
        boolean ok = monitor.process(series, mspec, sspec);
        System.out.println("Level");
        System.out.println("Gradient");
        System.out.println(monitor.getBestModel().model.toString());
        System.out.println(monitor.getBestModel().ll.getLogLikelihood());
    }
    @Test
    public void testSlopeIterative() {
        sspec.noisyComponent=Component.Slope;
        sspec.method=EstimationMethod.Iterative;
        SSSTSMonitor monitor=new SSSTSMonitor();
        boolean ok = monitor.process(series, mspec, sspec);
        System.out.println("Slope");
        System.out.println("Iterative");
        System.out.println(monitor.getBestModel().model.toString());
        System.out.println(monitor.getBestModel().ll.getLogLikelihood());
        System.out.println(monitor.getAllResults().get(0).ll.getLogLikelihood());
    }
    
    @Test
    public void testSlopeGrad() {
        sspec.noisyComponent=Component.Slope;
        sspec.method=EstimationMethod.LikelihoodGradient;
        SSSTSMonitor monitor=new SSSTSMonitor();
        boolean ok = monitor.process(series, mspec, sspec);
        System.out.println("Slope");
        System.out.println("Gradient");
        System.out.println(monitor.getBestModel().model.toString());
        System.out.println(monitor.getBestModel().ll.getLogLikelihood());
    }
}
