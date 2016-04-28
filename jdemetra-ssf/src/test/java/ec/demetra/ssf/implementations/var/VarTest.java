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
package ec.demetra.ssf.implementations.var;

import ec.demetra.eco.ILikelihood;
import ec.demetra.ssf.State;
import ec.demetra.ssf.akf.AugmentedPredictionErrorsDecomposition;
import ec.demetra.ssf.akf.MultivariateAugmentedFilter;
import ec.demetra.ssf.akf.MultivariateAugmentedFilterInitializer;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.implementations.MultivariateSsfWithIntercept;
import ec.demetra.ssf.implementations.MultivariateTimeInvariantSsf;
import ec.demetra.ssf.multivariate.IMultivariateSsf;
import ec.demetra.ssf.multivariate.IMultivariateSsfData;
import ec.demetra.ssf.multivariate.M2uAdapter;
import ec.demetra.ssf.multivariate.MultivariateFilter;
import ec.demetra.ssf.multivariate.MultivariateOrdinaryFilter;
import ec.demetra.ssf.multivariate.PredictionErrorsDecomposition;
import ec.demetra.ssf.multivariate.SsfMatrix;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.OrdinaryFilter;
import ec.demetra.ssf.univariate.PredictionErrorDecomposition;
import ec.tstoolkit.maths.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class VarTest {

    public VarTest() {
    }

    @Test
    @Ignore
    public void stressTestLL() {
        System.out.println("Without intercept");
        int Q = 100;
        for (int K = 4; K <= 12; K += 4) {
            for (int L = 3; L <= 12; L += 3) {
                System.out.print("K=");
                System.out.print(K);
                System.out.print(",L=");
                System.out.println(L);
                VarDescriptor desc = new VarDescriptor(K, L);
                Matrix a = Matrix.square(K);
                a.randomize(0);
                a.mul(.25);
                for (int i = 0; i < L; ++i) {
                    desc.getA(i + 1).copy(a.all());
                    a = a.times(.3);
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
                ILikelihood likelihood = null;
                long t0 = System.currentTimeMillis();
                for (int i = 0; i < Q; ++i) {
                    MultivariateOrdinaryFilter filter = new MultivariateOrdinaryFilter((State state, IMultivariateSsf ssf, IMultivariateSsfData data) -> {
                        state.P().diagonal().set(1);
                        return 0;
                    });
                    PredictionErrorsDecomposition pe1 = new PredictionErrorsDecomposition(false);
                    filter.process(MultivariateTimeInvariantSsf.of(Var.of(desc)), new SsfMatrix(M), pe1);
                    likelihood = pe1.likelihood();
                }
                long t1 = System.currentTimeMillis();
                System.out.println(t1 - t0);
                System.out.println(likelihood.getLogLikelihood());
                t0 = System.currentTimeMillis();
                for (int i = 0; i < Q; ++i) {
                    MultivariateFilter filter = new MultivariateFilter((State state, IMultivariateSsf ssf, IMultivariateSsfData data) -> {
                        state.P().diagonal().set(1);
                        return 0;
                    });
                    PredictionErrorsDecomposition pe2 = new PredictionErrorsDecomposition(false);
                    filter.process(Var.of(desc), new SsfMatrix(M), pe2);
                    likelihood = pe2.likelihood();
                }
                t1 = System.currentTimeMillis();
                System.out.println(t1 - t0);
                System.out.println(likelihood.getLogLikelihood());
                t0 = System.currentTimeMillis();
                for (int i = 0; i < Q; ++i) {
                    MultivariateOrdinaryFilter filter = new MultivariateOrdinaryFilter((State state, IMultivariateSsf ssf, IMultivariateSsfData data) -> {
                        state.P().diagonal().set(1);
                        return 0;
                    });
                    PredictionErrorsDecomposition pe2 = new PredictionErrorsDecomposition(false);
                    filter.process(Var.of(desc), new SsfMatrix(M), pe2);
                    likelihood = pe2.likelihood();
                }
                t1 = System.currentTimeMillis();
                System.out.println(t1 - t0);
                System.out.println(likelihood.getLogLikelihood());
                t0 = System.currentTimeMillis();
                OrdinaryFilter ofilter = new OrdinaryFilter((State state, ISsf ssf, ISsfData data) -> {
                    state.P().diagonal().set(1);
                    return 0;
                });
                for (int i = 0; i < Q; ++i) {
                    PredictionErrorDecomposition decomp = new PredictionErrorDecomposition(false);
                    SsfMatrix ssfdata = new SsfMatrix(M);
                    ISsf udfm = M2uAdapter.of(Var.of(desc));
                    ISsfData udata = M2uAdapter.of(ssfdata);
                    ofilter.process(udfm, udata, decomp);
                    likelihood = decomp.likelihood();
                }
                t1 = System.currentTimeMillis();
                System.out.println(t1 - t0);
                System.out.println(likelihood.getLogLikelihood());
            }
        System.out.println();
        }
    }

    @Test
    @Ignore
    public void stressTestLLC() {
        int Q = 100;
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
                a.mul(.25);
                for (int i = 0; i < L; ++i) {
                    desc.getA(i + 1).copy(a.all());
                    a = a.times(.3);
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
                ILikelihood likelihood = null;
                long t0 = System.currentTimeMillis();
                for (int i = 0; i < Q; ++i) {
                    IMultivariateSsf ssfc = MultivariateTimeInvariantSsf.of(MultivariateSsfWithIntercept.addIntercept(Var.of(desc)));
                    AugmentedPredictionErrorsDecomposition pe2 = new AugmentedPredictionErrorsDecomposition();
                    MultivariateAugmentedFilter filter = new MultivariateAugmentedFilter();
                    filter.process(ssfc, new SsfMatrix(M), pe2);
                    likelihood = pe2.likelihood();
                }
                long t1 = System.currentTimeMillis();
                System.out.println(t1 - t0);
                System.out.println(likelihood.getLogLikelihood());
                t0 = System.currentTimeMillis();
                for (int i = 0; i < Q; ++i) {
                    MultivariateSsfWithIntercept ssfc = MultivariateSsfWithIntercept.addIntercept(Var.of(desc));
                    AugmentedPredictionErrorsDecomposition pe2 = new AugmentedPredictionErrorsDecomposition();
                    MultivariateAugmentedFilter filter = new MultivariateAugmentedFilter();
                    filter.process(ssfc, new SsfMatrix(M), pe2);
                    likelihood = pe2.likelihood();
                }
                t1 = System.currentTimeMillis();
                System.out.println(t1 - t0);
                System.out.println(likelihood.getLogLikelihood());
                t0 = System.currentTimeMillis();
                for (int i = 0; i < Q; ++i) {
                    SsfMatrix ssfdata = new SsfMatrix(M);
                    ISsf udfm = M2uAdapter.of(MultivariateSsfWithIntercept.addIntercept(Var.of(desc)));
                    ISsfData udata = M2uAdapter.of(ssfdata);
                    likelihood = DkToolkit.likelihoodComputer().compute(udfm, udata);
                }
                t1 = System.currentTimeMillis();
                System.out.println(t1 - t0);
                System.out.println(likelihood.getLogLikelihood());
            }
        }
        System.out.println();
    }
}
