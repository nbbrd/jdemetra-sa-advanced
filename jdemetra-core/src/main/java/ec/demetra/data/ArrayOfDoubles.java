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
package ec.demetra.data;

/**
 *
 * @author Jean Palate
 */
public abstract class ArrayOfDoubles implements IArrayOfDoubles{

    private static final IArrayOfDoubles EMPTY = new FullArray(0);

    public static IArrayOfDoubles create(int n) {
        return n == 0 ? EMPTY : new FullArray(n);
    }

    public static IArrayOfDoubles of(double[] data) {
        return new FullArray(data);
    }

    public static IArrayOfDoubles of(double[] data, int start, int end) {
        return new PartialArray(data, start, end);
    }

    public static IArrayOfDoubles of(double[] data, int start) {
        return new PartialArray(data, start);
    }

    public static IArrayOfDoubles of(double[] data, int start, int end, int inc) {
        if ((end - start) % inc != 0) {
            throw new IllegalArgumentException("Invalid DoubleArray");
        }
        return new DataBlock(data, start, end, inc);
    }

    private static class FullArray extends ArrayOfDoubles {

        private final double[] data;

        FullArray(double[] data) {
            this.data = data;
        }

        FullArray(int n) {
            this.data = new double[n];
        }

        @Override
        public void copyFrom(double[] buffer, int start) {
            System.arraycopy(buffer, start, data, 0, data.length);
        }

        @Override
        public IArrayOfDoubles extract(int start, int length) {
            return ArrayOfDoubles.of(data, start, start + length);
        }

        @Override
        public IArrayOfDoubles reverse(){
            return new DataBlock(data, data.length-1, -1, -1);
        }

        @Override
        public void set(int idx, double value) {
            data[idx] = value;
        }

        @Override
        public double get(int idx) {
            return data[idx];
        }

        @Override
        public int getLength() {
            return data.length;
        }
        
    }

    private static class PartialArray extends ArrayOfDoubles {

        private final double[] data;
        private final int i0, i1;

        PartialArray(double[] data, int start, int end) {
            this.data = data;
            i0 = start;
            i1 = end;
        }

        PartialArray(double[] data, int start) {
            this.data = data;
            i0 = start;
            i1 = data.length;
        }

        @Override
        public void copyFrom(double[] buffer, final int start) {
            System.arraycopy(buffer, start, data, i0, i1 - i0);
        }

        @Override
        public IArrayOfDoubles extract(int start, int length) {
            int nstart = i0 + start;
            int nend = nstart + length;
            if (nend > i1) {
                throw new IllegalArgumentException("Double array: too large extract");
            }
            return ArrayOfDoubles.of(data, nstart, nend);
        }

        @Override
        public IArrayOfDoubles reverse(){
            return new DataBlock(data, i1-1, i0-1, -1);
        }

        @Override
        public void set(int idx, double value) {
            data[i0 + idx] = value;
        }

        @Override
        public double get(int idx) {
            return data[i0 + idx];
        }

        @Override
        public int getLength() {
            return i1 - i0;
        }

    }

    private static class DataBlock extends ArrayOfDoubles {

        private final double[] data;
        private final int i0, i1, inc;

        DataBlock(double[] data, int start, int end, int inc) {
            this.data = data;
            i0 = start;
            i1 = end;
            this.inc = inc;
        }

        @Override
        public void copyFrom(double[] buffer, int start) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public IArrayOfDoubles extract(int start, int length) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
        @Override
        public IArrayOfDoubles reverse(){
            return new DataBlock(data, i1-inc, i0-inc, -inc);
        }

        @Override
        public void set(int idx, double value) {
            data[i0 + idx * inc] = value;
        }

        @Override
        public double get(int idx) {
            return data[i0 + idx * inc];
        }

        @Override
        public int getLength() {
            return (i1 - i0) / inc;
        }

    }
}
