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
package ec.demetra.ssf.univariate;

import data.Models;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.demetra.ssf.ResultsRange;
import ec.demetra.ssf.dk.DefaultDiffuseFilteringResults;
import ec.demetra.ssf.dk.DkToolkit;
import ec.tstoolkit.data.DataBlockIterator;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class FastFilterTest {

    final static int N = 100000;

    public FastFilterTest() {
    }

    @Test
    @Ignore
    public void stressTestUcarima() {
        ISsf ssf = Models.ssfUcarima;
        SsfData data = Models.ssfXRandom;
        DefaultDiffuseFilteringResults fresults = DkToolkit.filter(ssf, data, false);
        Matrix x = new Matrix(data.getLength()-fresults.getEndDiffusePosition(), 1);
        FastFilter filter = new FastFilter(ssf, fresults, new ResultsRange(fresults.getEndDiffusePosition(), data.getLength()));
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000; ++i) {
            filter.filter(x.all());
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        x.randomize(0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000; ++i) {
            DataBlockIterator columns = x.columns();
            DataBlock col = columns.getData();
            do {
                DataBlock coll = new DataBlock(filter.getOutputLength(col.getLength()));
                filter.transform(col, coll);
            } while (columns.next());
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    public void testUcarima() {
        ISsf ssf = Models.ssfUcarima;
        SsfData data = Models.ssfXRandom;
        DefaultDiffuseFilteringResults fresults = DkToolkit.filter(ssf, data, false);
        Matrix x = new Matrix(data.getLength(), 1);
        x.column(0).copy(data);
        FastFilter filter = new FastFilter(ssf, fresults, new ResultsRange(fresults.getEndDiffusePosition(), data.getLength()));
        filter.filter(x.subMatrix(fresults.getEndDiffusePosition(), -1, 0, -1));
        DataBlock sel = x.column(0).drop(fresults.getEndDiffusePosition(), 0).select((z) -> Double.isFinite(z));
        DataBlock in = new DataBlock(data).drop(fresults.getEndDiffusePosition(), 0);
        DataBlock xsel = new DataBlock(filter.getOutputLength(in.getLength()));
        filter.transform(in, xsel);
        assertTrue(sel.distance(xsel)<1e-9);

    }
}
