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
package ec.demetra.ssf.dk;

import data.Data;
import data.Models;
import ec.demetra.ssf.implementations.arima.SsfUcarima;
import ec.demetra.ssf.univariate.IConcentratedLikelihoodComputer;
import ec.demetra.ssf.univariate.SsfData;
import ec.demetra.ssf.univariate.SsfRegressionModel;
import ec.tstoolkit.maths.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DkToolkitTest {

    public DkToolkitTest() {
    }

    @Test
    public void testArima() {
        SsfUcarima ssf = SsfUcarima.create(Models.ucmAirline(-.6, -.4));
        SsfData y = new SsfData(Data.P);
        IConcentratedLikelihoodComputer<DkConcentratedLikelihood> llc = DkToolkit.concentratedLikelihoodComputer(true, false);
        Matrix X = new Matrix(y.getLength(), 2);
        X.set(5, 0, 1);
        X.set(50, 1, 1);
        SsfRegressionModel model=new SsfRegressionModel(ssf, y, X.all(), null);
        DkConcentratedLikelihood ll = llc.compute(model);
        System.out.println(ll.getLogLikelihood());
    }

    @Test
    public void testFastArima() {
        SsfUcarima ssf = SsfUcarima.create(Models.ucmAirline(-.6, -.4));
        SsfData y = new SsfData(Data.P);
        IConcentratedLikelihoodComputer<DkConcentratedLikelihood> llc = DkToolkit.concentratedLikelihoodComputer(true, true);
        Matrix X = new Matrix(y.getLength(), 2);
        X.set(5, 0, 1);
        X.set(50, 1, 1);
        SsfRegressionModel model=new SsfRegressionModel(ssf, y, X.all(), null);
        DkConcentratedLikelihood ll = llc.compute(model);
        System.out.println(ll.getLogLikelihood());
    }
}
