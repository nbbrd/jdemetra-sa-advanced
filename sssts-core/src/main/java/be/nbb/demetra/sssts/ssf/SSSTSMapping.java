/*
 * Copyright 2013 National Bank of Belgium
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
import static ec.demetra.realfunctions.IParametersDomain.PARAM;
import ec.demetra.realfunctions.IParametricMapping;
import ec.demetra.realfunctions.ParamValidation;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public abstract class SSSTSMapping implements IParametricMapping<SSSTSModel> {

    final SSSTSModel model;

    SSSTSMapping(SSSTSModel m) {
        model = m;
    }

    public static SSSTSMapping all(SSSTSModel m) {
        return new All(m);
    }

    public static SSSTSMapping bsm(SSSTSModel m) {
        return new Bsm(m);
    }

    public static SSSTSMapping noise(SSSTSModel m) {
        return new Noise(m);
    }

    public static final String TH = "th", BTH = "bth", NVAR = "noise var";

    private static class All extends SSSTSMapping {

        All(SSSTSModel m) {
            super(m);
        }

        @Override
        public SSSTSModel map(IReadDataBlock p) {
            SSSTSModel m = model.clone();
            double q = p.get(0);
            m.setNoisyPeriodsVariance(q * q);
            int cur = 1;
            if (m.getLvar() != 0 && m.getLvar() != 1) {
                q = p.get(cur++);
                m.setLvar(q * q);
            }
            if (m.getSvar() != 0 && m.getSvar() != 1) {
                q = p.get(cur++);
                m.setSvar(q * q);
            }
            if (m.getSeasvar() != 0 && m.getSeasvar() != 1) {
                q = p.get(cur++);
                m.setSeasvar(q * q);
            }
            if (m.getNvar() != 0 && m.getNvar() != 1) {
                q = p.get(cur);
                m.setNvar(q * q);
            }
            return m;
        }

        @Override
        public boolean checkBoundaries(IReadDataBlock inparams) {
//        if (inparams.get(2)<=0)
//            return false;
            return true;
        }

        @Override
        public double epsilon(IReadDataBlock inparams, int idx) {
            return 1e-4;
        }

        @Override
        public int getDim() {
            int n = 1;
            if (model.getLvar() != 0 && model.getLvar() != 1) {
                ++n;
            }
            if (model.getSvar() != 0 && model.getSvar() != 1) {
                ++n;
            }
            if (model.getSeasvar() != 0 && model.getSeasvar() != 1) {
                ++n;
            }
            if (model.getNvar() != 0 && model.getNvar() != 1) {
                ++n;
            }

            return n;
        }

        @Override
        public double lbound(int idx) {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double ubound(int idx) {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public ParamValidation validate(IDataBlock ioparams) {
//        if (ioparams.get(2) <=0){
//            ioparams.set(2, .0001);
//            return ParamValidation.Changed;
//        }
            return ParamValidation.Valid;
        }

        @Override
        public IReadDataBlock getDefault() {
            double[] def = new double[getDim()];
            def[0] = Math.sqrt(model.getNoisyPeriodsVariance());
            int cur = 1;
            double var = model.getLvar();
            if (var != 0 && var != 1) {
                def[cur++] = Math.sqrt(var);
            }
            var = model.getSvar();
            if (var != 0 && var != 1) {
                def[cur++] = Math.sqrt(var);
            }
            var = model.getSeasvar();
            if (var != 0 && var != 1) {
                def[cur++] = Math.sqrt(var);
            }
            var = model.getNvar();
            if (var != 0 && var != 1) {
                def[cur] = Math.sqrt(var);
            }
            return new ReadDataBlock(def);
        }
    }

    private static class Bsm extends SSSTSMapping {

        Bsm(SSSTSModel m) {
            super(m);
        }

        @Override
        public SSSTSModel map(IReadDataBlock p) {
            SSSTSModel m = model.clone();
            int cur = 0;
            if (m.getLvar() != 0 && m.getLvar() != 1) {
                double q = p.get(cur++);
                m.setLvar(q * q);
            }
            if (m.getSeasvar() != 0 && m.getSeasvar() != 1) {
                double q = p.get(cur++);
                m.setSeasvar(q * q);
            }
            if (m.getSvar() != 0 && m.getSvar() != 1) {
                double q = p.get(cur++);
                m.setSvar(q * q);
            }
            if (m.getNvar() != 0 && m.getNvar() != 1) {
                double q = p.get(cur);
                m.setNvar(q * q);
            }
            return m;
        }

        @Override
        public boolean checkBoundaries(IReadDataBlock inparams) {
//        if (inparams.get(2)<=0)
//            return false;
            return true;
        }

        @Override
        public double epsilon(IReadDataBlock inparams, int idx) {
            return 1e-4;
        }

        @Override
        public int getDim() {
            int n = 0;
            if (model.getLvar() != 0 && model.getLvar() != 1) {
                ++n;
            }
            if (model.getSeasvar() != 0 && model.getSeasvar() != 1) {
                ++n;
            }
            if (model.getSvar() != 0 && model.getSvar() != 1) {
                ++n;
            }
            if (model.getNvar() != 0 && model.getNvar() != 1) {
                ++n;
            }
            return n;
        }

        @Override
        public double lbound(int idx) {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double ubound(int idx) {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public ParamValidation validate(IDataBlock ioparams) {
//        if (ioparams.get(2) <=0){
//            ioparams.set(2, .0001);
//            return ParamValidation.Changed;
//        }
            return ParamValidation.Valid;
        }

        @Override
        public IReadDataBlock getDefault() {
            double[] def = new double[getDim()];
            int cur = 0;
            double var = model.getLvar();
            if (var != 0 && var != 1) {
                def[cur++] = Math.sqrt(var);
            }
            var = model.getSeasvar();
            if (var != 0 && var != 1) {
                def[cur++] = Math.sqrt(var);
            }
            var = model.getSvar();
            if (var != 0 && var != 1) {
                def[cur++] = Math.sqrt(var);
            }
            var = model.getNvar();
            if (var != 0 && var != 1) {
                def[cur] = Math.sqrt(var);
            }
            return new ReadDataBlock(def);
        }
    }

    private static class Noise extends SSSTSMapping {

        Noise(SSSTSModel m) {
            super(m);
        }

        @Override
        public SSSTSModel map(IReadDataBlock p) {
            SSSTSModel nmodel = model.clone();
            nmodel.setNoisyPeriodsVariance(p.get(0) * p.get(0));
            return nmodel;
        }

        @Override
        public String getDescription(int idx) {
            switch (idx) {
                case 0:
                    return NVAR;
            }
            return PARAM + idx;
        }

        @Override
        public boolean checkBoundaries(IReadDataBlock inparams) {
            return true;
        }

        @Override
        public double epsilon(IReadDataBlock inparams, int idx) {
            return 1e-4;
        }

        @Override
        public int getDim() {
            return 1;
        }

        @Override
        public double lbound(int idx) {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double ubound(int idx) {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public ParamValidation validate(IDataBlock ioparams) {
            return ParamValidation.Valid;
        }

        @Override
        public IReadDataBlock getDefault() {
            return new ReadDataBlock(new double[]{Math.sqrt(model.getNoisyPeriodsVariance())});
        }
    }
}
