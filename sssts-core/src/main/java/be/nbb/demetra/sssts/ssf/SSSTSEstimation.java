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
import ec.demetra.realfunctions.IFunctionMinimizer;
import ec.demetra.realfunctions.ProxyMinimizer;
import ec.demetra.ssf.dk.SsfFunction;
import ec.demetra.ssf.dk.SsfFunctionInstance;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.SsfData;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
public class SSSTSEstimation {

    private IFunctionMinimizer minimizer = new ProxyMinimizer(new ec.demetra.realfunctions.levmar.LevenbergMarquardtMinimzer());

    public SSSTSEstimation() {
    }

    public SSSTSModel compute(SSSTSModel model, TsData s) {
        try {
            if (model.getNoisyPeriods() == null) {
                return searchBsm(model, s, 1e-12);
            } else {
                return searchAll(model, s, 1e-12);
            }
        } catch (RuntimeException e) {
            return null;
        }
    }

    public SSSTSModel compute2(SSSTSModel model, TsData s) {
        try {
            if (model.getNoisyPeriods() == null) {
                return searchBsm(model, s, 1e-12);
            }
            for (int i = 0; i < 5; ++i) {
                model = searchNoise(model, s);
                model = searchBsm(model, s, 1e-5);
            }
            return searchAll(model, s, 1e-12);

        } catch (RuntimeException e) {
            return null;
        }
    }

    private SSSTSModel searchBsm(SSSTSModel model, TsData s, double eps) {
        SSSTSMapping mapper = SSSTSMapping.bsm(model);
        minimizer.setConvergenceCriterion(eps);
        SsfFunction<SSSTSModel, ISsf> fn = new SsfFunction<>(new SsfData(s), mapper,
                (SSSTSModel m) -> SsfofSSSTS.of(m, s.getStart().getPosition()));
        boolean converged = minimizer.minimize(fn);
        SsfFunctionInstance<SSSTSModel, ISsf> rfn = (SsfFunctionInstance<SSSTSModel, ISsf>) minimizer.getResult();
        SSSTSModel core = rfn.getCore();
        core.rescaleVariances();
        return core;
    }

    private SSSTSModel searchAll(SSSTSModel model, TsData s, double eps) {
        SSSTSMapping mapper = SSSTSMapping.all(model);
        minimizer.setConvergenceCriterion(eps);
        SsfFunction<SSSTSModel, ISsf> fn = new SsfFunction<>(new SsfData(s), mapper,
                (SSSTSModel m) -> SsfofSSSTS.of(m, s.getStart().getPosition()));
        boolean converged = minimizer.minimize(fn);
        SsfFunctionInstance<SSSTSModel, ISsf> rfn = (SsfFunctionInstance<SSSTSModel, ISsf>) minimizer.getResult();
        SSSTSModel core = rfn.getCore();
        core.rescaleVariances();
        return core;
    }

    private SSSTSModel searchNoise(SSSTSModel model, TsData s) {
        SSSTSMapping mapper = SSSTSMapping.noise(model);
        SsfFunction<SSSTSModel, ISsf> fn = new SsfFunction<>(new SsfData(s), mapper,
                (SSSTSModel m) -> SsfofSSSTS.of(m, s.getStart().getPosition()));
        minimizer.minimize(fn);
        SsfFunctionInstance<SSSTSModel, ISsf> rfn = (SsfFunctionInstance<SSSTSModel, ISsf>) minimizer.getResult();
        return rfn.getCore();
    }

}
