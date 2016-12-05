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

import be.nbb.demetra.sssts.SSSTSModel;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.implementations.Measurement;
import ec.demetra.ssf.implementations.NoisyMeasurement;
import ec.demetra.ssf.implementations.structural.Component;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.demetra.ssf.univariate.Ssf;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 *
 * @author Jean Palate
 */
public class SsfofSSSTS {

    public static ISsf of(final SSSTSModel model, final int pstart) {
        switch (model.getNoisyComponent()) {
            case Noise:
                return ofNoise(model, pstart);
            case Level:
                return ofLevel(model, pstart);
            case Slope:
                return ofSlope(model, pstart);
        }
        return null;
    }

    private static ISsf ofNoise(final SSSTSModel model, final int pstart) {
        int s = model.getFrequency();
        ISsfDynamics dyn = new SSLLTDyn(model);
        ISsfMeasurement m = Measurement.cyclical(s, pstart, dyn.getStateDim());

        final int[] np = model.getNoisyPeriods();
        final boolean[] noisy = new boolean[s];
        final double evar = model.getNvar();
        NoisyMeasurement.INoise noise;
        if (np != null) {
            final double nvar = np == null ? 0 : model.getNoisyPeriodsVariance();
            for (int i = 0; i < np.length; ++i) {
                noisy[np[i]] = true;
            }
            noise = (int pos) -> noisy[(pstart + pos) % noisy.length] ? evar + nvar : evar;

        } else {
            noise = (int pos) -> evar;
        }
        return new Ssf(dyn, NoisyMeasurement.of(m, noise));
    }

    private static ISsf ofLevel(final SSSTSModel model, final int pstart) {
        int s = model.getFrequency();
        ISsfDynamics dyn = new SSLLTDyn(model);
        ISsfMeasurement m = Measurement.cyclical(s, pstart, dyn.getStateDim());

        final double evar = model.getNvar();
        NoisyMeasurement.INoise noise = (int pos) -> {
            return evar;
        };
        return new Ssf(dyn, NoisyMeasurement.of(m, noise));
    }

    private static ISsf ofSlope(final SSSTSModel model, final int pstart) {
        int s = model.getFrequency();
        ISsfDynamics dyn = new SSLLTDyn2(model);
        ISsfMeasurement m = Measurement.cyclical(s, pstart, dyn.getStateDim());

        final double evar = model.getNvar();
        NoisyMeasurement.INoise noise = (int pos) -> {
            return evar;
        };
        return new Ssf(dyn, NoisyMeasurement.of(m, noise));

    }

    static class SSLLDyn implements ISsfDynamics {

        private final int s;
        private final double lvar, seasvar;

        SSLLDyn(SSSTSModel model) {
            s = model.getFrequency();
            lvar = model.getLvar();
            seasvar = model.getSeasvar();
        }

        @Override
        public int getInnovationsDim() {
            return s;
        }

        @Override
        public void V(int pos, SubMatrix qm) {
            SubMatrix lv = qm.extract(0, s, 0, s);
            lv.set(lvar);
            lv.diagonal().add(seasvar);
        }

        @Override
        public void S(int pos, SubMatrix cm) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean hasInnovations(int pos) {
            return lvar > 0 || seasvar > 0;
        }

        @Override
        public void T(int pos, SubMatrix tr) {
            tr.diagonal().set(1);
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getNonStationaryDim() {
            return s;
        }

        @Override
        public void diffuseConstraints(SubMatrix b) {
            b.diagonal().set(1);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(SubMatrix pf0) {
            return true;
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void addV(int pos, SubMatrix p) {
            p.add(lvar);
            p.diagonal().add(seasvar);
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getStateDim() {
            return s;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    static class SSLLTDyn implements ISsfDynamics {

        private final int s;
        private final double svar, lvar;
        private final DataBlock slvar;

        SSLLTDyn(SSSTSModel model) {
            s = model.getFrequency();
            svar = model.getSvar();
            lvar = model.getLvar();
            double[] xvar = new double[s];
            slvar = new DataBlock(xvar);
            int[] np = model.getNoisyPeriods();
            double ssvar1 = model.getSeasvar();
            slvar.set(ssvar1);
            if (model.getNoisyComponent() == Component.Level && np != null) {
                double ssvar2 = model.getNoisyPeriodsVariance();
                for (int i = 0; i < np.length; ++i) {
                    xvar[np[i]] = ssvar1 + ssvar2;
                }
            }
        }

        @Override
        public int getInnovationsDim() {
            return s + 1;
        }

        @Override
        public void V(int pos, SubMatrix qm) {
            SubMatrix lv = qm.extract(0, s, 0, s);
            lv.set(lvar);
            lv.diagonal().add(slvar);
            qm.set(s, s, svar);
        }

        @Override
        public void S(int pos, SubMatrix cm) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void T(int pos, SubMatrix tr) {
            tr.diagonal().set(1);
            tr.column(s).set(1);
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getNonStationaryDim() {
            return s + 1;
        }

        @Override
        public void diffuseConstraints(SubMatrix b) {
            b.diagonal().set(1);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(SubMatrix pf0) {
            return true;
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.range(0, s).add(x.get(s));
        }

        @Override
        public void addV(int pos, SubMatrix p) {
            SubMatrix lv = p.extract(0, s, 0, s);
            lv.add(lvar);
            lv.diagonal().add(slvar);
            p.add(s, s, svar);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.add(s, x.range(0, s).sum());
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getStateDim() {
            return s + 1;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    static class SSLLTDyn2 implements ISsfDynamics {

        private final int s;
        private final double lvar, svar1, svar2, seasvar;
        private final int[] ins;

        SSLLTDyn2(SSSTSModel model) {
            s = model.getFrequency();
            svar1 = model.getSvar();
            lvar = model.getLvar();
            seasvar = model.getSeasvar();
            ins = new int[s];
            int[] np = model.getNoisyPeriods().clone();
            svar2 = model.getNoisyPeriodsVariance();
            for (int i = 0; i < np.length; ++i) {
                ins[np[i]] = 1;
            }
        }

        @Override
        public int getInnovationsDim() {
            return s + s;
        }

        @Override
        public void V(int pos, SubMatrix qm) {
            SubMatrix lv = qm.extract(0, s, 0, s);
            lv.set(lvar);
            lv.diagonal().add(seasvar);
            qm.set(s, s, svar1);
            qm.set(s + 1, s + 1, svar2);
        }

        @Override
        public void S(int pos, SubMatrix cm) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void T(int pos, SubMatrix tr) {
            tr.diagonal().set(1);
            for (int i = 0; i < s; ++i) {
                tr.set(i, s + ins[i], 1);
            }
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getNonStationaryDim() {
            return s + 2;
        }

        @Override
        public void diffuseConstraints(SubMatrix b) {
            b.diagonal().set(1);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(SubMatrix pf0) {
            return true;
        }

        @Override
        public void TX(int pos, DataBlock x) {
            double z0 = x.get(s), z1 = x.get(s + 1);
            for (int i = 0; i < s; ++i) {
                x.add(i, ins[i] == 0 ? z0 : z1);
            }
        }

        @Override
        public void addV(int pos, SubMatrix p) {
            SubMatrix lv = p.extract(0, s, 0, s);
            lv.add(lvar);
            lv.diagonal().add(seasvar);
            p.add(s, s, svar1);
            p.add(s + 1, s + 1, svar2);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            for (int i=0; i<s; ++i){
                if (ins[i] == 0)
                    x.add(s, x.get(i));
                else
                    x.add(s+1, x.get(i));
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getStateDim() {
            return s + 2;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    static class SSLLTFullDyn implements ISsfDynamics {

        private final int s;
        private final double lvar, svar, seasvar;
        private final DataBlock ssvar;

        SSLLTFullDyn(SSSTSModel model) {
            s = model.getFrequency();
            svar = model.getSvar();
            lvar = model.getLvar();
            seasvar = model.getSeasvar();
            double[] xvar = new double[s];
            ssvar = new DataBlock(xvar);
            int[] np = model.getNoisyPeriods();
            if (model.getNoisyComponent() == Component.Slope && np != null) {
                double ssvar2 = model.getNoisyPeriodsVariance();
                for (int i = 0; i < np.length; ++i) {
                    xvar[np[i]] = ssvar2;
                }
            }
        }

        @Override
        public int getInnovationsDim() {
            return s + s;
        }

        @Override
        public void V(int pos, SubMatrix qm) {
            SubMatrix lv = qm.extract(0, s, 0, s);
            lv.set(lvar);
            lv.diagonal().add(seasvar);
            SubMatrix sv = qm.extract(s, s + s, s, s + s);
            sv.set(svar);
            sv.diagonal().add(ssvar);
        }

        @Override
        public void S(int pos, SubMatrix cm) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void T(int pos, SubMatrix tr) {
            tr.diagonal().set(1);
            tr.subDiagonal(s).set(1);
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getNonStationaryDim() {
            return s + s;
        }

        @Override
        public void diffuseConstraints(SubMatrix b) {
            b.diagonal().set(1);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(SubMatrix pf0) {
            return true;
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.range(0, s).add(x.range(s, s + s));
        }

        @Override
        public void addV(int pos, SubMatrix p) {
            SubMatrix lv = p.extract(0, s, 0, s);
            lv.add(lvar);
            lv.diagonal().add(seasvar);
            SubMatrix sv = p.extract(s, s + s, s, s + s);
            sv.add(svar);
            sv.diagonal().add(ssvar);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.range(s, s + s).add(x.range(0, s));
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getStateDim() {
            return s + s;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
