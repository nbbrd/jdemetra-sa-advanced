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
package ec.demetra.ssf.implementations.var;

import data.Models;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.implementations.MultivariateTimeInvariantSsf;
import ec.demetra.ssf.implementations.TimeInvariantSsf;
import ec.demetra.ssf.multivariate.IMultivariateSsf;
import ec.demetra.ssf.univariate.ISsf;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class VarDynamicsTest {
    
    private static double EPS=1e-6;

    public VarDynamicsTest() {
    }

    @Test
    public void testDynamics() {
        int K = 10, L = 5;
        VarDescriptor desc = new VarDescriptor(K, L);
        Matrix a = Matrix.square(K);
        a.randomize();
        a.sub(.5);
        a.mul(.25);
        
        for (int i = 0; i < L; ++i) {
            desc.getA(i + 1).copy(a.all());
            a = a.times(a);
        }
        IMultivariateSsf ssf = Var.of(desc);
        IMultivariateSsf ref = MultivariateTimeInvariantSsf.of(ssf);
        int dim = ssf.getStateDim();
        Matrix M1 = new Matrix(dim, dim);
        M1.randomize();
        M1.sub(.5);
        M1 = SymmetricMatrix.XXt(M1);
        Matrix M2 = M1.clone();
        DataBlock x1 = new DataBlock(dim);
        x1.randomize();
        DataBlock x2 = x1.deepClone();
        ISsfDynamics dynref = ref.getDynamics();
        ISsfDynamics dyn = ssf.getDynamics();
        dynref.TX(0, x1);
        dyn.TX(dim, x2);
        assertTrue(x1.distance(x2) < EPS);
        dynref.XT(0, x1);
        dyn.XT(0, x2);
        assertTrue(x1.distance(x2) < EPS);
        dynref.TVT(0, M1.all());
        dyn.TVT(0, M2.all());
        assertTrue(M1.distance(M2) < EPS);
        dynref.addV(0, M1.all());
        dyn.addV(0, M2.all());
        assertTrue(M1.distance(M2) < EPS);
        dynref.TM(0, M1.all());
        dyn.TM(0, M2.all());
        assertTrue(M1.distance(M2) < EPS);
        dynref.MT(0, M1.all());
        dyn.MT(0, M2.all());
        assertTrue(M1.distance(M2) < EPS);
        Matrix Pi0 = Matrix.square(dim);
        Matrix B = new Matrix(dim, dyn.getNonStationaryDim());
        dyn.Pi0(Pi0.all());
        dyn.diffuseConstraints(B.all());
        assertTrue(Pi0.minus(SymmetricMatrix.XXt(B)).nrm2() < EPS);
    }

    @Test
    public void testDynamicsX() {
        int K = 10, L = 5;
        VarDescriptor desc = new VarDescriptor(K, L);
        Matrix a = Matrix.square(K);
        a.randomize();
        a.sub(.5);
        a.mul(.25);
        
        for (int i = 0; i < L; ++i) {
            desc.getA(i + 1).copy(a.all());
            a = a.times(a);
        }
        IMultivariateSsf ssf = Var.of(desc, L+3);
        IMultivariateSsf ref = MultivariateTimeInvariantSsf.of(ssf);
        int dim = ssf.getStateDim();
        Matrix M1 = new Matrix(dim, dim);
        M1.randomize();
        M1.sub(.5);
        M1 = SymmetricMatrix.XXt(M1);
        Matrix M2 = M1.clone();
        DataBlock x1 = new DataBlock(dim);
        x1.randomize();
        DataBlock x2 = x1.deepClone();
        ISsfDynamics dynref = ref.getDynamics();
        ISsfDynamics dyn = ssf.getDynamics();
        dynref.TX(0, x1);
        dyn.TX(dim, x2);
        assertTrue(x1.distance(x2) < EPS);
        dynref.XT(0, x1);
        dyn.XT(0, x2);
        assertTrue(x1.distance(x2) < EPS);
        dynref.TVT(0, M1.all());
        dyn.TVT(0, M2.all());
        assertTrue(M1.distance(M2) < EPS);
        dynref.addV(0, M1.all());
        dyn.addV(0, M2.all());
        assertTrue(M1.distance(M2) < EPS);
        dynref.TM(0, M1.all());
        dyn.TM(0, M2.all());
        assertTrue(M1.distance(M2) < EPS);
        dynref.MT(0, M1.all());
        dyn.MT(0, M2.all());
        assertTrue(M1.distance(M2) < EPS);
        Matrix Pi0 = Matrix.square(dim);
        Matrix B = new Matrix(dim, dyn.getNonStationaryDim());
        dyn.Pi0(Pi0.all());
        dyn.diffuseConstraints(B.all());
        assertTrue(Pi0.minus(SymmetricMatrix.XXt(B)).nrm2() < EPS);
    }

    @Test
    @Disabled
    public void stressTestTX() {
        int K = 10, L = 20;
        VarDescriptor desc = new VarDescriptor(K, L);
        Matrix a = Matrix.square(K);
        a.randomize();
        a.mul(.5);
        for (int i = 0; i < L; ++i) {
            desc.getA(i + 1).copy(a.all());
            a = a.times(a);
        }
        ISsfDynamics dynamics = VarDynamics.of(desc, Matrix.square(desc.getVariablesCount() * desc.getLagsCount()));
        System.out.println(dynamics.getStateDim());
        Matrix M = new Matrix(dynamics.getStateDim(), 1000);
        M.randomize();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            dynamics.TM(i, M.all());
        }
        long t1 = System.currentTimeMillis();
        System.out.println("functions");
        System.out.println(t1 - t0);
        M.randomize();
        t0 = System.currentTimeMillis();
        Matrix T = Matrix.square(dynamics.getStateDim());
        dynamics.T(0, T.all());
        for (int i = 0; i < 1000; ++i) {
            M = T.times(M);
        }
        t1 = System.currentTimeMillis();
        System.out.println("matrix");
        System.out.println(t1 - t0);
    }

}
