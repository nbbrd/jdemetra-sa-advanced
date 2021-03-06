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
package ec.demetra.ssf.akf;

import ec.demetra.ssf.StateInfo;
import ec.demetra.ssf.multivariate.IMultivariateSsf;
import ec.demetra.ssf.multivariate.IMultivariateSsfData;

/**
 *
 * @author Jean Palate
 */
public interface IMultivariateAugmentedFilteringResults {
    /**
     *
     */
    void close();

    /**
     *
     * @param ssf
     * @param data
     */
    void open(IMultivariateSsf ssf, IMultivariateSsfData data);

    /**
     *
     * @param t
     * @param pe
     */
    void save(int t, AugmentedPredictionErrors pe);
    
    /**
     *
     * @param t
     * @param state
     */
    void save(int t, AugmentedState state, StateInfo info);
    
    boolean canCollapse();
    
    boolean collapse(AugmentedState state);
}
