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
import ec.tstoolkit.maths.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class VarDynamicsTest {

    public VarDynamicsTest() {
    }

    @Test
    @Ignore
    public void stressTestTX() {
        int K = 10, L = 20;
        VarDescriptor desc = new VarDescriptor(K, L);
        Matrix a = Matrix.square(K);
        a.randomize();
        a.mul(.5);
        for (int i = 0; i < L; ++i) {
            desc.getA(i+1).copy(a.all());
            a = a.times(a);
        }
        ISsfDynamics dynamics = VarDynamics.of(desc, Matrix.square(2*desc.getVariablesCount()*desc.getLagsCount()));
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
