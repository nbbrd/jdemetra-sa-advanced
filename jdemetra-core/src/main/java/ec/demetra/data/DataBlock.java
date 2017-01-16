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

import ec.demetra.maths.matrices.SubMatrix;
import java.util.Iterator;

/**
 *
 * @author Jean Palate
 */
public abstract class DataBlock implements IArrayOfDoubles, Iterable<DataBlock.Cell> {

    public static final DataBlock EMPTY = new FullArray(0);

    public static DataBlock create(int n) {
        return n == 0 ? EMPTY : new FullArray(n);
    }

    public static DataBlock of(double[] data) {
        return new FullArray(data);
    }

    public static DataBlock of(double[] data, int start, int end) {
        return new PartialArray(data, start, end);
    }

    public static DataBlock of(double[] data, int start) {
        return new PartialArray(data, start);
    }

    public static DataBlock of(double[] data, int start, int end, int inc) {
        if (inc == 1) {
            return new PartialArray(data, start, end);
        }
        if ((end - start) % inc != 0) {
            throw new IllegalArgumentException("Invalid DoubleArray");
        }
        return new Block(data, start, end, inc);
    }

    public abstract double[] getData();

    public abstract int getStartPosition();

    public abstract int getEndPosition();

    public abstract int getLastPosition();

    public abstract int getIncrement();

    public abstract DataBlock shrink(int nbeg, int nend);

    public abstract DataBlock bshrink();

    public abstract DataBlock eshrink();

    public abstract DataBlock expand(int nbeg, int nend);

    public abstract DataBlock bexpand();

    public abstract DataBlock eexpand();

    public abstract DataBlock slide(int n);

    public abstract DataBlock reverse();

    public abstract DataBlock next(int n);

    public abstract DataBlock previous(int n);
    
        @Override
    public String toString() {
        return IArrayOfDoublesReader.toString(this);
    }

    public String toString(String fmt) {
        return IArrayOfDoublesReader.toString(this, fmt);
    }



    public static class Cell {

        private double[] data;
        private final int inc;
        private int pos;

        private Cell(double[] data, int pos, int inc) {
            this.data = data;
            this.inc = inc;
            this.pos = pos;
        }

        public Cell right() {
            pos += inc;
            return this;
        }

        public Cell left() {
            pos -= inc;
            return this;
        }

        public Cell right(int n) {
            pos += n * inc;
            return this;
        }

        public Cell left(int n) {
            pos -= n * inc;
            return this;
        }

        public double value() {
            return data[pos];
        }

        public Cell set(double nvalue) {
            data[pos] = nvalue;
            return this;
        }

        public Cell setAndIncrement(double nvalue) {
            data[pos++] = nvalue;
            return this;
        }

        public Cell setAndDecrement(double nvalue) {
            data[pos--] = nvalue;
            return this;
        }
    }

    private static class CellIterator implements Iterator<Cell> {

        private final Cell cell;
        private final int last;

        private CellIterator(DataBlock owner) {
            this.cell = new Cell(owner.getData(), owner.getStartPosition(), owner.getIncrement());
            cell.left();
            last = owner.getLastPosition();
        }

        @Override
        public boolean hasNext() {
            return cell.pos != last;
        }

        @Override
        public Cell next() {
            return cell.right();
        }

    }

    @Override
    public Iterator<Cell> iterator() {
        return new CellIterator(this);
    }

    private static class FullArray extends DataBlock {

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
            return DataBlock.of(data, start, start + length);
        }

        @Override
        public DataBlock reverse() {
            return new Block(data, data.length - 1, -1, -1);
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

        @Override
        public DataBlock slide(int n) {
            throw new java.lang.UnsupportedOperationException();
        }

        @Override
        public DataBlock next(int n) {
            throw new java.lang.UnsupportedOperationException();
        }

        @Override
        public double[] getData() {
            return data;
        }

        @Override
        public int getStartPosition() {
            return 0;
        }

        @Override
        public int getEndPosition() {
            return data.length;
        }

        @Override
        public int getLastPosition() {
            return data.length - 1;
        }

        @Override
        public int getIncrement() {
            return 1;
        }

        @Override
        public DataBlock shrink(int nbeg, int nend) {
            return new PartialArray(data, nbeg, data.length - nend);
        }

        @Override
        public DataBlock bshrink() {
            return new PartialArray(data, 1, data.length);
        }

        @Override
        public DataBlock eshrink() {
            return new PartialArray(data, 0, data.length - 1);
        }

        @Override
        public DataBlock previous(int n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataBlock expand(int nbeg, int nend) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataBlock bexpand() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataBlock eexpand() {
            throw new UnsupportedOperationException();
        }

    }

    private static class PartialArray extends DataBlock {

        private final double[] data;
        private int beg, end;

        PartialArray(double[] data, int start, int end) {
            this.data = data;
            beg = start;
            this.end = end;
        }

        PartialArray(double[] data, int start) {
            this.data = data;
            beg = start;
            end = data.length;
        }

        @Override
        public void copyFrom(double[] buffer, final int start) {
            System.arraycopy(buffer, start, data, beg, end - beg);
        }

        @Override
        public IArrayOfDoubles extract(int start, int length) {
            int nstart = beg + start;
            int nend = nstart + length;
            if (nend > end) {
                throw new IllegalArgumentException("Double array: too large extract");
            }
            return DataBlock.of(data, nstart, nend);
        }

        @Override
        public DataBlock reverse() {
            return new Block(data, end - 1, beg - 1, -1);
        }

        @Override
        public void set(int idx, double value) {
            data[beg + idx] = value;
        }

        @Override
        public double get(int idx) {
            return data[beg + idx];
        }

        @Override
        public int getLength() {
            return end - beg;
        }

        @Override
        public DataBlock slide(int n) {
            if (n > 0) {
                if (end + n <= data.length) {
                    beg += n;
                    end += n;
                } else {
                    throw new java.lang.UnsupportedOperationException();
                }
            } else if (n < 0) {
                if (beg + n >= 0) {
                    beg += n;
                    end += n;
                } else {
                    throw new java.lang.UnsupportedOperationException();
                }
            }
            return this;
        }

        @Override
        public DataBlock next(int n) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public double[] getData() {
            return data;
        }

        @Override
        public int getStartPosition() {
            return beg;
        }

        @Override
        public int getEndPosition() {
            return end;
        }

        @Override
        public int getLastPosition() {
            return end - 1;
        }

        @Override
        public int getIncrement() {
            return 1;
        }

        @Override
        public DataBlock shrink(int nbeg, int nend) {
            if (end-beg < nbeg+nend)
                throw new IllegalArgumentException();
            beg += nbeg;
            end -= nend;
            return this;
        }

        @Override
        public DataBlock bshrink() {
            if (beg == end)
                throw new IllegalArgumentException();
            ++beg;
            return this;
        }

        @Override
        public DataBlock eshrink() {
            if (beg == end)
                throw new IllegalArgumentException();
            --end;
            return this;
        }

        @Override
        public DataBlock previous(int n) {
            if (beg < n)
                throw new IllegalArgumentException();
            return new PartialArray(data, beg-n, beg);
         }

        @Override
        public DataBlock expand(int nbeg, int nend) {
            if (beg < nbeg || end > data.length - nend) {
                throw new UnsupportedOperationException();
            }

            beg -= nbeg;
            end += nend;
            return this;
        }

        @Override
        public DataBlock bexpand() {
            if (beg == 0) {
                throw new UnsupportedOperationException();
            }

            --beg;
            return this;
        }

        @Override
        public DataBlock eexpand() {
            if (end == data.length) {
                throw new UnsupportedOperationException();
            }

            ++end;
            return this;
        }

    }

    private static class Block extends DataBlock {

        private final double[] data;
        private int beg, end;
        private final int inc;

        Block(double[] data, int start, int end, int inc) {
            this.data = data;
            beg = start;
            this.end = end;
            this.inc = inc;
        }

        @Override
        public void copyFrom(double[] buffer, int start) {
            for (int i = beg, j = start; i != end; i += inc, ++j) {
                data[i] = buffer[j];
            }
        }

        @Override
        public IArrayOfDoubles extract(int start, int length) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public DataBlock reverse() {
            return new Block(data, end - inc, beg - inc, -inc);
        }

        @Override
        public void set(int idx, double value) {
            data[beg + idx * inc] = value;
        }

        @Override
        public double get(int idx) {
            return data[beg + idx * inc];
        }

        @Override
        public int getLength() {
            return (end - beg) / inc;
        }

        @Override
        public DataBlock slide(int n) {
            if (n > 0) {
                if (end + n <= data.length + inc) {
                    beg += n;
                    end += n;
                } else {
                    throw new java.lang.ArrayIndexOutOfBoundsException();
                }
            } else if (n < 0) {
                if (beg + n >= 0) {
                    beg += n;
                    end += n;
                } else {
                    throw new ArrayIndexOutOfBoundsException();
                }
            }
            return this;
        }

        @Override
        public DataBlock next(int n) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        /**
         * Shrinks the src block by 1 src at the beginning of the block
         *
         * @return true if the src block has been correctly shrunk, false
         * otherwise.
         */
        @Override
        public DataBlock bshrink() {
            if (beg != end) {
                beg += inc;
                return this;
            } else {
                throw new ArrayIndexOutOfBoundsException();
            }
        }

        /**
         * Shrinks the src block by 1 src at the end of the block
         *
         * @return true if the src block has been correctly shrunk, false
         * otherwise.
         */
        @Override
        public DataBlock eshrink() {
            if (beg != end) {
                end -= inc;
                return this;
            } else {
                throw new ArrayIndexOutOfBoundsException();
            }
        }

        /**
         * Shrinks the current src block by removing src at the beginning and at
         * the end of the src block.
         *
         * @param nbeg Number of elements removed at the beginning of this src
         * block
         * @param nend Number of elements removed at the end of this src block
         * @return true if the src block has been correctly shrunk, false
         * otherwise.
         */
        @Override
        public DataBlock shrink(int nbeg, int nend) {
            if (nbeg + nend <= getLength()) {
                beg += inc * nbeg;
                end -= inc * nend;
                return this;
            } else {
                throw new ArrayIndexOutOfBoundsException();
            }
        }

        /**
         * Expands the current src block by adding src at the beginning or at
         * the end of the src block. The buffer must be larger enough.
         *
         * @param nbeg Number of elements added at the beginning of this src
         * block
         * @param nend Number of elements added at the end of this src block
         * @return true if the src block has been correctly expanded, false
         * otherwise.
         */
        @Override
        public DataBlock expand(int nbeg, int nend) {
            int xbeg = beg - nbeg * inc;
            int xend = end + nend * inc;

            if (xbeg < 0 || xend > data.length) {
                throw new UnsupportedOperationException();
            } else {
                beg = xbeg;
                end = xend;
                return this;
            }
        }

        @Override
        public double[] getData() {
            return data;
        }

        @Override
        public int getStartPosition() {
            return beg;
        }

        @Override
        public int getEndPosition() {
            return end;
        }

        @Override
        public int getLastPosition() {
            return end - inc;
        }

        @Override
        public int getIncrement() {
            return inc;
        }

        @Override
        public DataBlock previous(int n) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public DataBlock bexpand() {
            if (beg - inc < 0) {
                throw new UnsupportedOperationException();
            }
            beg -= inc;
            return this;
        }

        @Override
        public DataBlock eexpand() {
            if (end + inc > data.length) {
                throw new UnsupportedOperationException();
            }
            end += inc;
            return this;
        }
    }
}
