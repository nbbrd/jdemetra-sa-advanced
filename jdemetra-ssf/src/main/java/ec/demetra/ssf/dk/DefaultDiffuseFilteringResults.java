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
package ec.demetra.ssf.dk;

import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.DefaultFilteringResults;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.demetra.ssf.DataBlockResults;
import ec.demetra.ssf.DataResults;
import ec.demetra.ssf.MatrixResults;
import ec.demetra.ssf.State;
import ec.demetra.ssf.StateInfo;
import ec.tstoolkit.data.IReadDataBlock;

/**
 *
 * @author Jean Palate
 */
public class DefaultDiffuseFilteringResults extends DefaultFilteringResults implements IDiffuseFilteringResults {

    private final DataBlockResults Ci;
    private final MatrixResults Pi;
    private final DataResults fi;
    private int enddiffuse;

    private DefaultDiffuseFilteringResults(boolean var) {
        super(var);
        Ci = new DataBlockResults();
        fi=new DataResults();
        Pi = var ? new MatrixResults() : null;
    }

    public static DefaultDiffuseFilteringResults full() {
        return new DefaultDiffuseFilteringResults(true);
    }

    public static DefaultDiffuseFilteringResults light() {
        return new DefaultDiffuseFilteringResults(false);
    }
    
    @Override
    public void prepare(ISsf ssf, final int start, final int end) {
        super.prepare(ssf, start, end);
        int dim = ssf.getStateDim(), n = ssf.getDynamics().getNonStationaryDim();
        fi.prepare(start, n);
        Ci.prepare(dim, start, n);
        if (Pi != null) {
            Pi.prepare(dim, start, n);
        }
    }

    @Override
    public void save(int t, DiffuseUpdateInformation pe) {
        super.save(t, pe);
        fi.save(t, pe.getDiffuseNorm2());
        Ci.save(t, pe.Mi());
    }

    @Override
    public void close(int pos) {
        enddiffuse = pos;
    }

    @Override
    public void save(final int t, final DiffuseState state, final StateInfo info) {
        if (info != StateInfo.Forecast) {
            return;
        }
        super.save(t, state, info);
        if (Pi != null) {
            Pi.save(t, state.Pi());
        }
    }

    @Override
    public IReadDataBlock errors(boolean normalized, boolean clean) {
        DataBlock r = new DataBlock(errors());
        // set diffuse elements to Double.NaN
        r.range(0, enddiffuse).apply(fi.rextract(0, enddiffuse), (x,y)->y!=0 ? Double.NaN : x);
        if (normalized) {
            IReadDataBlock allf = errorVariances();
            r.apply(allf, (x, y) -> Double.isFinite(x) && Double.isFinite(y) ? x / Math.sqrt(y) : Double.NaN);
        }
        if (clean){
            r=r.select((x)->Double.isFinite(x));
        }
        return r;
    }

    @Override
    public double diffuseNorm2(int pos) {
        return fi.get(pos);
    }

    @Override
    public DataBlock Mi(int pos) {
        return Ci.datablock(pos);
    }
 
    @Override
    public SubMatrix Pi(int pos) {
        return Pi.subMatrix(pos);
    }

    @Override
    public void clear() {
        super.clear();
        Ci.clear();
        fi.clear();
        Pi.clear();
        enddiffuse = 0;
    }

    @Override
    public int getEndDiffusePosition() {
        return enddiffuse;
    }
}
