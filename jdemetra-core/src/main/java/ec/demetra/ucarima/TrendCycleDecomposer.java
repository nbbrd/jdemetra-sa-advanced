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
public class TrendCycleDecomposer implements ITrendCycleDecomposer {

    private ArimaModel trend, cycle, sum;
    protected int del=3;
    /**
     * Cutting-point of the trend-cycle partition (expressed as a number of
     * periods)
     */
    private double lambda = 1600;

    @Override
    public boolean decompose(IArimaModel trendcycle) {
        clear();
        sum = ArimaModel.create(trendcycle);
        double v = sum.getInnovationVariance();
        BackFilter ar = sum.getStationaryAR();
        BackFilter ur = sum.getNonStationaryAR();
        BackFilter ma = sum.getMA();
        ArimaModel tc=computeTCModel();
        
        // computes the number of (1-B) in ur
        int d=0;
        Polynomial sur=ur.getPolynomial();
        do{
            Polynomial.Division div=Polynomial.divide(sur, UnitRoots.D1);
            if (! div.isExact())
                break;
            sur=div.getQuotient();
        }while (++d != del);
        
        BackFilter cma=ma;
        while (d++ != del){
            cma=cma.times(BackFilter.D1);
        }
        
        trend=new ArimaModel(tc.getMA().times(ar), ur, ma, v/tc.getInnovationVariance());
        cycle=new ArimaModel(tc.getMA().times(ar), null, cma, v*lambda/tc.getInnovationVariance());
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
        return 2*Math.PI/Math.acos(1-1/(2*Math.pow(lambda, 1.0/del)));
    }

    /**
     * @param tau the tau to set
     */
    public void setTau(double tau) {
        double x=2*(1-Math.cos(2*Math.PI/tau));
        lambda =1/(Math.pow(x, del));
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

    private ArimaModel computeTCModel() {
        ArimaModel S=new ArimaModel(null, BackFilter.of(UnitRoots.D(1, del).getCoefficients()), null, 1);
        ArimaModel N=new ArimaModel(lambda);
        return S.plus(N);
    }

    /**
     * @return the del
     */
    public int getDifferencing() {
        return del;
    }

    /**
     * @param del the del to set
     */
    public void setDifferencing(int del) {
        this.del = del;
    }
    
}
