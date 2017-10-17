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
import be.nbb.demetra.sssts.SSHSMonitor.MixedEstimation;
import be.nbb.demetra.sssts.SeasonalSpecification.EstimationMethod;
import ec.demetra.ssf.implementations.structural.ModelSpecification;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class SSHSMonitorTest {

    public SSHSMonitorTest() {
    }

    @Test
    @Ignore
    public void test1() {
        SSHSMonitor monitor = new SSHSMonitor();
        ModelSpecification mspec = new ModelSpecification();
        SeasonalSpecification sspec = new SeasonalSpecification();
        //sspec.method= EstimationMethod.Iterative;
        boolean process = monitor.process(Data.X, mspec, sspec);

        for (MixedEstimation me : monitor.getAllResults()) {
            System.out.print(me.model);
            System.out.print('\t');
            System.out.println(me.ll.getLogLikelihood());
            System.out.println(me.ll.AIC(4));
        }
        MixedEstimation best = monitor.getBestModel();
        MixedEstimation bsm = monitor.getAllResults().get(0);
        System.out.println(bsm.ll.AIC(3) - best.ll.AIC(4));
    }

    @Test
    @Ignore
    public void testSimulation() {
        ModelSpecification mspec = new ModelSpecification();
        SeasonalSpecification sspec = new SeasonalSpecification();
        //sspec.method= EstimationMethod.Iterative;
        int n = 10000;
        int i = 0;
        List<TsData> ss = Data.rndAirlines(n, 180, -.4, -.9);
        double[] x = ss.parallelStream().mapToDouble(
                s -> {
                    SSHSMonitor monitor = new SSHSMonitor();
                    boolean process = monitor.process(s, mspec, sspec);
                    MixedEstimation best = monitor.getBestModel();
                    MixedEstimation bsm = monitor.getAllResults().get(0);
                    double delta = bsm.ll.AIC(3) - best.ll.AIC(4);
                    System.out.println(delta);
                    return delta;
                }).toArray();
        System.out.println("");
        DescriptiveStatistics stats = new DescriptiveStatistics(x);
        for (int j = -1; j <30; ++j) {
            System.out.println(stats.countBetween(j , j+1));
        }
    }
}
