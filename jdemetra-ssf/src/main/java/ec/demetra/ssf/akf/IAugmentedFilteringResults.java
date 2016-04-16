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
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.demetra.ssf.univariate.IFilteringResults;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;

/**
 *
 * @author Jean Palate
 */
public interface IAugmentedFilteringResults extends IFilteringResults {

    /**
     *
     * @param pos
     */
    void close(int pos);

    /**
     *
     * @param t
     * @param pe
     */
    void save(int t, AugmentedUpdateInformation pe);

    /**
     *
     * @param state
     */
    void save(int t, AugmentedState state, StateInfo info);

    default SubMatrix B(int pos) {
        return null;
    }

    default DataBlock E(int pos) {
        return null;
    }

    boolean canCollapse();

    boolean collapse(int pos, AugmentedState state);

    int getCollapsingPosition();

    QAugmentation getAugmentation();
}
