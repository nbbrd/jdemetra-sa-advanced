/*
 * Copyright 2013-2014 National Bank of Belgium
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
package ec.demetra.ssf.dk;

import data.Models;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.demetra.ssf.ResultsRange;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.SsfData;
import ec.tstoolkit.data.DataBlockIterator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class FastDiffuseFilterTest {

    public FastDiffuseFilterTest() {
    }

    @Test
    @Disabled
    public void stressTestUcarima() {
        ISsf ssf = Models.ssfUcarima;
        SsfData data = Models.ssfX;
        DefaultDiffuseFilteringResults fresults = DkToolkit.filter(ssf, data, false);
        Matrix x = new Matrix(data.getLength(), 100);
        DkFilter filter = new DkFilter(ssf, fresults, new ResultsRange(0, data.getLength()));
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            x.randomize(0);
            filter.filter(x.all());
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        int nc = filter.getOutputLength(data.getLength());
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            x.randomize(0);
            DataBlockIterator columns = x.columns();
            DataBlock col = columns.getData();
            do {
                DataBlock coll = new DataBlock(nc);
                filter.transform(col, coll);
            } while (columns.next());
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    public void testUcarima() {
        ISsf ssf = Models.ssfUcarima;
        SsfData data = Models.ssfX;
        DefaultDiffuseFilteringResults fresults = DkToolkit.filter(ssf, data, false);
        Matrix x = new Matrix(data.getLength(), 10);
        x.randomize();
        x.column(0).copy(data);
        DkFilter filter = new DkFilter(ssf, fresults, new ResultsRange(0, data.getLength()));
        filter.filter(x.all());
        assertTrue(new DataBlock(fresults.errors(true, false)).distance(x.column(0)) < 1e-6);
        DataBlock xout = new DataBlock(filter.getOutputLength(data.getLength()));
        filter.transform(data, xout);
        assertTrue(new DataBlock(fresults.errors(true, true)).distance(xout) < 1e-6);

    }
}
