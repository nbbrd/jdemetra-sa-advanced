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
package ec.demetra.ssf.univariate;

import data.WeeklyData;
import ec.demetra.eco.ILikelihood;
import ec.demetra.ssf.multivariate.SsfMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.demetra.ssf.array.ArrayFilter;
import ec.demetra.ssf.array.MultivariateArrayFilter;
import ec.demetra.ssf.ckms.CkmsFilter;
import ec.demetra.ssf.implementations.MultivariateTimeInvariantSsf;
import ec.demetra.ssf.implementations.TimeInvariantSsf;
import ec.demetra.ssf.implementations.arima.SsfArima;
import ec.demetra.ssf.multivariate.MultivariateOrdinaryFilter;
import ec.demetra.ssf.multivariate.PredictionErrorsDecomposition;
import ec.demetra.ssf.multivariate.MultivariateSsf;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.Periodogram;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class OrdinaryFilterTest {

    private static final SarimaModel model;
    private static final Matrix M = new Matrix(360, 1);

    static {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.setP(1);
        spec.setQ(3);
        //spec.setBP(1);
        spec.setBQ(1);
        model = new SarimaModel(spec);
        M.randomize(0);
    }

    public OrdinaryFilterTest() {
    }

    @Test
    public void testNewMethod() {
        MultivariateOrdinaryFilter filter = new MultivariateOrdinaryFilter();
        SsfArima ssf = SsfArima.create(model);
        PredictionErrorsDecomposition pe = new PredictionErrorsDecomposition(false);
        filter.process(MultivariateSsf.proxy(ssf), new SsfMatrix(M), pe);
        ILikelihood ll = pe.likelihood();
    }

    @Test
    public void testNew1Method() {
        OrdinaryFilter filter = new OrdinaryFilter();
        SsfArima ssf = SsfArima.create(model);
        PredictionErrorDecomposition pe = new PredictionErrorDecomposition(false);
        filter.process(ssf, new SsfData(M.column(0)), pe);
        ILikelihood ll = pe.likelihood();
    }

    @Test
    public void testNew2Method() {
        MultivariateOrdinaryFilter filter = new MultivariateOrdinaryFilter();
        SsfArima ssf = SsfArima.create(model);
        PredictionErrorsDecomposition pe = new PredictionErrorsDecomposition(false);
        filter.process(MultivariateTimeInvariantSsf.of(ssf), new SsfMatrix(M), pe);
        ILikelihood ll = pe.likelihood();
    }

    @Test
    public void testNew3Method() {
        ArrayFilter filter = new ArrayFilter();
        SsfArima ssf = SsfArima.create(model);
        PredictionErrorDecomposition pe = new PredictionErrorDecomposition(false);
        filter.process(ssf, new SsfData(M.column(0)), pe);
        ILikelihood ll = pe.likelihood();
    }

    @Test
    public void testNew4Method() {
        MultivariateArrayFilter filter = new MultivariateArrayFilter();
        SsfArima ssf = SsfArima.create(model);
        PredictionErrorsDecomposition pe = new PredictionErrorsDecomposition(false);
        filter.process(MultivariateTimeInvariantSsf.of(ssf), new SsfMatrix(M), pe);
        ILikelihood ll = pe.likelihood();
    }

    @Test
    public void testNew5Method() {
        SsfArima ssf = SsfArima.create(model);
        CkmsFilter filter = new CkmsFilter(SsfArima.fastInitializer());
        PredictionErrorDecomposition pe = new PredictionErrorDecomposition(false);
        filter.process(ssf, new SsfData(M.column(0)), pe);
        ILikelihood ll = pe.likelihood();
    }

    @Disabled
    @Test
    public void stressTest1() {
        for (int q = 0; q < 2; ++q) {
            int N = q == 0 ? 1000 : 50000;

            long t0 = System.currentTimeMillis();
            for (int i = 0; i < N; ++i) {
                OrdinaryFilter filter = new OrdinaryFilter();
                SsfArima ssf = SsfArima.create(model);
                PredictionErrorDecomposition pe = new PredictionErrorDecomposition(false);
//            DefaultFilteringResults pe=DefaultFilteringResults.light();
                filter.process(ssf, new SsfData(M.column(0)), pe);
            }
            long t1 = System.currentTimeMillis();
            System.out.println("new ordinary filter");
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            for (int i = 0; i < N; ++i) {
                ArrayFilter filter = new ArrayFilter();
                SsfArima ssf = SsfArima.create(model);
                PredictionErrorDecomposition pe = new PredictionErrorDecomposition(false);
                filter.process(ssf, new SsfData(M.column(0)), pe);
            }
            t1 = System.currentTimeMillis();
            System.out.println("new array filter");
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            for (int i = 0; i < N; ++i) {
                MultivariateArrayFilter filter = new MultivariateArrayFilter();
                SsfArima ssf = SsfArima.create(model);
                PredictionErrorsDecomposition pe = new PredictionErrorsDecomposition(false);
                filter.process(MultivariateSsf.proxy(ssf), new SsfMatrix(M), pe);
            }
            t1 = System.currentTimeMillis();
            System.out.println("new multivariate array filter");
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            for (int i = 0; i < N; ++i) {
                SarimaModel arima = new SarimaModel(model.getSpecification());
                SsfArima ssf = SsfArima.create(arima);
                CkmsFilter filter = new CkmsFilter(SsfArima.fastInitializer());
                PredictionErrorDecomposition pe = new PredictionErrorDecomposition(false);
                filter.process(ssf, new SsfData(M.column(0)), pe);
            }
            t1 = System.currentTimeMillis();
            System.out.println("new fast filter");
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            for (int i = 0; i < N; ++i) {
                OrdinaryFilter filter = new OrdinaryFilter();
                SsfArima ssf = SsfArima.create(model);
                PredictionErrorDecomposition pe = new PredictionErrorDecomposition(false);
                filter.process(TimeInvariantSsf.of(ssf), new SsfData(M.column(0)), pe);
            }
            t1 = System.currentTimeMillis();
            System.out.println("new time invariant ordinary filter");
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            for (int i = 0; i < N; ++i) {
                MultivariateOrdinaryFilter filter = new MultivariateOrdinaryFilter();
                SsfArima ssf = SsfArima.create(model);
                PredictionErrorsDecomposition pe = new PredictionErrorsDecomposition(false);
                filter.process(MultivariateTimeInvariantSsf.of(ssf), new SsfMatrix(M), pe);
            }
            t1 = System.currentTimeMillis();
            System.out.println("new time invariant multivariate ordinary filter");
            System.out.println(t1 - t0);
        }
    }
    
    @Test
    public void testWeekly(){
        DataBlock x=new DataBlock(WeeklyData.US_PetroleumProducts);
        x.difference();
        Periodogram p=new Periodogram(x.drop(1, 0));
        p.setWindowLength(12);
//        System.out.println(new ReadDataBlock(p.getP()));
//        System.out.println(new ReadDataBlock(p.getS()));
    }

}
