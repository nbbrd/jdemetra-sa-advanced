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
package ec.demetra.ssf.implementations;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfMeasurement;
import java.util.Collection;

/**
 *
 * @author Jean Palate
 */
public class CompositeMeasurement implements ISsfMeasurement {

    public static ISsfMeasurement of(ISsf... ssf) {
        return CompositeMeasurement.of(0, ssf);
    }

    public static ISsfMeasurement of(double var, ISsf... ssf) {
        ISsfMeasurement[] l = new ISsfMeasurement[ssf.length];
        for (int i = 0; i < ssf.length; ++i) {
            l[i] = ssf[i].getMeasurement();
            if (l[i].hasErrors()) {
                return null;
            }
        }
        return new CompositeMeasurement(l, var);
    }
    public static ISsfMeasurement of(ISsfMeasurement... m) {
        return of(0, m);
    }
    
    public static ISsfMeasurement of(double var, ISsfMeasurement... m) {
        for (int i = 0; i < m.length; ++i) {
            if (m[i].hasErrors()) {
                return null;
            }
        }
        return new CompositeMeasurement(m, var);
    }

    public static ISsfMeasurement of(Collection<ISsf> ssf) {
        return CompositeMeasurement.of(0, ssf);
    }

    public static ISsfMeasurement of(double var, Collection<ISsf> ssf) {
        ISsfMeasurement[] l = new ISsfMeasurement[ssf.size()];
        int i = 0;
        for (ISsf s : ssf) {
            l[i] = s.getMeasurement();
            if (l[i].hasErrors()) {
                return null;
            }
            ++i;
        }
        return new CompositeMeasurement(l, var);
    }

    private final ISsfMeasurement[] measurements;
    private final int[] dim;
    private final int fdim;
    private final double var;
    private final DataBlock tmp;

    CompositeMeasurement(final ISsfMeasurement[] ms, double var) {
        this.measurements = ms;
        int n = ms.length;
        dim=new int[n];
        int tdim = 0;
        for (int i = 0; i < n; ++i) {
            dim[i]=ms[i].getStateDim();
            tdim += dim[i];
        }
        fdim = tdim;
        this.var = var;
        tmp = new DataBlock(fdim);
    }

    @Override
    public boolean isTimeInvariant() {
        for (int i = 0; i < measurements.length; ++i) {
            if (!measurements[i].isTimeInvariant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void Z(int pos, DataBlock z) {
        DataBlock cur = z.start();
        for (int i = 0; i < measurements.length; ++i) {
            cur.next(dim[i]);
            measurements[i].Z(pos, cur);
        }
    }

    @Override
    public boolean hasErrors() {
        return var != 0;
    }

    @Override
    public boolean hasError(int pos) {
        return var != 0;
    }

    @Override
    public double errorVariance(int pos) {
        return var;
    }

    @Override
    public double ZX(int pos, DataBlock m) {
        DataBlock cur = m.start();
        double x = 0;
        for (int i = 0; i < measurements.length; ++i) {
            cur.next(dim[i]);
            x += measurements[i].ZX(pos, cur);
        }
        return x;
    }

    @Override
    public double ZVZ(int pos, SubMatrix v) {
        SubMatrix D = v.topLeft();
        double x = 0;
        for (int i = 0; i < measurements.length; ++i) {
            int ni = dim[i];
            tmp.set(0);
            DataBlock buffer = tmp.start();
            D.next(ni, ni);
            x += measurements[i].ZVZ(pos, D);
            SubMatrix C = D.clone();
            for (int j = i + 1; j < measurements.length; ++j) {
                int nj = dim[j];
                buffer.next(nj);
                C.vnext(nj);
                measurements[j].ZM(pos, C, buffer);
                x += 2 * measurements[i].ZX(pos, buffer);
            }
        }
        return x;
    }

    @Override
    public void VpZdZ(int pos, SubMatrix V, double d) {
        tmp.set(0);
        Z(pos, tmp);
        DataBlockIterator cols = V.columns();
        DataBlock col = cols.getData();
        do {
            double cur = tmp.get(cols.getPosition());
            if (cur != 0) {
                col.addAY(d * cur, tmp);
            }
        } while (cols.next());
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        DataBlock cur = x.start();
        for (int i = 0; i < measurements.length; ++i) {
            cur.next(dim[i]);
            measurements[i].XpZd(pos, cur, d);
        }
    }

    @Override
    public int getStateDim() {
        return fdim;
    }

    @Override
    public boolean isValid() {
       for (int i = 0; i < measurements.length; ++i) {
            if (!measurements[i].isValid()) {
                return false;
            }
        }
        return true;
    }
}
