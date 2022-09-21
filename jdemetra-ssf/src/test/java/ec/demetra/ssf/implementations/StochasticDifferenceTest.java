/*
 * Copyright 2015 National Bank of Belgium
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
/*
 */
package ec.demetra.ssf.implementations;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.implementations.arima.SsfArima;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.demetra.ssf.univariate.OrdinaryFilter;
import ec.demetra.ssf.univariate.PredictionErrorDecomposition;
import ec.demetra.ssf.univariate.SsfData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class StochasticDifferenceTest {

    private static final SarimaModel starima, arima;
    private static final StochasticDifference stoch;

    static {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.setP(5);
        spec.setBP(1);
        spec.setQ(5);
        spec.setBQ(1);
        starima = new SarimaModel(spec);
        starima.setTheta(1, -.6);
        starima.setBTheta(1, -.8);
        spec.setD(1);
        spec.setBD(1);
        arima = new SarimaModel(spec);
        arima.setTheta(1, -.6);
        arima.setBTheta(1, -.8);

        stoch = StochasticDifference.create(SsfArima.create(starima), spec.getDifferencingFilter());
    }

    public StochasticDifferenceTest() {
    }

    @Test
    public void testSomeMethod() {
    }

    @Test
    public void testDynamics() {

        ISsf ref = TimeInvariantSsf.of(stoch);
        int dim = stoch.getStateDim();

        Matrix M1 = new Matrix(dim, dim);
        stoch.getDynamics().T(0, M1.all());
        M1.randomize();
        M1 = SymmetricMatrix.XXt(M1);
        Matrix M2 = M1.clone();
        DataBlock x1 = new DataBlock(dim);
        x1.randomize();
        DataBlock x2 = x1.deepClone();
        ISsfDynamics dynref = ref.getDynamics();
        ISsfDynamics dyn = stoch.getDynamics();
        dynref.TX(0, x1);
        dyn.TX(0, x2);
        assertTrue(x1.distance(x2) < 1e-9);
        dynref.XT(0, x1);
        dyn.XT(0, x2);
        assertTrue(x1.distance(x2) < 1e-9);
        dynref.TVT(0, M1.all());
        dyn.TVT(0, M2.all());
        assertTrue(M1.distance(M2) < 1e-9);
        dynref.TM(0, M1.all());
        dyn.TM(0, M2.all());
        assertTrue(M1.distance(M2) < 1e-9);
        dynref.MT(0, M1.all());
        dyn.MT(0, M2.all());
        assertTrue(M1.distance(M2) < 1e-9);
        Matrix Pi0 = Matrix.square(dim);
        Matrix B = new Matrix(dim, dyn.getNonStationaryDim());
        dyn.Pi0(Pi0.all());
        dyn.diffuseConstraints(B.all());
        assertTrue(Pi0.minus(SymmetricMatrix.XXt(B)).nrm2() < 1e-9);
        Matrix Pf0 = Matrix.square(dim);
        dyn.Pf0(Pf0.all());
    }

    @Test
    public void testMeasurement() {
        ISsf ref = TimeInvariantSsf.of(stoch);
        int dim = stoch.getStateDim();
        Matrix M1 = new Matrix(dim, dim);
        M1.randomize();
        M1 = SymmetricMatrix.XXt(M1);
        Matrix M2 = M1.clone();
        DataBlock x1 = new DataBlock(dim);
        x1.randomize();
        DataBlock x2 = x1.deepClone();
        ISsfMeasurement mref = ref.getMeasurement();
        ISsfMeasurement m = stoch.getMeasurement();
        assertTrue(Math.abs(mref.ZX(0, x1) - m.ZX(0, x2)) < 1e-9);
        assertTrue(Math.abs(mref.ZVZ(0, M1.all()) - m.ZVZ(0, M1.all())) < 1e-9);
        mref.VpZdZ(0, M1.all(), 5);
        m.VpZdZ(0, M2.all(), 5);
        assertTrue(M1.distance(M2) < 1e-9);
        mref.XpZd(0, x1, 5);
        m.XpZd(0, x2, 5);
        assertTrue(x1.distance(x2) < 1e-9);
        mref.ZM(dim, M1.all(), x1);
        m.ZM(dim, M1.all(), x2);
        assertTrue(x1.distance(x2) < 1e-9);
    }

    @Test
    public void testLikelihood() {
        OrdinaryFilter filter = new OrdinaryFilter();
        ISsf ssf = SsfArima.create(arima);
        PredictionErrorDecomposition pe = new PredictionErrorDecomposition(false);
        filter.process(ssf, new SsfData(data.Data.P), pe);
        double ll = pe.likelihood().getLogLikelihood();
        pe = new PredictionErrorDecomposition(false);
        ssf = StochasticDifference.create(SsfArima.create(starima), arima.getSpecification().getDifferencingFilter());
        filter.process(ssf, new SsfData(data.Data.P), pe);
        double ll1 = pe.likelihood().getLogLikelihood();
        assertTrue(Math.abs(ll - ll1) < 1e-6);
    }

    @Disabled
    @Test
    public void stressTest1() {
        double ll = 0, ll1 = 0;
        for (int q = 0; q < 2; ++q) {
            int N = q == 0 ? 100 : 5000;

            long t0 = System.currentTimeMillis();
            for (int i = 0; i < N; ++i) {
                OrdinaryFilter filter = new OrdinaryFilter();
                SsfArima ssf = SsfArima.create(arima);
                PredictionErrorDecomposition pe = new PredictionErrorDecomposition(false);
                filter.process(ssf, new SsfData(data.Data.P), pe);
                ll = pe.likelihood().getLogLikelihood();
            }
            long t1 = System.currentTimeMillis();
            System.out.println("Arima");
            System.out.println(t1 - t0);
            t0 = System.currentTimeMillis();
            for (int i = 0; i < N; ++i) {
                OrdinaryFilter filter = new OrdinaryFilter();
                PredictionErrorDecomposition pe = new PredictionErrorDecomposition(false);
                ISsf ssf = StochasticDifference.create(SsfArima.create(starima), arima.getSpecification().getDifferencingFilter());
                filter.process(ssf, new SsfData(data.Data.P), pe);
                ll1 = pe.likelihood().getLogLikelihood();
            }
            t1 = System.currentTimeMillis();
            System.out.println("Stochastic difference");
            System.out.println(t1 - t0);
        }
        System.out.println(ll);
        System.out.println(ll1);
    }

}
