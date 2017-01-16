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

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.utilities.IntList;
import java.text.DecimalFormat;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoublePredicate;
import java.util.function.IntToDoubleFunction;

/**
 * Read only data block. A data block is just an array of doubles
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IArrayOfDoublesReader {

    /**
     * Gets the data at a given position
     *
     * @param idx 0-based position of the data. idx must belong to [0,
     * getLength()[
     * @return The idx-th element
     */
    double get(int idx);

    /**
     * Gets the number of data in the array
     *
     * @return The number of data (&gt= 0).
     */
    int getLength();

    default IntToDoubleFunction asFunction() {
        return i -> get(i);
    }

    /**
     * Copies the data into a given buffer
     *
     * @param buffer The buffer that will receive the data.
     * @param start The start position in the buffer for the copy. The data will
     * be copied in the buffer at the indexes [start, start+getLength()[. The
     * length of the buffer is not checked (it could be larger than this array.
     */
    default void copyTo(double[] buffer, int start) {
        int n = getLength();
        for (int i = 0; i < n; ++i) {
            buffer[start + i] = get(i);
        }
    }

    /**
     * Makes an extract of this data block.
     *
     * @param start The position of the first extracted item.
     * @param length The number of extracted items. The size of the result could
     * be smaller than length, if the data block doesn't contain enough items.
     * Cannot be null.
     * @return A new (read only) data block. Cannot be null (but the length of
     * the result could be 0.
     */
    IArrayOfDoublesReader extract(int start, int length);

    default boolean check(DoublePredicate pred) {
        int n = getLength();
        for (int i = 0; i < n; ++i) {
            if (!pred.test(get(i))) {
                return false;
            }
        }
        return true;
    }

    default int count(DoublePredicate pred) {
        int n = getLength();
        int m = 0;
        for (int i = 0; i < n; ++i) {
            if (pred.test(get(i))) {
                m++;
            }
        }
        return m;
    }

    default int first(DoublePredicate pred) {
        int n = getLength();
        for (int i = 0; i < n; ++i) {
            if (pred.test(get(i))) {
                return i;
            }
        }
        return n;
    }

    default int last(DoublePredicate pred) {
        int n = getLength();
        for (int i = n - 1; i >= 0; --i) {
            if (pred.test(get(i))) {
                return i;
            }
        }
        return -1;
    }

    default double computeRecursively(DoubleBinaryOperator fn, final double initial) {
        double cur = initial;
        int n = getLength();
        for (int i = n - 1; i >= 0; --i) {
            cur = fn.applyAsDouble(cur, get(i));
        }
        return cur;
    }

    default int[] search(DoublePredicate pred) {
        IntList list = new IntList();
        int n = getLength();
        for (int j = 0; j < n; ++j) {
            double cur = get(j);
            if (pred.test(cur)) {
                list.add(j);
            }
        }
        return list.toArray();
    }

    default double sum() {
        int n = getLength();
        double s = 0;
        for (int i = 0; i < n; i++) {
            double cur = get(i);
            if (Double.isFinite(cur)) {
                s += cur;
            }
        }
        return s;
    }

    default double ssq() {
        int n = getLength();
        double s = 0;
        for (int i = 0; i < n; i++) {
            double cur = get(i);
            if (Double.isFinite(cur)) {
                s += cur * cur;
            }
        }
        return s;
    }

    default double ssqc(double mean) {
        int n = getLength();
        double s = 0;
        for (int i = 0; i < n; i++) {
            double cur = get(i) - mean;
            if (Double.isFinite(cur)) {
                s += cur * cur;
            }
        }
        return s;
    }

    default double average() {
        int n = getLength();
        int m = 0;
        double s = 0;
        for (int i = 0; i < n; i++) {
            double cur = get(i);
            if (Double.isFinite(cur)) {
                s += cur;
            } else {
                m++;
            }
        }
        return s / (n - m);
    }

    default double dot(IArrayOfDoublesReader data) {
        int n = getLength();
        double s = 0;
        for (int i = 0; i < n; i++) {
            s += get(i) * data.get(i);
        }
        return s;
    }

    default double distance(IArrayOfDoublesReader data) {
        double scale = 0;
        double ssq = 1;
        int n = getLength();
        for (int i = 0; i < n; ++i) {
            double x = get(i), y = data.get(i);
            if (Double.compare(x, y) != 0) {
                double d = x - y;
                if (d != 0) {
                    double absxi = Math.abs(d);
                    if (scale < absxi) {
                        double s = scale / absxi;
                        ssq = 1 + ssq * s * s;
                        scale = absxi;
                    } else {
                        double s = absxi / scale;
                        ssq += s * s;
                    }
                }
            }
        }
        return scale * Math.sqrt(ssq);
    }

    default boolean isMissing(int idx) {
        return !Double.isFinite(get(idx));
    }

    /**
     * Computes the norm1 of this src block (sum of the absolute values)
     *
     * @return Returns min{|src(i)|}
     */
    default double norm1() {
        int n = getLength();
        double nrm = 0;
        for (int i = 0; i < n; ++i) {
            nrm += Math.abs(get(i));
        }
        return nrm;
    }

    /**
     * Computes the euclidian norm of the src block. Based on the "dnrm2" Lapack
     * function.
     *
     * @return The euclidian norm (&gt=0).
     */
    default double norm2() {
        int n = getLength();
        switch (n) {
            case 0:
                return 0;
            case 1:
                return Math.abs(get(0));
            default:
                double scale = 0;
                double ssq = 1;
                for (int i = 0; i < n; ++i) {
                    double cur = get(i);
                    if (cur != 0) {
                        double absxi = Math.abs(cur);
                        if (scale < absxi) {
                            double s = scale / absxi;
                            ssq = 1 + ssq * s * s;
                            scale = absxi;
                        } else {
                            double s = absxi / scale;
                            ssq += s * s;
                        }
                    }
                }
                return scale * Math.sqrt(ssq);
        }
    }

    /**
     * Computes the infinite-norm of this src block
     *
     * @return Returns min{|src(i)|}
     */
    default double normInf() {
        int n = getLength();
        if (n == 0) {
            return 0;
        } else {
            double nrm = Math.abs(get(0));
            for (int i = 1; i < n; ++i) {
                double tmp = Math.abs(get(i));
                if (tmp > nrm) {
                    nrm = tmp;
                }
            }
            return nrm;
        }
    }

    public static double round(double r, final int ndec) {
        if (ndec < 0) {
            throw new IllegalArgumentException("Negative rounding parameter");
        }
        double f = 1;
        for (int i = 0; i < ndec; ++i) {
            f *= 10;
        }
        if (Double.isFinite(r)) {
            double v = r;
            if (ndec > 0) {
                r = Math.round(v * f) / f;
            } else {
                r = Math.round(v);
            }
        }
        return r;
    }

    /**
     * Counts the number of identical consecutive values.
     *
     * @return Missing values are omitted.
     */
    default int getRepeatCount() {
        int i = 0;
        int n = getLength();
        while ((i < n) && !Double.isFinite(get(i))) {
            ++i;
        }
        if (i == n) {
            return 0;
        }
        int c = 0;
        double prev = get(i++);
        for (; i < n; ++i) {
            double cur = get(i);
            if (Double.isFinite(cur)) {
                if (cur == prev) {
                    ++c;
                } else {
                    prev = cur;
                }
            }
        }
        return c;
    }

    public static String toString(IArrayOfDoublesReader rd){
        StringBuilder builder = new StringBuilder();
        int n=rd.getLength();
        if (n>0) {
            builder.append(rd.get(0));
            for (int i = 1; i <n; ++i) {
                builder.append('\t').append(rd.get(i));
           }
        }
        builder.append(System.lineSeparator());
        return builder.toString();
    }
    
    public static String toString(IArrayOfDoublesReader rd, String fmt){
        StringBuilder builder = new StringBuilder();
        int n=rd.getLength();
        if (n>0) {
            builder.append(new DecimalFormat(fmt).format(rd.get(0)));
            for (int i = 1; i <n; ++i) {
                builder.append('\t').append(new DecimalFormat(fmt).format(rd.get(i)));
           }
        }
        builder.append(System.lineSeparator());
        return builder.toString();
    }

}
