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

import ec.tstoolkit.data.IReadDataBlock;
import ec.demetra.realfunctions.IFunction;
import ec.demetra.realfunctions.IFunctionInstance;
import ec.demetra.realfunctions.IParametersDomain;
import ec.demetra.realfunctions.IParametricMapping;
import ec.demetra.realfunctions.ISsqFunction;
import ec.demetra.realfunctions.ISsqFunctionInstance;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
public class SsfFunction<S extends ISsf> implements IFunction, ISsqFunction {

    private final boolean mt, sym;
    final IParametricMapping<S> mapper;
    final ISsfData data;
    boolean ml = true, log=false, fast=false;

    /**
     *
     * @param ssf
     * @param data
     * @param symderivatives
     * @param mt
     * @param mapper
     */
    public SsfFunction(ISsfData data, IParametricMapping<S> mapper, boolean symderivatives, boolean mt) {
        this.data = data;
        this.mapper = mapper;
        this.mt = mt;
        this.sym = symderivatives;
    }

    public boolean isMaximumLikelihood() {
        return ml;
    }

    public void setMaximumLikelihood(boolean ml) {
        this.ml = ml;
    }

    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    public boolean isFast() {
        return fast;
    }

    public void setFast(boolean fast) {
        this.fast = fast;
    }
    
    @Override
    public IFunctionInstance evaluate(IReadDataBlock parameters) {
        return new SsfFunctionInstance<>(this, parameters);
    }

    /**
     *
     * @return
     */
    @Override
    public IParametersDomain getDomain() {
        return mapper;
    }

    @Override
    public ISsqFunctionInstance ssqEvaluate(IReadDataBlock parameters) {
        return new SsfFunctionInstance<>(this, parameters);
    }
}
