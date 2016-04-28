/*
 * Copyright 2013-2014 National Bank of Belgium
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
package ec.demetra.ssf.dk;

import data.Data;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.eco.ILikelihood;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.demetra.ssf.implementations.TimeInvariantSsf;
import ec.demetra.ssf.implementations.arima.SsfArima;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.SsfData;
import java.util.Arrays;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class DkToolkitTest {

    private static final int N = 100000, M = 10000;
    static final SarimaModel model;
    static final double[] data;

    static {
        SarimaModelBuilder builder = new SarimaModelBuilder();
        model = builder.createArimaModel(12, 3, 1, 1, 0, 1, 1);
        double[] p = new double[]{-.3, -.3, -.3, -.5, -.9};
        model.setParameters(new DataBlock(p));
        //model = builder.createAirlineModel(12, -.6, -.8);
        TsData s = Data.X;
        data = new double[s.getLength()];
        s.copyTo(data, 0);
        data[2] = Double.NaN;
        data[11] = Double.NaN;
        data[119] = Double.NaN;
    }

    public DkToolkitTest() {
    }

    @Test
    public void testLikelihood() {
        SsfArima ssf = SsfArima.create(model);
        SsfData ssfData = new SsfData(data);
        DkLikelihood ll = DkToolkit.likelihoodComputer(false, true).compute(ssf, ssfData);
    }

   @Test
    public void testErrors() {
        SsfArima ssf = SsfArima.create(model);
        SsfData ssfData = new SsfData(data);
        DefaultDiffuseFilteringResults fr = DkToolkit.filter(ssf, ssfData, false);
        assertTrue(fr.errors(true, true).distance(DataBlock.select(fr.errors(true, false), (x)->Double.isFinite(x)))<1e-9);
        Arrays.stream(data);
    }

    @Test
    @Ignore
    public void testEstimation() {
//        long t0 = System.currentTimeMillis();
//        for (int i = 0; i < 1000; ++i) {
//            SsfArima ssf = SsfArima.create(model);
//            SsfData ssfData = new SsfData(data);
//            IParametricMapping<SsfArima> mapping = SsfArima.mapping(model.getSpecification());
//            SsfFunction<SsfArima> fn = DkToolkit.likelihoodFunction(ssf, ssfData, mapping);
//            //LevenbergMarquardtMinimizer opt = new LevenbergMarquardtMinimizer();
//            LevenbergMarquardtMethod opt = new LevenbergMarquardtMethod();
//            opt.minimize(fn, mapping.map(ssf));
//            SsfFunctionInstance<SsfArima> result = (SsfFunctionInstance) opt.getResult();
////            System.out.println(result.getLikelihood().getLogLikelihood());
////            System.out.println(result.getLikelihood().getSigma());
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println("Estimation");
//        System.out.println(t1 - t0);
    }

    @Test
    public void testSqr() {
        SsfArima ssf = SsfArima.create(model);
        SsfData ssfData = new SsfData(data);
        DkLikelihood ll =  DkToolkit.likelihoodComputer(true, true).compute(ssf, ssfData);
    }

    @Ignore
    @Test
    public void testStressLikelihood() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < M; ++i) {
            SsfArima ssf = SsfArima.create(model);
            DkLikelihood ll = DkToolkit.likelihoodComputer(false).compute(ssf, new SsfData(data));
        }
        long t1 = System.currentTimeMillis();
        System.out.println("DK (normal)");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < M; ++i) {
            SsfArima ssf = SsfArima.create(model);
            DkLikelihood ll = DkToolkit.likelihoodComputer(true).compute(ssf, new SsfData(data));
        }
        t1 = System.currentTimeMillis();
        System.out.println("DK (square root form)");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < M; ++i) {
            SsfArima ssf = SsfArima.create(model);
            ISsf tssf = TimeInvariantSsf.of(ssf);
            DkLikelihood ll = DkToolkit.likelihoodComputer(true).compute(tssf, new SsfData(data));
        }

        t1 = System.currentTimeMillis();
        System.out.println("DK Filter. Matrix");
        System.out.println(t1 - t0);
    }

}
