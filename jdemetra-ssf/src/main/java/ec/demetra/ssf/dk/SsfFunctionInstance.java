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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.demetra.eco.ILikelihood;
import ec.demetra.realfunctions.IFunctionInstance;
import ec.demetra.realfunctions.ISsqFunctionInstance;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.realfunctions.IFunction;
import ec.demetra.realfunctions.ISsqFunction;
import ec.demetra.ssf.univariate.IConcentratedLikelihoodComputer;
import ec.demetra.ssf.univariate.SsfRegressionModel;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
public class SsfFunctionInstance<S, F extends ISsf> implements
        ISsqFunctionInstance, IFunctionInstance {

    /**
     *
     */
    private final F currentSsf;
    private final S current;

    /**
     *
     */
    private final DkConcentratedLikelihood ll;
    private final DataBlock p;
    private DataBlock E;
    private final SsfFunction<S, F> fn;

    /**
     *
     * @param fn
     * @param p
     */
    public SsfFunctionInstance(SsfFunction<S, F> fn, IReadDataBlock p) {
        this.fn = fn;
        this.p = new DataBlock(p);
        current=fn.getMapping().map(p);
        currentSsf = fn.getBuilder().buildSsf(current);
        boolean fastcomputer=fn.isFast() && !fn.isMissing() && currentSsf.isTimeInvariant();
        IConcentratedLikelihoodComputer<DkConcentratedLikelihood> computer= DkToolkit.concentratedLikelihoodComputer(true, fastcomputer);
        if (fn.getX() == null)
            ll=computer.compute(currentSsf, fn.getData());
        else
            ll=computer.compute(new SsfRegressionModel(currentSsf, fn.getData(), fn.getX(), fn.getDiffuseX()));
    }

    public F getSsf() {
        return currentSsf;
    }

    public S getCore() {
        return current;
    }

    @Override
    public IReadDataBlock getE() {
        if (E == null) {
            IReadDataBlock res = ll.getResiduals();
            if (res == null) {
                return null;
            } else {
                E = DataBlock.select(res, x->Double.isFinite(x));
                if (fn.isMaximumLikelihood()) {
                    double factor = Math.sqrt(ll.getFactor());
                    E.mul(factor);
                }
            }
        }
        return E;
    }

    /**
     *
     * @return
     */
    public ILikelihood getLikelihood() {
        return ll;
    }

    @Override
    public IReadDataBlock getParameters() {
        return p;
    }

    @Override
    public double getSsqE() {
        if (ll == null) {
            return Double.NaN;
        }
        return fn.isMaximumLikelihood() ? ll.getSsqErr() * ll.getFactor() : ll.getSsqErr();
    }

    @Override
    public double getValue() {
        if (ll == null) {
            return Double.NaN;
        }
        if (fn.isLog()) {
            return fn.isMaximumLikelihood() ? -ll.getLogLikelihood() : Math.log(ll.getSsqErr());
        } else {
            return fn.isMaximumLikelihood() ? ll.getSsqErr() * ll.getFactor() : ll
                    .getSsqErr();
        }
    }

    @Override
    public ISsqFunction getSsqFunction() {
        return fn;
    }

    @Override
    public IFunction getFunction() {
        return fn;
    }
}
