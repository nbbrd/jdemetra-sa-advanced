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
package be.nbb.demetra.modelling.outliers;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.linearfilters.RationalBackFilter;

/**
 *
 * @author Jean Palate
 */
public interface IOutlierVariable {

    public static class FilterRepresentation {

        public final RationalBackFilter filter;
        public final double correction;

        public FilterRepresentation(RationalBackFilter filter, double correction) {
            this.filter = filter;
            this.correction = correction;
        }
    }

    /**
     * Fills a buffer with the data corresponding to this variable. 
     * At entry, the buffer is initialized to 0.
     * The data corresponds to the range [start, start + buffer.getLength()[.
     * @param start The index corresponding to the beginning of the buffer
     * @param buffer The buffer that will contain the data
     */
    void data(int start, DataBlock buffer);

    /**
     * 
     * @return
     */
    String getCode();

    /**
     *
     * @return
     */
    int getPosition();

    /**
     *
     * @return
     */
    FilterRepresentation getFilterRepresentation();

}
