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

package ec.demetra.realfunctions.minpack;

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.demetra.realfunctions.ISsqFunction;
import ec.demetra.realfunctions.ISsqFunctionMinimizer;
import ec.demetra.realfunctions.ISsqFunctionPoint;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class LevenbergMarquardtMinimizer implements ISsqFunctionMinimizer {

    private final LevenbergMarquardtEstimator m_estimator = new LevenbergMarquardtEstimator();

    private SsqEstimationProblem m_problem;
    
    public void setHook(ILmHook hook){
        m_estimator.setHook(hook);
    }

    @Override
    public ISsqFunctionMinimizer exemplar() {
	return new LevenbergMarquardtMinimizer();
    }

    /**
     * 
     * @return
     */
    @Override
    public double getConvergenceCriterion() {
	return m_estimator.getCostRelativeTolerance();
    }

    /**
     * 
     * @return
     */
    @Override
    public Matrix getCurvature() {
        try{
	return m_estimator.curvature(m_problem);
        }
        catch(Exception err){
            return null;
        }
    }
    
    @Override
    public IReadDataBlock getGradient(){
        return this. m_problem.getGradient();
    }
            

    /**
     *
     * @return
     */
    @Override
    public int getIterCount() {
	return m_estimator.getIterCount();
    }

    /**
     *
     * @return
     */
    @Override
    public int getMaxIter() {
	return m_estimator.getMaxIter();
    }

    @Override
    public ISsqFunctionPoint getResult() {
	return m_problem.getResult();
    }

     @Override
    public double getObjective() {
	return m_problem.getResult() == null ? Double.NaN : m_problem.getResult().getSsqE();
    }
    
    @Override
    public boolean minimize(ISsqFunctionPoint start) {
	m_problem = new SsqEstimationProblem(start);
	try {
	    m_estimator.estimate(m_problem);
	    return m_estimator.getIterCount() < m_estimator.getMaxIter();
	} catch (RuntimeException err) {
	    return false;
	}
    }

    @Override
    public void setConvergenceCriterion(double value) {
	m_estimator.setCostRelativeTolerance(value);
    }

    /**
     *
     * @param n
     */
    @Override
    public void setMaxIter(int n) {
	m_estimator.setMaxIter(n);
    }

    @Override
    public double getPrecision() {
        return m_estimator.getParametersRelativeTolerance();
    }

    @Override
    public void setPrecision(double value) {
        m_estimator.setParametersRelativeTolerance(value);
    }
}
