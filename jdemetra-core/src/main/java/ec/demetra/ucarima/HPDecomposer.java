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
package ec.demetra.ucarima;

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;

/**
 *
 * @author Jean Palate
 */
public class HPDecomposer implements ITrendCycleDecomposer {

    private ArimaModel trend, cycle, sum;
    /**
     * Cutting-point of the trend-cycle partition (expressed as a number of
     * periods)
     */
    protected double lambda = 1600;

    @Override
    public boolean decompose(IArimaModel trendcycle) {
        clear();
        sum = ArimaModel.create(trendcycle);
        double v = sum.getInnovationVariance();
        BackFilter ar = sum.getStationaryAR();
        BackFilter ur = sum.getNonStationaryAR();
        BackFilter ma = sum.getMA();
        ArimaModel hp=computeHPModel();
        
        // computes the number of (1-B) in ur
        int d=0;
        Polynomial sur=ur.getPolynomial();
        do{
            Polynomial.Division div=Polynomial.divide(sur, UnitRoots.D1);
            if (! div.isExact())
                break;
            sur=div.getQuotient();
        }while (++d != 2);
        
        BackFilter cma=ma;
        while (d++ != 2){
            cma=cma.times(BackFilter.D1);
        }
        
        trend=new ArimaModel(hp.getMA().times(ar), ur, ma, v/hp.getInnovationVariance());
        cycle=new ArimaModel(hp.getMA().times(ar), null, cma, v*lambda/hp.getInnovationVariance());
        return true;
    }

    @Override
    public ArimaModel getTrend() {
        return trend;
    }

    @Override
    public ArimaModel getCycle() {
        return cycle;
    }

    private void clear() {
        trend = null;
        cycle = null;
    }

    /**
     * @return the tau
     */
    public double getTau() {
        return 2*Math.PI/Math.acos(1-1/(2*Math.sqrt(lambda)));
    }

    /**
     * @param tau the tau to set
     */
    public void setTau(double tau) {
        double x=2*(1-Math.cos(2*Math.PI/tau));
        lambda =1/(x*x);
    }

    /**
     * @return lambda (=2pi/tau)
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * Sets lambda (=2pi/tau)
     *
     * @param lambda the value to set
     */
    public void setLambda(double lambda) {
        this.lambda=lambda;
    }

    private ArimaModel computeHPModel() {
        ArimaModel S=new ArimaModel(null, BackFilter.D1.times(BackFilter.D1), null, 1);
        ArimaModel N=new ArimaModel(lambda);
        return S.plus(N);
    }
    
}
