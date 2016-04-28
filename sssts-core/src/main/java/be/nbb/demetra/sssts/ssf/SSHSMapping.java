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

import be.nbb.demetra.sssts.SSHSModel;
import ec.demetra.ssf.implementations.structural.BasicStructuralModel;
import be.nbb.demetra.sts.BsmMapping;
import static ec.demetra.realfunctions.IParametersDomain.PARAM;
import ec.demetra.realfunctions.IParametricMapping;
import ec.demetra.realfunctions.ParamValidation;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.sarima.estimation.SarimaMapping;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public abstract class SSHSMapping implements IParametricMapping<SSHSModel> {

    final int[] noisyPeriods;
    final double nvarRef;
    final BsmMapping bsmMapping;
    final IReadDataBlock bsmRef;

    SSHSMapping(SSHSModel m) {
        noisyPeriods = m.getNoisyPeriods();
        nvarRef = m.getNoisyPeriodsVariance();
        BasicStructuralModel bsm = m.getBasicStructuralModel();
        bsmMapping = new BsmMapping(bsm.getSpecification(), bsm.getFrequency());
        bsmMapping.setFixedComponent(bsm.getMaxVariance());
        bsmRef = bsmMapping.map(bsm);
    }

    public static SSHSMapping all(SSHSModel m) {
        return new All(m);
    }

    public static SSHSMapping bsm(SSHSModel m) {
        return new Bsm(m);
    }

    public static SSHSMapping noise(SSHSModel m) {
        return new Noise(m);
    }

    public static final String TH = "th", BTH = "bth", NVAR = "noise var";

    private static class All extends SSHSMapping {

        All(SSHSModel m) {
            super(m);
        }

        @Override
        public SSHSModel map(IReadDataBlock p) {
            SSHSModel m = new SSHSModel();
            BasicStructuralModel bsm = bsmMapping.map(p.rextract(1, p.getLength() - 1));
            m.setBasicStructuralMode(bsm);
            m.setNoisyPeriods(noisyPeriods);
            m.setNoisyPeriodsVariance(p.get(0) * p.get(0));
            return m;
        }

        @Override
        public String getDescription(int idx) {
            if (idx == 0) {
                return NVAR;
            } else {
                return bsmMapping.getDescription(idx - 1);
            }
        }

        @Override
        public boolean checkBoundaries(IReadDataBlock inparams) {
//        if (inparams.get(2)<=0)
//            return false;
            return true;
        }

        @Override
        public double epsilon(IReadDataBlock inparams, int idx) {
            return 1e-6;
        }

        @Override
        public int getDim() {
            return 1 + bsmMapping.getDim();
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
            double[] def = new double[bsmRef.getLength() + 1];
            def[0] = Math.sqrt(nvarRef);
            bsmRef.copyTo(def, 1);
            return new ReadDataBlock(def);
        }
    }

    private static class Bsm extends SSHSMapping {

        Bsm(SSHSModel m) {
            super(m);
        }

        @Override
        public SSHSModel map(IReadDataBlock p) {
            SSHSModel m = new SSHSModel();
            BasicStructuralModel bsm = bsmMapping.map(p);
            m.setBasicStructuralMode(bsm);
            m.setNoisyPeriods(noisyPeriods);
            m.setNoisyPeriodsVariance(nvarRef);
            return m;
        }

        @Override
        public String getDescription(int idx) {
            return bsmMapping.getDescription(idx);
        }

        @Override
        public boolean checkBoundaries(IReadDataBlock inparams) {
//        if (inparams.get(2)<=0)
//            return false;
            return true;
        }

        @Override
        public double epsilon(IReadDataBlock inparams, int idx) {
            return 1e-6;
        }

        @Override
        public int getDim() {
            return bsmMapping.getDim();
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
            return bsmRef;
        }
    }

    private static class Noise extends SSHSMapping {

        Noise(SSHSModel m) {
            super(m);
        }

        @Override
        public SSHSModel map(IReadDataBlock p) {
            SSHSModel model = new SSHSModel();
            BasicStructuralModel bsm = bsmMapping.map(bsmRef);
            model.setBasicStructuralMode(bsm);
            model.setNoisyPeriods(noisyPeriods);
            model.setNoisyPeriodsVariance(p.get(0) * p.get(0));
            return model;
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
//        if (inparams.get(2)<=0)
//            return false;
            return true;
        }

        @Override
        public double epsilon(IReadDataBlock inparams, int idx) {
            return 1e-3;
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
//        if (ioparams.get(2) <=0){
//            ioparams.set(2, .0001);
//            return ParamValidation.Changed;
//        }
            return ParamValidation.Valid;
        }

        @Override
        public IReadDataBlock getDefault() {
            return new ReadDataBlock(new double[]{Math.sqrt(nvarRef)});
        }
    }
}
