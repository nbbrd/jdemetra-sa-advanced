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
package be.nbb.demetra.sssts.ssf;

import be.nbb.demetra.sssts.SSSTSModel;
import be.nbb.demetra.sssts.ssf.SsfofSSSTS.SSLLTDyn;
import data.Data;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.dk.DkLikelihood;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.implementations.TimeInvariantDynamics;
import ec.demetra.ssf.implementations.structural.Component;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.SsfData;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class SsfofSSSTSTest {

    SSSTSModel model = new SSSTSModel();

    public SsfofSSSTSTest() {
        model.setLvar(1);
        model.setSvar(1);
        model.setNvar(1);
        model.setNoisyPeriodsVariance(1);
        model.setFrequency(12);
        model.setNoisyPeriods(new int[]{6, 7, 11});
    }

    @Test
    @Ignore
    public void testSSLLTDyn() {

        SSLLTDyn dyn = new SSLLTDyn(model);
        TimeInvariantDynamics dynref = TimeInvariantDynamics.of(dyn);
        int dim = dyn.getStateDim();
        Matrix M1 = new Matrix(dim, dim);
        M1.randomize();
        M1 = SymmetricMatrix.XXt(M1);
        Matrix M2 = M1.clone();
        DataBlock x1 = new DataBlock(dim);
        x1.randomize();
        DataBlock x2 = x1.deepClone();
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
    public void testNoise() {
        model.setNoisyComponent(Component.Noise);
        ISsf ssf = SsfofSSSTS.of(model, 0);
        DkLikelihood ll = DkToolkit.likelihoodComputer().compute(ssf, new SsfData(Data.P));
//        System.out.println(ll.getLogLikelihood());
        model.setSvar(10);
        ssf = SsfofSSSTS.of(model, 0);
        ll = DkToolkit.likelihoodComputer().compute(ssf, new SsfData(Data.P));
//        System.out.println(ll.getLogLikelihood());
    }

}
