/*
 * Copyright 2016-2017 National Bank of Belgium
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

import data.Models;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.ResultsRange;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.dk.DkFilter;
import ec.demetra.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import ec.demetra.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import ec.demetra.ssf.univariate.DefaultSmoothingResults;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.OrdinaryFilter;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.HouseholderR;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.RegressionUtilities;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class RegSsfTest {

    int M = 10000;

    final RegSsf ssf;
    final ISsf arima;
    final Matrix matrix;
    final ISsfData p;

    public RegSsfTest() {
        arima = Models.ssfArima;
        p = Models.ssfProd;
        GregorianCalendarVariables vars = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        Matrix m = RegressionUtilities.matrix(vars, new TsDomain(TsFrequency.Monthly, 1967, 0, p.getLength()));
        matrix = new Matrix(m.getRowsCount(), 2 * m.getColumnsCount());
        matrix.subMatrix(0, -1, 0, m.getColumnsCount()).copy(m.all());
        matrix.subMatrix(0, -1, m.getColumnsCount(), -1).copy(m.all());
        ssf = RegSsf.create(arima, matrix.all());
    }

    @Test
    public void testDynamics() {
        ISsf ref = TimeInvariantSsf.of(ssf);
        int dim = ssf.getStateDim();
        Matrix M1 = new Matrix(dim, dim);
        M1.randomize();
        M1 = SymmetricMatrix.XXt(M1);
        Matrix M2 = M1.clone();
        DataBlock x1 = new DataBlock(dim);
        x1.randomize();
        DataBlock x2 = x1.deepClone();
        ISsfDynamics dynref = ref.getDynamics();
        ISsfDynamics dyn = ssf.getDynamics();
        dynref.TX(0, x1);
        dyn.TX(dim, x2);
        assertTrue(x1.distance(x2) < 1e-6);
        dynref.XT(0, x1);
        dyn.XT(0, x2);
        assertTrue(x1.distance(x2) < 1e-9);
        dynref.TVT(0, M1.all());
        dyn.TVT(0, M2.all());
        assertTrue(M1.distance(M2) < 1e-9);
        dynref.TM(0, M1.all());
        dyn.TM(0, M2.all());
        assertTrue(M1.distance(M2) < 1e-9);
        dynref.MT(0, M1.all());
        dyn.MT(0, M2.all());
        assertTrue(M1.distance(M2) < 1e-9);
        Matrix Pi0 = Matrix.square(dim);
        Matrix B = new Matrix(dim, dyn.getNonStationaryDim());
        dyn.Pi0(Pi0.all());
        dyn.diffuseConstraints(B.all());
        assertTrue(Pi0.minus(SymmetricMatrix.XXt(B)).nrm2() < 1e-9);
    }

    @Test
    public void testSmoothing() {
        DefaultSmoothingResults sm1 = DkToolkit.sqrtSmooth(ssf, p, true);
        DefaultSmoothingResults sm2 = DkToolkit.smooth(ssf, p, true);
        assertTrue(sm1.getComponent(0).distance(sm2.getComponent(0)) < 1e-6);
    }

    @Test
    public void testReg() {
        DefaultDiffuseSquareRootFilteringResults fr = DkToolkit.sqrtFilter(arima, p, false);
        IReadDataBlock yl = fr.errors(true, true);
        DkFilter filter = new DkFilter(arima, fr, new ResultsRange(0, p.getLength()));
        int n = filter.getOutputLength(matrix.getRowsCount());
        Matrix ml = new Matrix(n, matrix.getColumnsCount());
        for (int i = 0; i < matrix.getColumnsCount(); ++i) {
            filter.transform(matrix.column(i), ml.column(i));
        }
        Householder hous = new Householder(true);
        hous.setEpsilon(1e-13);
        hous.decompose(ml);
        DataBlock b = new DataBlock(hous.getRank());
        DataBlock e = new DataBlock(n - b.getLength());
        hous.leastSquares(yl, b, e);
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(null);
        OrdinaryFilter ofilter = new OrdinaryFilter(initializer);
        ofilter.process(ssf, p, null);
    }

    @Test
    @Disabled
    public void stressTestReg() {
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < M; ++k) {
            DefaultDiffuseSquareRootFilteringResults fr = DkToolkit.sqrtFilter(arima, p, false);
            IReadDataBlock yl = fr.errors(true, true);
            DkFilter filter = new DkFilter(arima, fr, new ResultsRange(0, p.getLength()));
            int n = filter.getOutputLength(matrix.getRowsCount());
            Matrix ml = new Matrix(n, matrix.getColumnsCount());
            for (int i = 0; i < matrix.getColumnsCount(); ++i) {
                filter.transform(matrix.column(i), ml.column(i));
            }
            HouseholderR hous = new HouseholderR(false);
            hous.setEpsilon(1e-12);
            hous.decompose(ml);
            DataBlock b = new DataBlock(ml.getColumnsCount());
            DataBlock e = new DataBlock(n - b.getLength());
            hous.leastSquares(yl, b, e);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("QR");
        System.out.println(t1 - t0);

        t0 = System.currentTimeMillis();
        for (int k = 0; k < M; ++k) {
            DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(null);
            OrdinaryFilter ofilter = new OrdinaryFilter(initializer);
            ofilter.process(ssf, p, null);
        }
        t1 = System.currentTimeMillis();
        System.out.println("XReg");
        System.out.println(t1 - t0);
    }
}
