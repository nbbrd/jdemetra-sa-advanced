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
package ec.demetra.ssf.implementations;

import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 * Adds a noise to a given measurement. The noise is supposed time varying
 *
 * @author Jean Palate
 */
public class NoisyMeasurement implements ISsfMeasurement {

    public static interface INoise {

        double get(int pos);
    }

    public static ISsfMeasurement of(ISsfMeasurement m, INoise noise) {
        return new NoisyMeasurement(m, noise);
    }

    private final INoise noise;
    private final ISsfMeasurement measurement;

    private NoisyMeasurement(ISsfMeasurement m, INoise n) {
        this.measurement = m;
        this.noise = n;
    }

    @Override
    public void Z(int pos, DataBlock z) {
        measurement.Z(pos, z);
    }

    @Override
    public boolean hasErrors() {
        return true;
    }

    @Override
    public boolean hasError(int pos) {
        return measurement.hasError(pos) || noise.get(pos) > 0;
    }

    @Override
    public double errorVariance(int pos) {
        return noise.get(pos)+measurement.errorVariance(pos);
    }

    @Override
    public double ZX(int pos, DataBlock m) {
        return measurement.ZX(pos, m);
    }

    @Override
    public double ZVZ(int pos, SubMatrix V) {
        return measurement.ZVZ(pos, V);
    }

    @Override
    public void VpZdZ(int pos, SubMatrix V, double d) {
        measurement.VpZdZ(pos, V, d);
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        measurement.XpZd(pos, x, d);
    }

    @Override
    public int getStateDim() {
        return measurement.getStateDim();
    }

    @Override
    public boolean isTimeInvariant() {
        return false;
    }

    @Override
    public boolean isValid() {
        return measurement.isValid();
    }


}
