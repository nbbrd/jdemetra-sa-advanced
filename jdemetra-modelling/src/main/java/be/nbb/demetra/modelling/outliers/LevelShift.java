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
public class LevelShift implements IOutlierVariable {

    public static class Factory implements IOutlierFactory {

        private boolean zeroEnded;

        @Override
        public IOutlierVariable create(int position) {
            return new LevelShift(position, zeroEnded);
        }

        @Override
        public int excludingZoneAtStart() {
            return 2;
        }

        @Override
        public int excludingZoneAtEnd() {
            return 1;
        }

        @Override
        public String getOutlierCode() {
            return "LS";
        }

        /**
         * @return the zeroEnded
         */
        public boolean isZeroEnded() {
            return zeroEnded;
        }

        /**
         * @param zeroEnded the zeroEnded to set
         */
        public void setZeroEnded(boolean zeroEnded) {
            this.zeroEnded = zeroEnded;
        }

    }

    private final int pos;
    protected final boolean zeroEnded;

    public LevelShift(int pos, boolean zeroEnded) {
        this.pos = pos;
        this.zeroEnded = zeroEnded;
    }

    @Override
    public void data(int start, DataBlock data) {
        int n = data.getLength();
        double Zero = zeroEnded ? -1 : 0, One = zeroEnded ? 0 : 1;
        int xpos = pos - start;
        if (xpos <= 0) {
            data.set(One);
        } else if (xpos >= n) {
            data.set(Zero);
        } else {
            data.range(0, xpos).set(Zero);
            data.range(xpos, n).set(One);
        }
    }

    @Override
    public String getCode() {
        return "LS";
    }

    @Override
    public int getPosition() {
        return pos;
    }

    @Override
    public FilterRepresentation getFilterRepresentation() {
        return new FilterRepresentation(new RationalBackFilter(
                BackFilter.ONE, BackFilter.D1), zeroEnded ? -1 : 0);
    }

    /**
     * @return the zeroEnded
     */
    public boolean isZeroEnded() {
        return zeroEnded;
    }

    @Override
    public String toString(){
        return "LS-"+pos;
    }
}
