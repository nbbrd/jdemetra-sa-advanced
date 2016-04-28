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
package ec.demetra.ssf.akf;

import data.Models;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.demetra.ssf.implementations.arima.SsfArima;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class QRFilterTest {

    public QRFilterTest() {
    }

    @Test
    public void testArima() {

        for (int i = 1; i <= 100; ++i) {
            SarimaModel arima = new SarimaModelBuilder().createAirlineModel(12, -.6, -1 + .01 * i) ;

            SsfArima ssf = SsfArima.create(arima);

            QRFilter qrf = new QRFilter();

            qrf.process(ssf, Models.ssfX);

//            System.out.print(qrf.getDiffuseLikelihood().getLogLikelihood());
//            System.out.print('\t');
//            System.out.print(qrf.getMarginalLikelihood().getLogLikelihood());
//            System.out.print('\t');
//            System.out.println(qrf.getProfileLikelihood().getLogLikelihood());
        }

    }
}
