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
package ec.demetra.ssf.dk;

import ec.tstoolkit.data.DataBlockStorage;
import ec.demetra.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import ec.demetra.eco.ILikelihood;
import ec.demetra.realfunctions.IParametricMapping;
import ec.demetra.ssf.ResultsRange;
import ec.demetra.ssf.State;
import ec.demetra.ssf.ckms.CkmsDiffuseInitializer;
import ec.demetra.ssf.ckms.CkmsFilter;
import ec.demetra.ssf.ckms.CkmsInitializer;
import ec.demetra.ssf.dk.sqrt.CompositeDiffuseSquareRootFilteringResults;
import ec.demetra.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import ec.demetra.ssf.dk.sqrt.DiffuseSquareRootSmoother;
import ec.demetra.ssf.univariate.DefaultSmoothingResults;
import ec.demetra.ssf.univariate.IConcentratedLikelihoodComputer;
import ec.demetra.ssf.univariate.ILikelihoodComputer;
import ec.demetra.ssf.univariate.ISmoothingResults;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfBuilder;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.OrdinaryFilter;
import ec.demetra.ssf.univariate.SsfRegressionModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.eco.Determinant;
import ec.tstoolkit.maths.linearfilters.ILinearProcess;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.matrices.UpperTriangularMatrix;

/**
 *
 * @author Jean Palate
 */
public class DkToolkit {

    private DkToolkit() {
    }

    public static ILikelihoodComputer<DkLikelihood> likelihoodComputer() {
        return likelihoodComputer(true, false);
    }

    public static ILikelihoodComputer<DkLikelihood> likelihoodComputer(boolean res) {
        return likelihoodComputer(true, res);
    }

    public static ILikelihoodComputer<DkLikelihood> likelihoodComputer(boolean sqr, boolean res) {
        return sqr ? new LLComputer2(res) : new LLComputer1(res);
    }

    public static IConcentratedLikelihoodComputer<DkConcentratedLikelihood> concentratedLikelihoodComputer() {
        return concentratedLikelihoodComputer(true, false);
    }

    public static IConcentratedLikelihoodComputer<DkConcentratedLikelihood> concentratedLikelihoodComputer(boolean sqr, boolean fast) {
        return new CLLComputer(sqr, fast);
    }

    public static <S, F extends ISsf> SsfFunction<S, F> likelihoodFunction(ISsfData data, IParametricMapping<S> mapping, ISsfBuilder<S, F> builder) {
        return new SsfFunction<>(data, mapping, builder);
    }

    public static <F extends ISsf> SsfFunction<F, F> likelihoodFunction(ISsfData data, IParametricMapping<F> mapping) {
        return new SsfFunction<>(data, mapping, (F f) -> f);
    }

    public static DefaultDiffuseFilteringResults filter(ISsf ssf, ISsfData data, boolean all) {
        DefaultDiffuseFilteringResults frslts = all
                ? DefaultDiffuseFilteringResults.full() : DefaultDiffuseFilteringResults.light();
        frslts.prepare(ssf, 0, data.getLength());
        DurbinKoopmanInitializer initializer = new DurbinKoopmanInitializer(frslts);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
        return frslts;
    }

    public static DefaultDiffuseSquareRootFilteringResults sqrtFilter(ISsf ssf, ISsfData data, boolean all) {
        DefaultDiffuseSquareRootFilteringResults frslts = all
                ? DefaultDiffuseSquareRootFilteringResults.full() : DefaultDiffuseSquareRootFilteringResults.light();
        frslts.prepare(ssf, 0, data.getLength());
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(frslts);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
        return frslts;
    }

    public static DefaultSmoothingResults smooth(ISsf ssf, ISsfData data, boolean all) {
        DiffuseSmoother smoother = new DiffuseSmoother();
        smoother.setCalcVariances(all);
        DefaultSmoothingResults sresults = all ? DefaultSmoothingResults.full()
                : DefaultSmoothingResults.light();
        sresults.prepare(ssf, 0, data.getLength());
        if (smoother.process(ssf, data, sresults)) {
            if (all) {
                sresults.rescaleVariances(var(data.getLength(), smoother.getFilteringResults()));
            }
            return sresults;
        } else {
            return null;
        }
    }

    public static boolean smooth(ISsf ssf, ISsfData data, ISmoothingResults sresults) {
        boolean all = sresults.hasVariances();
        DiffuseSmoother smoother = new DiffuseSmoother();
        smoother.setCalcVariances(all);
        if (smoother.process(ssf, data, sresults)) {
            if (all) {
                sresults.rescaleVariances(var(data.getLength(), smoother.getFilteringResults()));
            }
            return true;
        } else {
            return false;
        }
    }

    public static DataBlockStorage fastSmooth(ISsf ssf, ISsfData data) {
        return fastSmooth(ssf, data, null);
    }

    public static DataBlockStorage fastSmooth(ISsf ssf, ISsfData data, FastStateSmoother.Corrector corrector) {
        FastStateSmoother smoother = new FastStateSmoother();
        smoother.setCorrector(corrector);
        return smoother.process(ssf, data);
    }

    public static DefaultSmoothingResults sqrtSmooth(ISsf ssf, ISsfData data, boolean all) {
        DiffuseSquareRootSmoother smoother = new DiffuseSquareRootSmoother();
        smoother.setCalcVariances(all);
        DefaultSmoothingResults sresults = all ? DefaultSmoothingResults.full()
                : DefaultSmoothingResults.light();
        sresults.prepare(ssf, 0, data.getLength());
        if (smoother.process(ssf, data, sresults)) {
            if (all) {
                sresults.rescaleVariances(var(data.getLength(), smoother.getFilteringResults()));
            }
            return sresults;
        } else {
            return null;
        }
    }

    public static boolean sqrtSmooth(ISsf ssf, ISsfData data, ISmoothingResults sresults) {
        boolean all = sresults.hasVariances();
        DiffuseSquareRootSmoother smoother = new DiffuseSquareRootSmoother();
        smoother.setCalcVariances(all);
        if (smoother.process(ssf, data, sresults)) {
            if (all) {
                sresults.rescaleVariances(var(data.getLength(), smoother.getFilteringResults()));
            }
            return true;
        } else {
            return false;
        }
    }

    private static class LLComputer1 implements ILikelihoodComputer<DkLikelihood> {

        private final boolean res;

        LLComputer1(boolean res) {
            this.res = res;
        }

        @Override
        public DkLikelihood compute(ISsf ssf, ISsfData data) {

            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(res);
            if (res) {
                pe.prepare(ssf, data.getLength());
            }
            DurbinKoopmanInitializer initializer = new DurbinKoopmanInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            return pe.likelihood();
        }

    }

    private static class LLComputer2 implements ILikelihoodComputer<DkLikelihood> {

        private final boolean res;

        LLComputer2(boolean res) {
            this.res = res;
        }

        @Override
        public DkLikelihood compute(ISsf ssf, ISsfData data) {

            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(res);
            if (res) {
                pe.prepare(ssf, data.getLength());
            }
            DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            return pe.likelihood();
        }
    }

    private static class CLLComputer implements IConcentratedLikelihoodComputer<DkConcentratedLikelihood> {

        private final boolean sqr, fast;
        private boolean scaling = true;

        private CLLComputer(boolean sqr, boolean fast) {
            this.sqr = sqr;
            this.fast = fast;
        }

        @Override
        public DkConcentratedLikelihood compute(SsfRegressionModel model) {
            ISsfData y = model.getY();
            int n = y.getLength();
            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(true);
            pe.prepare(model.getSsf(), n);
            ILinearProcess lp = filteringResults(model.getSsf(), y, pe);
            DkConcentratedLikelihood dcll = new DkConcentratedLikelihood();
            DkLikelihood ll = pe.likelihood();
            IReadDataBlock yl = pe.errors(true, true);
            int nl = yl.getLength();
            Matrix xl = xl(model, lp, nl);
            if (xl == null) {
                dcll.set(ll.getSsqErr(), ll.getLogDeterminant(), ll.getDiffuseCorrection(), ll.getN(), ll.getD());
                dcll.setResiduals(yl);
                return dcll;
            } else {
                Householder qr = new Householder(true);
                qr.setEpsilon(1e-12);
                qr.decompose(xl);
                if (qr.getRank() == 0) {
                    dcll.set(ll.getSsqErr(), ll.getLogDeterminant(), ll.getDiffuseCorrection(), ll.getN(), ll.getD());
                    dcll.setResiduals(yl);
                    return dcll;
                } else {
                    DataBlock b = new DataBlock(qr.getRank());
                    DataBlock res = new DataBlock(nl - qr.getRank());
                    qr.leastSquares(yl, b, res);
                    double ssqerr = res.ssq();
                    Matrix u = UpperTriangularMatrix.inverse(qr.getR());
                    int[] unused = qr.getUnused();
                    // expand the results, if need be
                    b = expand(b, unused);
                    u = expand(u, unused);
                    // initializing the results...
                    int nobs = ll.getN();
                    int d = ll.getD();
                    int[] idiffuse = model.getDiffuseElements();
                    double ldet = ll.getLogDeterminant(), dcorr = ll.getDiffuseCorrection();
                    if (idiffuse != null) {
                        DataBlock rdiag = qr.getRDiagonal();
                        double lregdet = 0;
                        int ndc = 0;
                        for (int i = 0; i < idiffuse.length; ++i) {
                            if (isUsed(idiffuse[i], unused)) {
                                lregdet += Math.log(Math.abs(rdiag
                                        .get(idiffuse[i])));
                                ++ndc;
                            }
                        }
                        lregdet *= 2;
                        dcorr += lregdet;
                        d += ndc;
                    }
                    double sig = ssqerr / (nobs - d);
                    Matrix bvar = SymmetricMatrix.XXt(u);
                    bvar.mul(sig);
                    dcll.set(ssqerr, ldet, dcorr, n, d);
                    dcll.setResiduals(res);
                    dcll.set(b, bvar);
                }
            }
            return dcll;
        }

        private DataBlock expand(DataBlock x, int[] unused) {
            if (unused == null) {
                return x;
            }
            double[] bc = new double[x.getLength() + unused.length];
            for (int i = 0, j = 0, k = 0; i < bc.length; ++i) {
                if (k < unused.length && i == unused[k]) {
                    ++k;
                } else {
                    bc[i] = x.get(j);
                    ++j;
                }
            }
            return new DataBlock(bc);
        }

        private Matrix expand(Matrix v, int[] unused) {
            if (unused == null) {
                return v;
            }
            int nx = v.getColumnsCount() + unused.length;
            Matrix bvar = new Matrix(nx, nx);
            for (int i = 0, j = 0, k = 0; i < nx; ++i) {
                if (k < unused.length && i == unused[k]) {
                    ++k;
                } else {
                    for (int ci = 0, cj = 0, ck = 0; ci <= i; ++ci) {
                        if (ck < unused.length && ci == unused[ck]) {
                            ++ck;
                        } else {
                            double d = v.get(j, cj);
                            bvar.set(i, ci, d);
                            bvar.set(ci, i, d);
                            ++cj;
                        }
                    }
                    ++j;
                }
            }
            return bvar;
        }

        private ILinearProcess filteringResults(ISsf ssf, ISsfData data, DiffusePredictionErrorDecomposition pe) {
            if (sqr) {
                DefaultDiffuseSquareRootFilteringResults fr = DefaultDiffuseSquareRootFilteringResults.light();
                fr.prepare(ssf, 0, data.getLength());
                CompositeDiffuseSquareRootFilteringResults dr = new CompositeDiffuseSquareRootFilteringResults(fr, pe);
                DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(dr);
                if (fast) {
                    CkmsDiffuseInitializer ff = new CkmsDiffuseInitializer(initializer);
                    CkmsFilter ffilter = new CkmsFilter(ff);
                    ffilter.process(ssf, data, dr);
                    ResultsRange range = new ResultsRange(0, data.getLength());
                    return new DkFilter(ssf, fr, range);
                } else {
                    OrdinaryFilter filter = new OrdinaryFilter(initializer);
                    filter.process(ssf, data, dr);
                    ResultsRange range = new ResultsRange(0, data.getLength());
                    return new DkFilter(ssf, fr, range);
                }
            } else {
                DefaultDiffuseFilteringResults fr = DefaultDiffuseFilteringResults.light();
                fr.prepare(ssf, 0, data.getLength());
                CompositeDiffuseFilteringResults dr = new CompositeDiffuseFilteringResults(fr, pe);
                DurbinKoopmanInitializer initializer = new DurbinKoopmanInitializer(dr);
                if (fast) {
                    CkmsDiffuseInitializer ff = new CkmsDiffuseInitializer(initializer);
                    CkmsFilter ffilter = new CkmsFilter(ff);
                    ffilter.process(ssf, data, dr);
                    ResultsRange range = new ResultsRange(0, data.getLength());
                    return new DkFilter(ssf, fr, range);
                } else {
                    OrdinaryFilter filter = new OrdinaryFilter(initializer);
                    filter.process(ssf, data, dr);
                    ResultsRange range = new ResultsRange(0, data.getLength());
                    return new DkFilter(ssf, fr, range);
                }
            }
        }

        private Matrix xl(SsfRegressionModel model, ILinearProcess lp, int nl) {
            SubMatrix x = model.getX();
            if (x == null) {
                return null;
            }
            Matrix xl = new Matrix(nl, x.getColumnsCount());
            DataBlockIterator lcols = xl.columns();
            DataBlockIterator cols = x.columns();
            DataBlock lcol = lcols.getData();
            DataBlock col = cols.getData();
            do {
                lp.transform(col, lcol);
            } while (cols.next() && lcols.next());
            return xl;
        }

        private static boolean isUsed(final int i, final int[] unused) {
            for (int j = 0; j < unused.length; ++j) {
                if (unused[j] == i) {
                    return false;
                }
            }
            return true;
        }
    }

    public static double var(int n, IBaseDiffuseFilteringResults frslts) {
        int m = 0;
        double ssq = 0;
        int nd = frslts.getEndDiffusePosition();
        for (int i = 0; i < nd; ++i) {
            double e = frslts.error(i);
            if (Double.isFinite(e) && frslts.diffuseNorm2(i) == 0) {
                ++m;
                ssq += e * e / frslts.errorVariance(i);
            }
        }
        for (int i = nd; i < n; ++i) {
            double e = frslts.error(i);
            if (Double.isFinite(e)) {
                ++m;
                ssq += e * e / frslts.errorVariance(i);
            }
        }
        return ssq / m;
    }

    public static double logDeterminant(int n, IBaseDiffuseFilteringResults frslts) {
        Determinant det = new Determinant();
        for (int i = 0; i < frslts.getEndDiffusePosition(); ++i) {
            if (Double.isFinite(frslts.error(i))) {
                double d = frslts.diffuseNorm2(i);
                if (d == 0) {
                    double e = frslts.errorVariance(i);
                    if (e > State.ZERO) {
                        det.add(e);
                    }
                }
            }
        }
        for (int i = frslts.getEndDiffusePosition(); i < n; ++i) {
            if (Double.isFinite(frslts.error(i))) {
                double e = frslts.errorVariance(i);
                if (e > State.ZERO) {
                    det.add(e);
                }
            }
        }
        return det.getLogDeterminant();

    }

    public static double diffuseCorrection(IBaseDiffuseFilteringResults frslts) {
        Determinant det = new Determinant();
        for (int i = 0; i < frslts.getEndDiffusePosition(); ++i) {
            if (Double.isFinite(frslts.error(i))) {
                double d = frslts.diffuseNorm2(i);
                if (d > 0) {
                    det.add(d);
                }
            }
        }
        return det.getLogDeterminant();
    }

}
