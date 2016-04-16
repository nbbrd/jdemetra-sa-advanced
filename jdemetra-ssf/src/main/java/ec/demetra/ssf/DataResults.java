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
package ec.demetra.ssf;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;

/**
 *
 * @author Jean Palate
 */
public class DataResults implements IReadDataBlock{

    double[] data;
    int start;
    int nused;

    /**
     *
     */
    public DataResults() {
    }

    /**
     *
     */
    public void clear() {
        data = null;
        nused=0;
    }

    public void prepare(final int start, final int end) {
        this.start = start;
        data = new double[end - start];
        for (int i=0; i<data.length; ++i)
            data[i]=Double.NaN;
    }
    
    public DataBlock all(){
        return new DataBlock(data,0 , nused, 1);
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public double get(final int t) {
        if (data == null || t < start) {
            return Double.NaN;
        } else {
            return data[t - start];
        }
    }

    public void save(final int t, final double x) {
        int st = t - start;
        if (st < 0) {
            return;
        }
        checkSize(st+1);
        data[st] = x;
    }

    /**
     *
     * @return
     */
    public int getStartSaving() {
        return start;
    }

    /**
     *
     * @param p
     */
    public void setStartSaving(int p) {
        start = p;
        data = null;
    }

    private void checkSize(int size) {
        if (nused<size)
            nused=size;            
        int cursize = data == null ? 0 : data.length;
        if (size > cursize) {
            int nsize = Math.max(DataBlockStorage.calcSize(size), cursize << 1);
            double[] tmp = new double[nsize];
            if (cursize > 0) {
                System.arraycopy(data, 0, tmp, 0, cursize);
            }
            for (int i = cursize; i < nsize; ++i) {
                tmp[i] = Double.NaN;
            }
            data = tmp;
        }
    }

    @Override
    public void copyTo(double[] buffer, int start) {
        System.arraycopy(data, 0, buffer, start, nused);
    }

    @Override
    public int getLength() {
        return nused;
    }

    @Override
    public IReadDataBlock rextract(int t0, int length) {
        return new ReadDataBlock(data, t0, length);
    }
    
    public void rescale(double factor){
        if (factor == 1)
            return;
        for (int i=0; i<nused; ++i){
            data[i]*=factor;
        }
    }
}
