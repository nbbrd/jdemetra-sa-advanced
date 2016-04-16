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
import ec.tstoolkit.eco.ILikelihood;
import ec.demetra.realfunctions.IParametricMapping;
import ec.demetra.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import ec.demetra.ssf.dk.sqrt.DiffuseSquareRootSmoother;
import ec.demetra.ssf.univariate.DefaultSmoothingResults;
import ec.demetra.ssf.univariate.ILikelihoodComputer;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.OrdinaryFilter;

/**
 *
 * @author Jean Palate
 */
public class DkToolkit {
    
    private DkToolkit() {
    }
    
    public static ILikelihoodComputer likelihoodComputer() {
        return likelihoodComputer(true, false);
    }
    
    public static ILikelihoodComputer likelihoodComputer(boolean res) {
        return likelihoodComputer(true, res);
    }
    
    public static ILikelihoodComputer likelihoodComputer(boolean sqr, boolean res) {
        return sqr ? new LLComputer2(res) : new LLComputer1(res);
    }
    
    public static <S extends ISsf> SsfFunction<S> likelihoodFunction(S ssf, ISsfData data, IParametricMapping<S> mapping) {
        return new SsfFunction<>(data, mapping, false, false);
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
    
    public static DataBlockStorage fastSmooth(ISsf ssf, ISsfData data) {
        FastStateSmoother smoother=new FastStateSmoother();
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
    
    private static class LLComputer1 implements ILikelihoodComputer {
        
        private final boolean res;
        
        LLComputer1(boolean res) {
            this.res = res;
        }
        
        @Override
        public ILikelihood compute(ISsf ssf, ISsfData data) {
            
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
    
    private static class LLComputer2 implements ILikelihoodComputer {
        
        private final boolean res;
        
        LLComputer2(boolean res) {
            this.res = res;
        }
        
        @Override
        public ILikelihood compute(ISsf ssf, ISsfData data) {
            
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
    
}
