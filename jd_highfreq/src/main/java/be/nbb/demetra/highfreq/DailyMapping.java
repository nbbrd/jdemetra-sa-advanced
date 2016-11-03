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
package be.nbb.demetra.highfreq;

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;

/**
 *
 * @author Jean Palate
 */
public class DailyMapping implements IParametricMapping<ArimaModel> {

    protected boolean adjust = true;

    static final double W0 = 5.75 / 7, W1 = 1 - W0;

    int freq1 = 7, freq2 = 365;

    @Override
    public ArimaModel map(IReadDataBlock p) {
        double th = p.get(0), bth1 = p.get(1), bth2 = p.get(2);
        double[] ma = new double[2];
        double[] sma1 = new double[freq1 + 1];
        double[] sma2 = new double[freq2 + (adjust ? 2 : 1)];
        double[] d = new double[2];
        double[] d1 = new double[freq1 + 1];
        double[] d2 = new double[freq2 + (adjust ? 2 : 1)];
        ma[0] = 1;
        ma[1] = -th;
        sma1[0] = 1;
        sma1[freq1] = -bth1;
        sma2[0] = 1;
        if (adjust) {
            sma2[freq2] = -bth2 * .75;
            sma2[freq2 + 1] = -bth2 * .25;
        } else {
            sma2[freq2] = -bth2;
        }

        d[0] = 1;
        d[1] = -1;
        d1[0] = 1;
        d1[freq1] = -1;
        d2[0] = 1;
        if (adjust) {
            d2[freq2] = -.75;
            d2[freq2 + 1] = -.25;
        } else {
            d2[freq2] = -1;
        }
        Polynomial.Division div = Polynomial.divide(Polynomial.of(d2), UnitRoots.D1);
        ArimaModel arima = new ArimaModel(
                new BackFilter(div.getQuotient()),
                BackFilter.D1.times(BackFilter.D1).times(BackFilter.of(d1)),
                BackFilter.of(ma).times(BackFilter.of(sma1)).times(BackFilter.of(sma2)),
                1);
        return arima;
    }

    @Override
    public IReadDataBlock map(ArimaModel t) {
        BackFilter ma = t.getMA();
        double[] p = new double[3];
        p[0] = -ma.get(1);
        p[1] = -ma.get(freq1);
        p[2] = adjust ? -ma.get(freq2) / .75 : -ma.get(freq2);
        return new DataBlock(p);
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        return inparams.check(x -> Math.abs(x) < .99);
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        return 1e-6;
    }

    @Override
    public int getDim() {
        return 3;
    }

    @Override
    public double lbound(int idx) {
        return -1;
    }

    @Override
    public double ubound(int idx) {
        return 1;
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
        boolean changed = false;
        double p = ioparams.get(0);
        if (Math.abs(p) >= .999) {
            ioparams.set(0, 1 / p);
            changed = true;
        }
        p = ioparams.get(1);
        if (Math.abs(p) >= .999) {
            ioparams.set(1, 1 / p);
            changed = true;
        }
        p = ioparams.get(2);
        if (Math.abs(p) >= .999) {
            ioparams.set(1, 1 / p);
            changed = true;
        }
        return changed ? ParamValidation.Changed : ParamValidation.Valid;
    }

    @Override
    public String getDescription(int idx) {
        return "p" + idx;
    }

    /**
     * @return the adjust
     */
    public boolean isAdjust() {
        return adjust;
    }

    /**
     * @param adjust the adjust to set
     */
    public void setAdjust(boolean adjust) {
        this.adjust = adjust;
    }
}
