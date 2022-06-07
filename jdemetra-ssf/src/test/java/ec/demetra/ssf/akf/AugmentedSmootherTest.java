/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.ssf.akf;

import data.Data;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.implementations.arima.SsfUcarima;
import ec.demetra.ssf.univariate.DefaultSmoothingResults;
import ec.demetra.ssf.univariate.SsfData;
import ec.tstoolkit.data.IReadDataBlock;
import java.util.Random;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Admin
 */
public class AugmentedSmootherTest {

    static final int N = 0;
    static final UcarimaModel ucm;
    static final SsfData data;

    static {
        TrendCycleSelector tsel = new TrendCycleSelector(.5);
        tsel.setDefaultLowFreqThreshold(12);
        SeasonalSelector ssel = new SeasonalSelector(12, 3);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);
        TsData x = Data.X.clone();
        int[] missing = new int[N];
        Random rng = new Random();
        for (int i = 0; i < N; ++i) {
            missing[i] = rng.nextInt(x.getLength());
        }
        SarimaModel arima = new SarimaModelBuilder().createAirlineModel(12, -.8, -.9);
        ucm = decomposer.decompose(ArimaModel.create(arima));
        ucm.setVarianceMax(-1);
        ucm.simplify();

        for (int i = 0; i < N; ++i) {
            x.setMissing(missing[i]);
        }
        data = new SsfData(x);
    }

    public AugmentedSmootherTest() {
    }

    @Test
    public void testSmoother() {
        SsfUcarima ssf = SsfUcarima.create(ucm);
        DefaultSmoothingResults srslts = DkToolkit.smooth(ssf, data, true);
        DefaultSmoothingResults srslts2 = AkfToolkit.smooth(ssf, data, true);
        IReadDataBlock component = srslts.getComponentVariance(0);
        IReadDataBlock component2 = srslts2.getComponentVariance(0);
        assertTrue(srslts.getComponent(0).distance(srslts2.getComponent(0)) < 1e-6);
    }

    @Test
    @Disabled
    public void stressTestSmoother() {
        int N = 5000;
        SsfUcarima ssf = SsfUcarima.create(ucm);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DefaultSmoothingResults srslts = DkToolkit.smooth(ssf, data, true);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("DK smoother");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DefaultSmoothingResults srslts2 = DkToolkit.sqrtSmooth(ssf, data, true);
        }
        t1 = System.currentTimeMillis();
        System.out.println("SQR DK smoother");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DefaultSmoothingResults srslts2 = AkfToolkit.smooth(ssf, data, true);
        }
        t1 = System.currentTimeMillis();
        System.out.println("AKF smoother");
        System.out.println(t1 - t0);
    }
}
