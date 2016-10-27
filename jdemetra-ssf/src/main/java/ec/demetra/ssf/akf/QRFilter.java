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
package ec.demetra.ssf.akf;

import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.ResultsRange;
import ec.demetra.ssf.univariate.DefaultFilteringResults;
import ec.demetra.ssf.univariate.FastFilter;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.OrdinaryFilter;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.eco.Determinant;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.matrices.UpperTriangularMatrix;

/**
 * QR variant of the augmented Kalman filter. See for instance Gomez-Maravall.
 * This implementation doesn't use collapsing
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class QRFilter {

    private ProfileLikelihood pll;
    private MarginalLikelihood mll;
    private DiffuseLikelihood dll;
    private Matrix R, X, Xl;
    private DataBlock yl, b;
    private double ldet, ssq, dcorr, pcorr, mcorr;

    /**
     *
     */
    public QRFilter() {
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsf ssf, final ISsfData data) {
        clear();
        OrdinaryFilter filter = new OrdinaryFilter();
        DefaultFilteringResults fr = DefaultFilteringResults.light();
        fr.prepare(ssf, 0, data.getLength());
        if (!filter.process(ssf, data, fr)) {
            return false;
        }
        Determinant det = new Determinant();
        IReadDataBlock vars = fr.errorVariances();
        for (int i = 0; i < vars.getLength(); ++i) {
            double v = vars.get(i);
            if (v != 0) {
                det.add(v);
            }
        }
        ldet = det.getLogDeterminant();

        // apply the filter on the diffuse effects
        ISsfDynamics dynamics = ssf.getDynamics();
        X = new Matrix(data.getLength(), dynamics.getNonStationaryDim());
        ssf.diffuseEffects(X.all());
        yl = new DataBlock(fr.errors(true, true));
        FastFilter ffilter = new FastFilter(ssf, fr, new ResultsRange(0, data.getLength()));
        int n = ffilter.getOutputLength(X.getRowsCount());
        Xl = new Matrix(n, X.getColumnsCount());
        for (int i = 0; i < X.getColumnsCount(); ++i) {
            ffilter.transform(X.column(i), Xl.column(i));
        }
        return true;
    }

    private void calcMLL() {
        if (dll == null) {
            calcDLL();
        }

        Householder housx = new Householder(false);
        housx.setEpsilon(1e-13);
        housx.decompose(X);
        mcorr = 2 * housx.getRDiagonal().sumLog().value;
        int nd = housx.getRank(), n = Xl.getRowsCount();

        mll = new MarginalLikelihood();
        mll.set(ssq, ldet, dcorr, mcorr, n, nd);
    }

    private void calcDLL() {
        Householder hous = new Householder(false);
        hous.setEpsilon(1e-13);
        hous.decompose(Xl);
        b = new DataBlock(hous.getRank());
        int nd = b.getLength(), n = Xl.getRowsCount();
        DataBlock e = new DataBlock(n - nd);
        hous.leastSquares(yl, b, e);
        ssq = e.ssq();
        dcorr = 2 * hous.getRDiagonal().sumLog().value;
        R = hous.getR();
        dll = new DiffuseLikelihood();
        dll.set(ssq, ldet, dcorr, n, nd);
    }

    private void calcPLL() {
        if (dll == null) {
            calcDLL();
        }

        int n = Xl.getRowsCount();
        Matrix bvar = SymmetricMatrix.XXt(UpperTriangularMatrix
                .inverse(R));
        bvar.mul(ssq / n);
        pll = new ProfileLikelihood();
        pll.set(ssq, ldet, b, bvar, n);

    }

    private void clear() {
        ssq = 0;
        ldet = 0;
        dcorr = 0;
        pcorr = 0;
        mcorr = 0;
        mll = null;
        pll = null;
        X = null;
        Xl = null;
        yl = null;
        R = null;
    }

    public ProfileLikelihood getProfileLikelihood() {
        if (pll == null) {
            calcPLL();
        }
        return pll;
    }

    public MarginalLikelihood getMarginalLikelihood() {
        if (mll == null) {
            calcMLL();
        }
        return mll;
    }

    public DiffuseLikelihood getDiffuseLikelihood() {
        if (dll == null) {
            calcDLL();
        }
        return dll;
    }
}
