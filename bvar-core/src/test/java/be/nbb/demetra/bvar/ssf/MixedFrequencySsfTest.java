/*
 * Copyright 2016 National Bank of Belgium
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
package be.nbb.demetra.bvar.ssf;

import ec.demetra.eco.ILikelihood;
import ec.demetra.ssf.akf.AugmentedPredictionErrorsDecomposition;
import ec.demetra.ssf.akf.AugmentedSmoother;
import ec.demetra.ssf.akf.MultivariateAugmentedFilter;
import ec.demetra.ssf.dk.DiffuseSimulationSmoother;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import ec.demetra.ssf.implementations.MultivariateSsfWithIntercept;
import ec.demetra.ssf.implementations.MultivariateTimeInvariantSsf;
import ec.demetra.ssf.implementations.var.Var;
import ec.demetra.ssf.implementations.var.VarDescriptor;
import ec.demetra.ssf.multivariate.IMultivariateSsf;
import ec.demetra.ssf.multivariate.M2uAdapter;
import ec.demetra.ssf.multivariate.MultivariateOrdinarySmoother;
import ec.demetra.ssf.multivariate.SsfMatrix;
import ec.demetra.ssf.univariate.DefaultSmoothingResults;
import ec.demetra.ssf.univariate.DisturbanceSmoother;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.OrdinarySmoother;
import ec.demetra.ssf.univariate.PartialSmoothingResults;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;

/**
 *
 * @author Jean Palate
 */
public class MixedFrequencySsfTest {

    public MixedFrequencySsfTest() {
    }

    @Test
    public void testSmoothing() {
        int K = 6, L = 6;
        VarDescriptor desc = new VarDescriptor(K, L);
        Matrix a = Matrix.square(K);
        a.randomize(0);
        a.sub(.5);
        //a.mul(.75);
        for (int i = 0; i < L; ++i) {
            desc.getA(i + 1).copy(a.all());
            a = a.times(a);
        }
        Matrix M = new Matrix(500, K);
        M.randomize(0);
        for (int i = 0; i < 500; ++i) {
            if ((1 + i) % 3 != 0) {
                M.set(i, 0, Double.NaN);
                M.set(i, 1, Double.NaN);
                M.set(i, 2, Double.NaN);
            }
        }
        int[] c = new int[K];
        for (int i = 0; i < 3; ++i) {
            c[i] = 3;
        }
        for (int i = 3; i < c.length; ++i) {
            c[i] = 1;
        }
        MixedFrequencySsf ssf = MixedFrequencySsf.of(desc, c);
        SsfMatrix ssfdata = new SsfMatrix(M.clone());
        ISsf udfm = M2uAdapter.of(ssf);
        ISsfData udata = M2uAdapter.of(ssfdata);
        DefaultSmoothingResults sr = DefaultSmoothingResults.light();
        sr.prepare(udfm.getStateDim(), 0, ssfdata.getCount());
        PartialSmoothingResults psr = new PartialSmoothingResults(M.getColumnsCount(), sr);
        DkToolkit.sqrtSmooth(udfm, udata, psr);
        DataBlock s = new DataBlock(psr.getComponent(0));
//        System.out.println(s);
//        System.out.println(M.column(0).extract(2, -1, 3));

    }

//    @Test
//    public void testSmoothing2() {
//        int K = 6, L = 3;
//        VarDescriptor desc = new VarDescriptor(K, L);
//        Matrix a = Matrix.square(K);
//        a.randomize(0);
//        a.sub(.5);
//        a.mul(.25);
//        for (int i = 0; i < L; ++i) {
//            desc.getA(i + 1).copy(a.all());
//            a = a.times(a);
//        }
//        Matrix M = new Matrix(500, K);
//        M.randomize(0);
//        for (int i = 0; i < 500; ++i) {
//            if ((i+1) % 3 != 0) {
//                M.set(i, 0, Double.NaN);
//                M.set(i, 1, Double.NaN);
//                M.set(i, 2, Double.NaN);
//            }
//        }
//        int[] c = new int[K];
//        for (int i = 0; i < 3; ++i) {
//            c[i] = 3;
//        }
//        for (int i = 3; i < c.length; ++i) {
//            c[i] = 1;
//        }
//        MixedFreqSsf ssf = MixedFreqSsf.of(desc, c);
//        SsfMatrix ssfdata = new SsfMatrix(M);
//        MultivariateOrdinarySmoother smoother=new MultivariateOrdinarySmoother();
//        smoother.process(ssf, ssfdata);
//        System.out.println(sr.getComponent(0).extract(0, -1, K));
//        System.out.println(M.column(0).extract(2, -1, 3));
//    }
//    
    @Test
    @Disabled
    public void stressTestLLC() {
        int Q = 10;
        System.out.println("With diffuse intercept");
        for (int K = 4; K <= 12; K += 4) {
            for (int L = 3; L <= 12; L += 3) {
                System.out.print("K=");
                System.out.print(K);
                System.out.print(",L=");
                System.out.println(L);
                VarDescriptor desc = new VarDescriptor(K, L);
                Matrix a = Matrix.square(K);
                a.randomize(0);
                a.sub(.5);
                a.mul(.9);
                for (int i = 0; i < L; ++i) {
                    desc.getA(i + 1).copy(a.all());
                    a = a.times(a);
                }
                Matrix M = new Matrix(500, K);
                M.randomize(0);
                for (int i = 0; i < 500; ++i) {
                    if (i % 3 != 0) {
                        M.set(i, 0, Double.NaN);
                        M.set(i, 1, Double.NaN);
                        M.set(i, 2, Double.NaN);
                    }
                }
                int[] c = new int[K];
                for (int i = 0; i < 3; ++i) {
                    c[i] = 3;
                }
                for (int i = 3; i < c.length; ++i) {
                    c[i] = 1;
                }
                MixedFrequencySsf ssf = MixedFrequencySsf.of(desc, c);
                IMultivariateSsf mssfc = MultivariateTimeInvariantSsf.of(MultivariateSsfWithIntercept.addIntercept(ssf));
                IMultivariateSsf ssfc = MultivariateSsfWithIntercept.addIntercept(ssf);
                ILikelihood likelihood = null;
                long t0 = System.currentTimeMillis();
                for (int i = 0; i < Q; ++i) {
                    try {
                        AugmentedPredictionErrorsDecomposition pe2 = new AugmentedPredictionErrorsDecomposition();
                        MultivariateAugmentedFilter filter = new MultivariateAugmentedFilter();
                        filter.process(mssfc, new SsfMatrix(M), pe2);
                        likelihood = pe2.likelihood();
                    } catch (Exception err) {
                        System.out.println("failed");
                        likelihood = null;
                        break;
                    }
                }
                long t1 = System.currentTimeMillis();
                System.out.println(t1 - t0);
                if (likelihood != null) {
                    System.out.println(likelihood.getLogLikelihood());
                }
                t0 = System.currentTimeMillis();
                for (int i = 0; i < Q; ++i) {
                    try {
                        AugmentedPredictionErrorsDecomposition pe2 = new AugmentedPredictionErrorsDecomposition();
                        MultivariateAugmentedFilter filter = new MultivariateAugmentedFilter();
                        filter.process(ssfc, new SsfMatrix(M), pe2);
                        likelihood = pe2.likelihood();
                    } catch (Exception err) {
                        System.out.println("failed");
                        likelihood = null;
                        break;
                    }
                }
                t1 = System.currentTimeMillis();
                System.out.println(t1 - t0);
                if (likelihood != null) {
                    System.out.println(likelihood.getLogLikelihood());
                }
                t0 = System.currentTimeMillis();
                for (int i = 0; i < Q; ++i) {
                    try {
                        SsfMatrix ssfdata = new SsfMatrix(M);
                        ISsf udfm = M2uAdapter.of(ssfc);
                        ISsfData udata = M2uAdapter.of(ssfdata);
                        likelihood = DkToolkit.likelihoodComputer().compute(udfm, udata);
                    } catch (Exception err) {
                        System.out.println("failed");
                        likelihood = null;
                        break;
                    }
                }
                t1 = System.currentTimeMillis();
                System.out.println(t1 - t0);
                if (likelihood != null) {
                    System.out.println(likelihood.getLogLikelihood());
                }
            }
        }
        System.out.println();
    }

    @Test
    @Disabled
    public void testSimulation() {
        long t0 = System.currentTimeMillis();
        int K = 6, L = 6, N = 100000;
        VarDescriptor desc = new VarDescriptor(K, L);
        Matrix a = Matrix.square(K);
        a.randomize(0);
        a.sub(.5);
        //a.mul(.75);
        for (int i = 0; i < L; ++i) {
            desc.getA(i + 1).copy(a.all());
            a = a.times(a);
        }
        Matrix M = new Matrix(500, K);
        M.randomize(0);
        for (int i = 0; i < 500; ++i) {
            if ((1 + i) % 3 != 0) {
                M.set(i, 0, Double.NaN);
                M.set(i, 1, Double.NaN);
                M.set(i, 2, Double.NaN);
            }
        }
        int[] c = new int[K];
        for (int i = 0; i < 3; ++i) {
            c[i] = 3;
        }
        for (int i = 3; i < c.length; ++i) {
            c[i] = 1;
        }
        MixedFrequencySsf ssf = MixedFrequencySsf.of(desc, c);
        SsfMatrix ssfdata = new SsfMatrix(M.clone());
        ISsf udfm = M2uAdapter.of(ssf);
        ISsfData udata = M2uAdapter.of(ssfdata);
        IMultivariateSsf ssfc = MultivariateSsfWithIntercept.addIntercept(ssf);
        DiffuseSimulationSmoother dss = new DiffuseSimulationSmoother(udfm, udata);
//        System.out.println(dss.getReferenceSmoothing().getSmoothedStates().item(0));
        DataBlock sum = null;
        for (int i = 0; i < N; ++i) {
            DiffuseSimulationSmoother.Simulation simul = dss.newSimulation();
//            System.out.println(simul.getSmoothedStates().item(0).extract(0, -1, K));
            DataBlock item = simul.getSimulatedStates().item(0).extract(0, -1, K);
            if (sum != null) {
                sum.add(item);
            } else {
                sum = item.deepClone();
            }
        }
        sum.div(N);
        System.out.println(sum);
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
