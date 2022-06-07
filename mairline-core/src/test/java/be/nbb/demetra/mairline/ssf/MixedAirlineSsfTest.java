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
package be.nbb.demetra.mairline.ssf;

import be.nbb.demetra.mairline.MixedAirlineModel;
import data.Data;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.SsfData;
import ec.demetra.eco.ILikelihood;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class MixedAirlineSsfTest {

    public MixedAirlineSsfTest() {
    }

    @Test
    public void testSsf() {
        MixedAirlineModel model = new MixedAirlineModel();
        model.setFrequency(12);
        model.setNoisyPeriods(new int[]{0, 1, 11});
        model.setNoisyPeriodsVariance(1);

        ISsf ssf = MixedAirlineSsf.of(model);
        ILikelihood ll = DkToolkit.likelihoodComputer().compute(ssf, new SsfData(Data.P));
//        System.out.println(ll.getSsqErr());
    }

    @Test
    public void testSsf2() {
        MixedAirlineModel model = new MixedAirlineModel();
        model.setFrequency(12);
        model.setNoisyPeriods(new int[]{0, 1, 11});
        model.setNoisyPeriodsVariance(1);

        ISsf ssf = MixedAirlineSsf.of2(model);
        ILikelihood ll = DkToolkit.likelihoodComputer().compute(ssf, new SsfData(Data.P));
//        System.out.println(ll.getSsqErr());
    }

    @Test
    @Disabled
    public void testEstimate() {
        long t0 = System.currentTimeMillis();
        MixedAirlineModel rslt = null;
        for (int i = 0; i < 100; ++i) {
            MixedAirlineModel model = new MixedAirlineModel();
            model.setFrequency(12);
            model.setNoisyPeriods(new int[]{6});
            model.setNoisyPeriodsVariance(1);
            MixedAirlineEstimation estimation = new MixedAirlineEstimation();
            rslt = estimation.compute(model.getAirline(), model.getNoisyPeriods(), Data.P);
        }
        long t1 = System.currentTimeMillis();
//        System.out.println("estimate 1 " + (t1 - t0));
        ISsf ssf = MixedAirlineSsf.of(rslt);
        ILikelihood ll = DkToolkit.likelihoodComputer(true, true).compute(ssf, new SsfData(Data.P));
//        System.out.println(ll.getLogLikelihood());
//        System.out.println(rslt.getAirline());
//        System.out.println(rslt.getNoisyPeriodsVariance());
    }

    @Test
    @Disabled
    public void testEstimate2() {
        long t0 = System.currentTimeMillis();
        MixedAirlineModel rslt = null;
        for (int i = 0; i < 100; ++i) {
            MixedAirlineModel model = new MixedAirlineModel();
            model.setFrequency(12);
            model.setNoisyPeriods(new int[]{6});
            model.setNoisyPeriodsVariance(1);
            MixedAirlineEstimation estimation = new MixedAirlineEstimation();
            rslt = estimation.compute2(model.getAirline(), model.getNoisyPeriods(), Data.P);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("estimate 2 " + (t1 - t0));
        ISsf ssf = MixedAirlineSsf.of(rslt);
        ILikelihood ll = DkToolkit.likelihoodComputer().compute(ssf, new SsfData(Data.P));
//        System.out.println(ll.getLogLikelihood());
//        System.out.println(rslt.getAirline());
//        System.out.println(rslt.getNoisyPeriodsVariance());
    }

    @Test
    @Disabled
    public void stressTest() {
        MixedAirlineModel model = new MixedAirlineModel();
        model.setFrequency(12);
        model.setNoisyPeriods(new int[]{0, 1, 11});
        model.setNoisyPeriodsVariance(1);

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            ISsf ssf = MixedAirlineSsf.of(model);
            ILikelihood ll = DkToolkit.likelihoodComputer().compute(ssf, new SsfData(Data.P));
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            ISsf ssf = MixedAirlineSsf.of2(model);
            ILikelihood ll = DkToolkit.likelihoodComputer().compute(ssf, new SsfData(Data.P));
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
