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
import ec.demetra.ssf.implementations.CompositeSsf;
import ec.demetra.ssf.implementations.Measurement;
import ec.demetra.ssf.implementations.NoisyMeasurement;
import ec.demetra.ssf.implementations.WeightedCompositeMeasurement;
import ec.demetra.ssf.implementations.arima.SsfArima;
import ec.demetra.ssf.implementations.structural.Noise;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.demetra.ssf.univariate.Ssf;

/**
 *
 * @author Jean Palate
 */
public class MixedAirlineSsf {

    public static ISsf of(final MixedAirlineModel model) {
        return of(model, 0);
    }

    public static ISsf of(final MixedAirlineModel model, final int pstart) {
        SsfArima.SsfArimaDynamics dynamics = new SsfArima.SsfArimaDynamics(model.getAirline());
        ISsfMeasurement m = Measurement.create(dynamics.getStateDim(), 0);
        int[] np = model.getNoisyPeriods();
        final boolean[] noisy = new boolean[model.getFrequency()];
        final double nvar = model.getNoisyPeriodsVariance();
        for (int i = 0; i < np.length; ++i) {
            noisy[np[i]] = true;
        }
        NoisyMeasurement.INoise noise = (int pos) -> {
            return noisy[(pstart + pos) % noisy.length] ? nvar : 0;
        };
        return new Ssf(dynamics, NoisyMeasurement.of(m, noise));
    }

    public static ISsf of2(final MixedAirlineModel model) {
        return of2(model, 0);
    }

    public static ISsf of2(final MixedAirlineModel model, final int pstart) {
        SsfArima arima = SsfArima.create(model.getAirline());
        int[] np = model.getNoisyPeriods();
        final boolean[] noisy = new boolean[model.getFrequency()];
        final double nvar = model.getNoisyPeriodsVariance();
        for (int i = 0; i < np.length; ++i) {
            noisy[np[i]] = true;
        }
        WeightedCompositeMeasurement.IWeights w = new WeightedCompositeMeasurement.IWeights() {

            @Override
            public double get(int cmp, int pos) {
                if (cmp == 0) {
                    return 1;
                } else {
                    return noisy[(pstart + pos) % noisy.length] ? 1 : 0;
                }
            }

            @Override
            public boolean isTimeInvariant() {
                return false;
            }
        };

        Noise noise = new Noise(nvar);
        return CompositeSsf.of(w, arima, noise);

    }

}
