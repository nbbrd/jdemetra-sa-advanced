/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.mairline;

import be.nbb.demetra.mairline.MixedAirlineMonitor.MixedEstimation;
import data.Data;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class MixedAirlineMonitorTest {
    
    public MixedAirlineMonitorTest() {
    }

    @Test
    //@Ignore
    public void testSimulation() {
        MaSpecification mspec=new MaSpecification();
        //mspec.method= EstimationMethod.Iterative;
        int n = 1000;
        int i = 0;
        List<TsData> ss = Data.rndAirlines(n, 180, -.4, -.9);
        double[] x = ss.parallelStream().mapToDouble(
                s -> {
                    MixedAirlineMonitor monitor = new MixedAirlineMonitor();
                    boolean process = monitor.process(s, mspec);
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
