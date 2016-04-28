/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.ssf.dk;

import data.Models;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.demetra.ssf.univariate.DefaultSmoothingResults;
import ec.tstoolkit.data.DescriptiveStatistics;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Admin
 */
public class DiffuseSimulationSmootherTest {

    final static int N = 100000;

    public DiffuseSimulationSmootherTest() {
    }

    @Test
    public void testGenerate() {
        DiffuseSimulationSmoother smoother = new DiffuseSimulationSmoother(Models.ssfUcarima, Models.ssfX);
        DiffuseSimulationSmoother.Simulation simul = smoother.newSimulation();
        simul.getSimulatedStates();
//        System.out.println(DkToolkit.smooth(Models.ssfUcarima, Models.ssfX, false).getComponent(0));
//        System.out.println(simul.getGeneratedStates().item(0));
//        System.out.println(simul.getSmoothedStates().item(0));
//        System.out.println(simul.getSimulatedStates().item(0));
    }

    @Test
    @Ignore
    public void stressTestUcarima() {
        int CMP = 0;
        long t0 = System.currentTimeMillis();
        DiffuseSimulationSmoother smoother = new DiffuseSimulationSmoother(Models.ssfUcarima, Models.ssfXRandom);
        Matrix all = new Matrix(Models.ssfX.getLength(), N);
        for (int i = 0; i < N; ++i) {
            DiffuseSimulationSmoother.Simulation simul = smoother.newSimulation();
            DataBlockStorage simulatedStates = simul.getSimulatedStates();
            all.column(i).copy(simulatedStates.item(CMP));
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Arima");
        System.out.println(t1 - t0);
        DataBlock M = new DataBlock(all.getRowsCount());
        DataBlock E = new DataBlock(all.getRowsCount());
        for (int i = 0; i < all.getRowsCount(); ++i) {
            DescriptiveStatistics stats = new DescriptiveStatistics(all.row(i));
            M.set(i, stats.getAverage());
            E.set(i, stats.getStdev());
        }
        DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(Models.ssfUcarima, Models.ssfXRandom, true);
        System.out.println(sr.getComponent(CMP));
        System.out.println(M);
        System.out.println(E);
        DataBlock e = new DataBlock(sr.getComponentVariance(CMP));
        e.sqrt();
        System.out.println(e);
        //         t0 = System.currentTimeMillis();
//        for (int i = 0; i < N; ++i) {
//            DkToolkit.smooth(Models.ssfUcarima, Models.ssfXRandom, false).getComponent(0);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
    }

    @Test
    @Ignore
    public void stressTestBsm() {
        int CMP = 1;
        long t0 = System.currentTimeMillis();
        DiffuseSimulationSmoother smoother = new DiffuseSimulationSmoother(Models.ssfBsm, Models.ssfXRandom);
        Matrix all = new Matrix(Models.ssfX.getLength(), N);
        for (int i = 0; i < N; ++i) {
            DiffuseSimulationSmoother.Simulation simul = smoother.newSimulation();
            DataBlockStorage simulatedStates = simul.getSimulatedStates();
            all.column(i).copy(simulatedStates.item(CMP));
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Bsm");
        System.out.println(t1 - t0);
        DataBlock M = new DataBlock(all.getRowsCount());
        DataBlock E = new DataBlock(all.getRowsCount());
        for (int i = 0; i < all.getRowsCount(); ++i) {
            DescriptiveStatistics stats = new DescriptiveStatistics(all.row(i));
            M.set(i, stats.getAverage());
            E.set(i, stats.getStdev());
        }
        DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(Models.ssfBsm, Models.ssfXRandom, true);
        System.out.println(sr.getComponent(CMP));
        System.out.println(M);
        System.out.println(E);
        DataBlock e = new DataBlock(sr.getComponentVariance(CMP));
        e.sqrt();
        System.out.println(e);
        //         t0 = System.currentTimeMillis();
//        for (int i = 0; i < N; ++i) {
//            DkToolkit.smooth(Models.ssfUcarima, Models.ssfXRandom, false).getComponent(0);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
    }

}
