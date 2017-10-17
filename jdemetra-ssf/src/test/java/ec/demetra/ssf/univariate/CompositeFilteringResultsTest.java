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

import data.Data;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.demetra.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.implementations.arima.SsfArima;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class CompositeFilteringResultsTest {

    static final SarimaModel model;

    static {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline();
        spec.setP(3);
        spec.setBP(1);
        model = new SarimaModel(spec);
        model.setDefault(-.2, -.4);
    }

    public CompositeFilteringResultsTest() {
    }

    @Test
    public void test2results() {
        SsfArima create = SsfArima.create(model);
        SsfData data = new SsfData(Data.P);
        PredictionErrorDecomposition rslts0 = new PredictionErrorDecomposition(false);
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(null);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        DefaultFilteringResults rslts1 = DefaultFilteringResults.full();
        rslts1.prepare(create, 0, data.getLength());
        CompositeFilteringResults all = new CompositeFilteringResults(rslts0, rslts1);
        filter.process(create, data, all);
        assertEquals(rslts0.likelihood().getLogLikelihood(),DkToolkit.likelihoodComputer(false, false).compute(create, data).getLogLikelihood(), 1e-8);
    }

}
