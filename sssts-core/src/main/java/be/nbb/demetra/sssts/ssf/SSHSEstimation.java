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
import ec.demetra.ssf.implementations.structural.BasicStructuralModel;
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
public class SSHSEstimation {

    private IFunctionMinimizer minimizer = new ProxyMinimizer(new ec.demetra.realfunctions.levmar.LevenbergMarquardtMinimzer());
    private final boolean noise;
    private final int start;
    
    public SSHSEstimation(boolean noise, int start){
        this.noise=noise;
        this.start=start;
    }
    
    public SSHSModel compute(BasicStructuralModel bsm, int[] noisy, TsData s) {
        try {
            SSHSModel model = new SSHSModel();
            model.setBasicStructuralMode(bsm);
            model.setNoisyPeriods(noisy);
            model.setNoisyPeriodsVariance(1);
            return searchAll(model, s, 1e-12);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public SSHSModel compute2(BasicStructuralModel bsm, int[] noisy, TsData s) {
        try {
            SSHSModel model = new SSHSModel();
            model.setBasicStructuralMode(bsm);
            model.setNoisyPeriods(noisy);
            model.setNoisyPeriodsVariance(1);

            for (int i = 0; i < 5; ++i) {
                model = searchNoise(model, s);
                model = searchBsm(model, s);
            }
            return searchAll(model, s, 1e-12);

        } catch (RuntimeException e) {
            return null;
        }
    }

    private SSHSModel searchBsm(SSHSModel model, TsData s) {
        SSHSMapping mapper = SSHSMapping.bsm(model);
        minimizer.setConvergenceCriterion(1e-6);
        SsfFunction<SSHSModel, ISsf> fn = new SsfFunction<>(new SsfData(s), mapper, 
                (SSHSModel m) ->noise ? SsfofSSHS.ofNoise(m, start):SsfofSSHS.ofSeasonal(m, start));
        boolean converged = minimizer.minimize(fn);
        SsfFunctionInstance<SSHSModel, ISsf> rfn = (SsfFunctionInstance<SSHSModel, ISsf>) minimizer.getResult();
        return rfn.getCore();
    }

    private SSHSModel searchAll(SSHSModel model, TsData s, double eps) {
        SSHSMapping mapper = SSHSMapping.all(model);
        minimizer.setConvergenceCriterion(eps);
        SsfFunction<SSHSModel, ISsf> fn = new SsfFunction<>(new SsfData(s), mapper, 
                (SSHSModel m) ->noise ? SsfofSSHS.ofNoise(m, start):SsfofSSHS.ofSeasonal(m, start));
        boolean converged = minimizer.minimize(fn);
        SsfFunctionInstance<SSHSModel, ISsf> rfn = (SsfFunctionInstance<SSHSModel, ISsf>) minimizer.getResult();
        return rfn.getCore();
    }

    private SSHSModel searchNoise(SSHSModel model, TsData s) {
        SSHSMapping mapper = SSHSMapping.noise(model);
        SsfFunction<SSHSModel, ISsf> fn = new SsfFunction<>(new SsfData(s), mapper, 
            (SSHSModel m) ->noise ? SsfofSSHS.ofNoise(m, start):SsfofSSHS.ofSeasonal(m, start));
        minimizer.minimize(fn);
        SsfFunctionInstance<SSHSModel, ISsf> rfn = (SsfFunctionInstance<SSHSModel, ISsf>) minimizer.getResult();
        return rfn.getCore();
    }

}
