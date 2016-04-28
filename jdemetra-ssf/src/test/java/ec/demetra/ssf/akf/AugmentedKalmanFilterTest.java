/*
 * Copyright 2015 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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
 /*
 */
package ec.demetra.ssf.akf;

import ec.tstoolkit.arima.ArimaModelBuilder;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.demetra.ssf.State;
import ec.demetra.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import ec.demetra.ssf.dk.DurbinKoopmanInitializer;
import ec.demetra.ssf.implementations.TimeInvariantSsf;
import ec.demetra.ssf.implementations.arima.SsfArima;
import ec.demetra.ssf.univariate.FilteringErrors;
import ec.demetra.ssf.univariate.SsfData;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class AugmentedKalmanFilterTest {

    private static final int N = 100000, M = 25000;
    static final SarimaModel model;
    static final double[] data;

    static {
        SarimaModelBuilder builder = new SarimaModelBuilder();
        model = builder.createArimaModel(12, 3, 1, 1, 0, 1, 1);
        model.setBTheta(1, -.99);
        //model = builder.createAirlineModel(12, -.6, -.8);
        ArimaModelBuilder gbuilder = new ArimaModelBuilder();
        data = gbuilder.generate(model, 500);
        data[5] = Double.NaN;
        data[12] = Double.NaN;
        data[21] = Double.NaN;
    }

    public AugmentedKalmanFilterTest() {
    }

    @Test
    public void testAkf() {
        AugmentedFilter akf = new AugmentedFilter(true);
        SsfArima ssf = SsfArima.create(model);
        AugmentedPredictionErrorDecomposition pe = new AugmentedPredictionErrorDecomposition(false);
        pe.prepare(ssf, data.length);
        akf.process(ssf, new SsfData(data), pe);
        AugmentedState state = akf.getState();
    }

    @Test
    public void testDK() {
        DurbinKoopmanInitializer dk = new DurbinKoopmanInitializer(null);
        SsfArima ssf = SsfArima.create(model);
        FilteringErrors pe = new FilteringErrors(false);
        SsfData ssfdata = new SsfData(data);
        State state = new State(ssf.getStateDim());
        dk.initialize(state, ssf, ssfdata);
    }

    @Test
    public void testSquareRoot() {
        DiffuseSquareRootInitializer dk = new DiffuseSquareRootInitializer(null);
        SsfArima ssf = SsfArima.create(model);
        FilteringErrors pe = new FilteringErrors(false);
        SsfData ssfdata = new SsfData(data);
        State state = new State(ssf.getStateDim());
        dk.initialize(state, ssf, ssfdata);
    }

    @Ignore
    @Test
    public void testStressInitialisation() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            AugmentedFilter akf = new AugmentedFilter(true);
            SsfArima ssf = SsfArima.create(model);
            AugmentedPredictionErrorDecomposition pe = new AugmentedPredictionErrorDecomposition(false);
            pe.prepare(ssf, data.length);
            akf.process(ssf, new SsfData(data), pe);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("akf");
        System.out.println(t1 - t0);
//        t0 = System.currentTimeMillis();
//        for (int i = 0; i < N; ++i) {
//            AugmentedFilter akf = new AugmentedFilter(false);
//            SsfArima ssf = SsfArima.create(model);
//            AugmentedPredictionErrorDecomposition pe = new AugmentedPredictionErrorDecomposition(false);
//            pe.prepare(ssf, data.length);
//            akf.process(ssf, new SsfData(data), pe);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println("akf without collapsing");
//        System.out.println(t1 - t0);
        System.out.println("dknew");
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DurbinKoopmanInitializer dk = new DurbinKoopmanInitializer(null);
            SsfArima ssf = SsfArima.create(model);
            FilteringErrors pe = new FilteringErrors(false);
            SsfData ssfdata = new SsfData(data);
            State state = new State(ssf.getStateDim());
            dk.initialize(state, ssf, ssfdata);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println("square root");
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DiffuseSquareRootInitializer dk = new DiffuseSquareRootInitializer(null);
            SsfArima ssf = SsfArima.create(model);
            FilteringErrors pe = new FilteringErrors(false);
            SsfData ssfdata = new SsfData(data);
            State state = new State(ssf.getStateDim());
            dk.initialize(state, ssf, ssfdata);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println("square root - Matrix");
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DiffuseSquareRootInitializer dk = new DiffuseSquareRootInitializer(null);
            SsfArima ssf = SsfArima.create(model);
            FilteringErrors pe = new FilteringErrors(false);
            SsfData ssfdata = new SsfData(data);
            State state = new State(ssf.getStateDim());
            dk.initialize(state, TimeInvariantSsf.of(ssf), ssfdata);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
