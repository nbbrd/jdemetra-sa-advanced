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
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.RationalBackFilter;

/**
 *
 * @author Jean Palate
 */
public class AdditiveOutlier implements IOutlierVariable {

    public static class Factory implements IOutlierFactory {

        @Override
        public IOutlierVariable create(int position) {
            return new AdditiveOutlier(position);
        }

        @Override
        public int excludingZoneAtStart() {
            return 0;
        }

        @Override
        public int excludingZoneAtEnd() {
            return 0;
        }

        @Override
        public String getOutlierCode() {
            return "AO";
        }

    }

    private final int pos;

    public AdditiveOutlier(int pos) {
        this.pos=pos;
    }

    @Override
    public void data(int start, DataBlock data) {
        int dpos = pos - start;
        if (dpos >= 0 && dpos < data.getLength()) {
            data.set(dpos, 1);
        }
    }

    @Override
    public String getCode() {
        return "AO";
    }

    @Override
    public int getPosition() {
        return pos;
    }

    @Override
    public FilterRepresentation getFilterRepresentation() {
        return new FilterRepresentation(new RationalBackFilter(
                BackFilter.ONE, BackFilter.ONE), 0);
    }

    @Override
    public String toString(){
        return "AO-"+pos;
    }
    
}
