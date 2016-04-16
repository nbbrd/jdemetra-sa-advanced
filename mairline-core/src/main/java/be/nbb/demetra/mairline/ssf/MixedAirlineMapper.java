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
package be.nbb.demetra.mairline.ssf;

import be.nbb.demetra.mairline.*;
import static ec.demetra.realfunctions.IParametersDomain.PARAM;
import ec.demetra.realfunctions.IParametricMapping;
import ec.demetra.realfunctions.ParamValidation;
import ec.demetra.ssf.univariate.ISsf;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public abstract class MixedAirlineMapper implements IParametricMapping<ISsf> {

      final int freq;
        final int[] noisyPeriods;
        final double th, bth, nvar;

    MixedAirlineMapper(MixedAirlineModel m) {
        freq=m.getFrequency();
        noisyPeriods=m.getNoisyPeriods();
        th=m.getTheta();
        bth=m.getBTheta();
        nvar=m.getNoisyPeriodsVariance();
    }

    public abstract MixedAirlineModel toModel(IReadDataBlock p);

    public static MixedAirlineMapper all(MixedAirlineModel m) {
        return new All(m);
    }

    public static MixedAirlineMapper airline(MixedAirlineModel m) {
        return new Airline(m);
    }

    public static MixedAirlineMapper noise(MixedAirlineModel m) {
        return new Noise(m);
    }

    public static final String TH = "th", BTH = "bth", NVAR = "noise var";

    private static class All extends MixedAirlineMapper {

        All(MixedAirlineModel m) {
            super(m);
        }


        @Override
        public MixedAirlineModel toModel(IReadDataBlock p) {
            SarimaModelBuilder builder = new SarimaModelBuilder();
            SarimaModel airline = builder.createAirlineModel(freq, p.get(0), p.get(1));
            MixedAirlineModel m = new MixedAirlineModel();
            m.setAirline(airline);
            m.setNoisyPeriods(noisyPeriods);
            m.setNoisyPeriodsVariance(p.get(2)*p.get(2));
            return m;
        }

        @Override
        public ISsf map(IReadDataBlock p) {
            SarimaModelBuilder builder = new SarimaModelBuilder();
            SarimaModel airline = builder.createAirlineModel(freq, p.get(0), p.get(1));
            MixedAirlineModel m = new MixedAirlineModel();
            m.setAirline(airline);
            m.setNoisyPeriods(noisyPeriods);
            m.setNoisyPeriodsVariance(p.get(2)*p.get(2));
            return MixedAirlineSsf.of(m);
        }

        @Override
        public String getDescription(int idx) {
            switch (idx) {
                case 0:
                    return TH;
                case 1:
                    return BTH;
                case 2:
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
            return 1e-6;
        }

        @Override
        public int getDim() {
            return 3;
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
            return new ReadDataBlock(new double[]{th, bth, Math.sqrt(nvar)});
        }
    }

    private static class Airline extends MixedAirlineMapper {

        Airline(MixedAirlineModel m) {
            super(m);
        }

        @Override
        public MixedAirlineModel toModel(IReadDataBlock p) {
            SarimaModelBuilder builder = new SarimaModelBuilder();
            SarimaModel airline = builder.createAirlineModel(freq, p.get(0), p.get(1));
            MixedAirlineModel m = new MixedAirlineModel();
            m.setAirline(airline);
            m.setNoisyPeriods(noisyPeriods);
            m.setNoisyPeriodsVariance(nvar);
            return m;
        }

        @Override
        public ISsf map(IReadDataBlock p) {
            SarimaModelBuilder builder = new SarimaModelBuilder();
            SarimaModel airline = builder.createAirlineModel(freq, p.get(0), p.get(1));
            MixedAirlineModel m = new MixedAirlineModel();
            m.setAirline(airline);
            m.setNoisyPeriods(noisyPeriods);
            m.setNoisyPeriodsVariance(nvar);
            return MixedAirlineSsf.of(m);
        }

        @Override
        public String getDescription(int idx) {
            switch (idx) {
                case 0:
                    return TH;
                case 1:
                    return BTH;
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
            return 1e-6;
        }

        @Override
        public int getDim() {
            return 2;
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
            return new ReadDataBlock(new double[]{th, bth});
        }
    }

    private static class Noise extends MixedAirlineMapper {

        Noise(MixedAirlineModel m) {
            super(m);
         }

  
        @Override
        public MixedAirlineModel toModel(IReadDataBlock p) {
            SarimaModelBuilder builder = new SarimaModelBuilder();
            SarimaModel airline = builder.createAirlineModel(freq, th, bth);
            MixedAirlineModel model = new MixedAirlineModel();
            model.setAirline(airline);
            model.setNoisyPeriods(noisyPeriods);
            model.setNoisyPeriodsVariance(p.get(0)*p.get(0));
            return model;
        }

        @Override
        public ISsf map(IReadDataBlock p) {
            SarimaModelBuilder builder = new SarimaModelBuilder();
            SarimaModel airline = builder.createAirlineModel(freq, th, bth);
            MixedAirlineModel model = new MixedAirlineModel();
            model.setAirline(airline);
            model.setNoisyPeriods(noisyPeriods);
            model.setNoisyPeriodsVariance(p.get(0)*p.get(0));
            return MixedAirlineSsf.of(model);
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
            return 1e-6;
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
            return new ReadDataBlock(new double[]{Math.sqrt(nvar)});
        }
    }
}
