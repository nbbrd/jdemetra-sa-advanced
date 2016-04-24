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
package be.nbb.demetra.sssts.ssf;

import be.nbb.demetra.sssts.SSHSModel;
import be.nbb.demetra.sts.BasicStructuralModel;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.implementations.CompositeDynamics;
import ec.demetra.ssf.implementations.CompositeMeasurement;
import ec.demetra.ssf.implementations.Measurement;
import ec.demetra.ssf.implementations.NoisyMeasurement;
import ec.demetra.ssf.implementations.arima.SsfRandomWalk;
import ec.demetra.ssf.implementations.structural.Component;
import ec.demetra.ssf.implementations.structural.CyclicalComponent;
import ec.demetra.ssf.implementations.structural.LocalLinearTrend;
import ec.demetra.ssf.implementations.structural.SeasonalComponent;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.demetra.ssf.univariate.Ssf;
import ec.tstoolkit.utilities.IntList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class SsfofSSHS {

    public static ISsf ofNoise(final SSHSModel model) {
        return SsfofSSHS.ofNoise(model, 0);
    }

    public static ISsf ofNoise(final SSHSModel model, final int pstart) {
        BasicStructuralModel bsm = model.getBasicStructuralModel();
        List<ISsfDynamics> dyn = new ArrayList<>();
        IntList mpos = new IntList();
        // cycle ?
        int curpos = 0;
        double cvar = bsm.getVariance(Component.Cycle);
        if (cvar >= 0) {
            mpos.add(curpos);
            CyclicalComponent.Dynamics cdyn = new CyclicalComponent.Dynamics(bsm.getCyclicalDumpingFactor(), bsm.getCyclicalPeriod(), cvar);
            curpos += cdyn.getStateDim();
            dyn.add(cdyn);
        }
        double lvar = bsm.getVariance(Component.Level), svar = bsm.getVariance(Component.Slope);
        if (lvar >= 0 && svar >= 0) {
            mpos.add(curpos);
            LocalLinearTrend.Dynamics lltdyn = new LocalLinearTrend.Dynamics(lvar, svar);
            curpos += lltdyn.getStateDim();
            dyn.add(lltdyn);
        } else if (lvar >= 0) {
            mpos.add(curpos);
            ISsfDynamics lldyn = new SsfRandomWalk.Dynamics(lvar);
            curpos += lldyn.getStateDim();
            dyn.add(lldyn);
        }
        double seasvar = bsm.getVariance(Component.Seasonal);
        if (seasvar >= 0) {
            mpos.add(curpos);
            SeasonalComponent.Dynamics sdyn = new SeasonalComponent.Dynamics(bsm.getSpecification().getSeasonalModel(), seasvar, bsm.getFrequency());
            dyn.add(sdyn);
        }
        CompositeDynamics cdyn = new CompositeDynamics(dyn.toArray(new ISsfDynamics[dyn.size()]));
        ISsfMeasurement m = Measurement.create(cdyn.getStateDim(), mpos.toArray());
        int[] np = model.getNoisyPeriods();
        final boolean[] noisy = new boolean[bsm.getFrequency()];
        final double nvar = model.getNoisyPeriodsVariance();
        final double evar = bsm.getVariance(Component.Noise);
        for (int i = 0; i < np.length; ++i) {
            noisy[np[i]] = true;
        }
        NoisyMeasurement.INoise noise = (int pos) -> {
            return noisy[(pstart + pos) % noisy.length] ? evar + nvar : evar;
        };
        return new Ssf(cdyn, NoisyMeasurement.of(m, noise));
    }

   public static ISsf ofNoise2(final SSHSModel model, final int pstart) {
        BasicStructuralModel bsm = model.getBasicStructuralModel();
        List<ISsfDynamics> dyn = new ArrayList<>();
         List<ISsfMeasurement> m = new ArrayList<>();
        // cycle ?
        double cvar = bsm.getVariance(Component.Cycle);
        if (cvar >= 0) {
            CyclicalComponent.Dynamics cdyn = new CyclicalComponent.Dynamics(bsm.getCyclicalDumpingFactor(), bsm.getCyclicalPeriod(), cvar);
            dyn.add(cdyn);
            m.add(Measurement.create(cdyn.getStateDim(), 0));
        }
        double lvar = bsm.getVariance(Component.Level), svar = bsm.getVariance(Component.Slope);
        if (lvar >= 0 && svar >= 0) {
            LocalLinearTrend.Dynamics lltdyn = new LocalLinearTrend.Dynamics(lvar, svar);
            dyn.add(lltdyn);
            m.add(Measurement.create(lltdyn.getStateDim(), 0));
        } else if (lvar >= 0) {
            ISsfDynamics lldyn = new SsfRandomWalk.Dynamics(lvar);
            dyn.add(lldyn);
            m.add(Measurement.create(lldyn.getStateDim(), 0));
        }
        double seasvar = bsm.getVariance(Component.Seasonal);
        if (seasvar >= 0) {
            SeasonalComponent.Dynamics sdyn = new SeasonalComponent.Dynamics(bsm.getSpecification().getSeasonalModel(), seasvar, bsm.getFrequency());
            dyn.add(sdyn);
              m.add(Measurement.create(sdyn.getStateDim(), 0));
      }
        CompositeDynamics cdyn = new CompositeDynamics(dyn.toArray(new ISsfDynamics[dyn.size()]));
        CompositeMeasurement cm = new CompositeMeasurement(m.toArray(new ISsfMeasurement[m.size()]), 0);
        int[] np = model.getNoisyPeriods();
        final boolean[] noisy = new boolean[bsm.getFrequency()];
        final double nvar = model.getNoisyPeriodsVariance();
        final double evar = bsm.getVariance(Component.Noise);
        for (int i = 0; i < np.length; ++i) {
            noisy[np[i]] = true;
        }
        NoisyMeasurement.INoise noise = (int pos) -> {
            return noisy[(pstart + pos) % noisy.length] ? evar + nvar : evar;
        };
        return new Ssf(cdyn, NoisyMeasurement.of(cm, noise));
    }

   public static ISsf ofSeasonal(final SSHSModel model) {
        return SsfofSSHS.ofSeasonal(model, 0);
    }

    public static ISsf ofSeasonal(final SSHSModel model, final int pstart) {
        BasicStructuralModel bsm = model.getBasicStructuralModel();
        List<ISsfDynamics> dyn = new ArrayList<>();
        List<ISsfMeasurement> m = new ArrayList<>();
        // cycle ?
        double cvar = bsm.getVariance(Component.Cycle);
        if (cvar >= 0) {
            CyclicalComponent.Dynamics cdyn = new CyclicalComponent.Dynamics(bsm.getCyclicalDumpingFactor(), bsm.getCyclicalPeriod(), cvar);
            dyn.add(cdyn);
            m.add(Measurement.create(cdyn.getStateDim(), 0));
        }
        double lvar = bsm.getVariance(Component.Level), svar = bsm.getVariance(Component.Slope);
        if (lvar >= 0 && svar >= 0) {
            LocalLinearTrend.Dynamics lltdyn = new LocalLinearTrend.Dynamics(lvar, svar);
            dyn.add(lltdyn);
            m.add(Measurement.create(lltdyn.getStateDim(), 0));
        } else if (lvar >= 0) {
            ISsfDynamics lldyn = new SsfRandomWalk.Dynamics(lvar);
            dyn.add(lldyn);
            m.add(Measurement.create(lldyn.getStateDim(), 0));
        }
        double seasvar = bsm.getVariance(Component.Seasonal);
        double nvar = model.getNoisyPeriodsVariance();
        if (seasvar >= 0) {
            int freq = bsm.getFrequency();
            double[] var = new double[freq];
            for (int i = 0; i < freq; ++i) {
                var[i] = seasvar;
            }
            int[] np = model.getNoisyPeriods();
            for (int i = 0; i < np.length; ++i) {
                var[np[i]] += nvar;
            }
            ISsfDynamics sdyn = new SeasonalComponent.HarrisonStevensDynamics(var);
            dyn.add(sdyn);
            m.add(Measurement.cyclical(freq, pstart));
        }
        CompositeDynamics cdyn = new CompositeDynamics(dyn.toArray(new ISsfDynamics[dyn.size()]));
        ISsfMeasurement cm = CompositeMeasurement.of(bsm.getVariance(Component.Noise), m.toArray(new ISsfMeasurement[m.size()]));
        return new Ssf(cdyn, cm);
    }
}
