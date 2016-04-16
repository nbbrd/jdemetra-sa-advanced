/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.ssf.univariate;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.ResultsRange;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.linearfilters.ILinearProcess;

/**
 *
 * @author Jean Palate
 */
public class FastFilter implements ILinearProcess {

    private final IFilteringResults frslts;
    private final ISsfMeasurement measurement;
    private final ISsfDynamics dynamics;
    private final int start, end;
    private SubMatrix states;
    // temporaries
    private DataBlock tmp, scol;
    private DataBlockIterator scols;

    public FastFilter(ISsf ssf, IFilteringResults frslts, ResultsRange range) {
        this.frslts = frslts;
        measurement = ssf.getMeasurement();
        dynamics = ssf.getDynamics();
        start = range.getStart();
        end = range.getEnd();
    }

    public boolean filter(SubMatrix x) {
        if (end - start < x.getRowsCount()) {
            return false;
        }
        int dim = dynamics.getStateDim();
        states = new Matrix(dim, x.getColumnsCount()).all();
        prepareTmp();
        DataBlockIterator rows = x.rows();
        DataBlock row = rows.getData();
        int pos = start;
        do {
            iterate(pos, row);
        } while (++pos < end && rows.next());
        return true;
    }

    private void prepareTmp() {
        int nvars = states.getColumnsCount();
        tmp = new DataBlock(nvars);
        scols = states.columns();
        scol = scols.getData();
    }

    private void iterate(int i, DataBlock row) {
        boolean missing = !Double.isFinite(frslts.error(i));
        if (!missing) {
            double f = frslts.errorVariance(i);
            if (f > 0) {
                measurement.ZM(i, states, tmp);
                row.sub(tmp);
                // update the states
                DataBlock C = frslts.M(i);
                // process by column
                scols.begin();
                int j = 0;
                do {
                    scol.addAY(row.get(j++) / f, C);
                } while (scols.next());
                row.mul(1 / Math.sqrt(f));
            } else {
                row.set(Double.NaN);
            }
        }
        dynamics.TM(i, states);
        //  
    }

    @Override
    public boolean transform(IReadDataBlock in, IDataBlock out) {
        if (in.getLength() > end - start) {
            return false;
        }
        int dim = dynamics.getStateDim(), n = in.getLength();
        DataBlock state = new DataBlock(dim);
        int pos = start, ipos = 0, opos = 0;
        do {
            boolean missing = !Double.isFinite(frslts.error(pos));
            if (!missing) {
                double f = frslts.errorVariance(pos);
                double e = in.get(ipos) - measurement.ZX(pos, state);
                if (f != 0) {
                    out.set(opos++, e / Math.sqrt(f));
                    // update the state
                    DataBlock C = frslts.M(pos);
                    // process by column
                    state.addAY(e / f, C);
                }
            }
            dynamics.TX(pos++, state);
        } while (++ipos < n);
        return true;
    }

    @Override
    public int getOutputLength(int inputLength) {
        int n = 0;
        int imax = start + inputLength;
        if (imax > end) {
            return -1;
        }
        for (int i = start; i < imax; ++i) {
            double e = frslts.error(i), v = frslts.errorVariance(i);
            if (Double.isFinite(e) && v != 0) {
                ++n;
            }
        }
        return n;
    }
}
