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
package ec.demetra.ssf.array;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.ElementaryTransformations;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.State;
import ec.demetra.ssf.univariate.IFilteringResults;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.demetra.ssf.univariate.UpdateInformation;

/**
 * Array form of the Kalman filter
 *
 * @author Jean Palate
 */
public class ArrayFilter {

    private LState state_;
    private UpdateInformation pe_;
    private ISsfMeasurement m_;
    private ISsfDynamics dyn_;
    private ISsfData data_;
    private int pos_, end_, dim_, nres_;
    private Matrix A;

    /**
     *
     */
    public ArrayFilter() {
    }

    /**
     */
    protected void error() {

        double y = data_.get(pos_);
        pe_.set(y - m_.ZX(pos_, state_.a));
    }

    private boolean initFilter() {
        pos_ = 0;
        end_ = data_.getLength();
        nres_ = dyn_.getInnovationsDim();
        dim_ = dyn_.getStateDim();
        A = new Matrix(dim_ + 1, dim_ + 1 + nres_);
        return true;
    }

    private int initState() {
        state_ = new LState(L());
        pe_ = new UpdateInformation(dim_);
        if (!dyn_.a0(state_.a))
            return -1;
        Matrix P0 = new Matrix(dim_, dim_);
        if (! dyn_.Pf0(P0.all()))
            return -1;
        SymmetricMatrix.lcholesky(P0, State.ZERO);
        state_.L.copy(P0.all());

        return 0;
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsf ssf, final ISsfData data, final IFilteringResults rslts) {
        m_ = ssf.getMeasurement();
        dyn_ = ssf.getDynamics();
        data_ = data;
        if (!initFilter()) {
            return false;
        }
        pos_ = initState();
        if (pos_ < 0) {
            return false;
        }
        do {
            preArray();
            ElementaryTransformations.fastGivensTriangularize(A.all());
            postArray();
            error();
            rslts.save(pos_, pe_);
            nextState();
        } while (++pos_ < end_);
        return true;
    }

    private void preArray() {
        m_.ZM(pos_, L(), ZL());
        dyn_.TM(pos_, L());
        U().set(0);
        dyn_.S(pos_, U());
        K().set(0);
        if (m_.hasError(pos_))
            A.set(0,0, Math.sqrt(m_.errorVariance(pos_)));
        else
            A.set(0,0,0);
    }

    private void postArray() {
        double e=A.get(0,0);
        pe_.setStandardDeviation(e);
        pe_.M().copy(K());
        pe_.M().mul(e);
    }

    private void nextState() {
        dyn_.TX(pos_, state_.a);
        state_.a.addAY(pe_.get() / pe_.getVariance(), pe_.M());
     }

    private DataBlock K() {
        return A.column(0).drop(1, 0);
    }

    private DataBlock ZL() {
        return A.row(0).range(1, 1 + dim_);
    }

    private SubMatrix L() {
        return A.subMatrix(1, 1 + dim_, 1, 1 + dim_);
    }

    private SubMatrix U() {
        return A.subMatrix(1, 1 + dim_, 1 + dim_, -1);
    }
}
