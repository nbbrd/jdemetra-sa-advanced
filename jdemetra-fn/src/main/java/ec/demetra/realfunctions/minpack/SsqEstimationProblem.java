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
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.demetra.realfunctions.FunctionException;
import ec.demetra.realfunctions.ISsqFunction;
import ec.demetra.realfunctions.ISsqFunctionDerivatives;
import ec.demetra.realfunctions.ISsqFunctionInstance;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SsqEstimationProblem implements IEstimationProblem {

    private ISsqFunction m_fn;

    private DataBlock m_p;
    private ISsqFunctionInstance m_ftry;
    private ISsqFunctionDerivatives m_derivatives;

    /**
     * 
     * @param fn
     */
    public SsqEstimationProblem(ISsqFunction fn) {
	this(fn.ssqEvaluate(fn.getDomain().getDefault()));
    }

    /**
     * 
     * @param start
     */
    public SsqEstimationProblem(ISsqFunctionInstance start) {
	m_fn = start.getSsqFunction();
	m_ftry = start;
	IReadDataBlock p = start.getParameters();
	m_p = new DataBlock(p.getLength());
	p.copyTo(m_p.getData(), 0);
    }

    @Override
    public void bound(int idx, boolean bound) {
	throw new FunctionException(
		"The method or operation is not implemented.");
    }

    private void calc() {
	m_fn.getDomain().validate(m_p);
        try{
	m_ftry = m_fn.ssqEvaluate(m_p);
        }
        catch (Exception err){
            m_ftry=null;
        }
    }

    private void clear() {
	m_derivatives = null;
	m_ftry = null;
    }

    @Override
    public double getMeasurementParialDerivative(int midx, int pidx) {
	if (m_ftry == null) {
	    calc();
	}
        if (m_ftry == null)
            return Math.sqrt(Double.MAX_VALUE);
	if (m_derivatives == null) {
	    m_derivatives = m_ftry.getSsqDerivatives();
	}
	return -m_derivatives.dEdX(pidx).get(midx);
    }

    /**
     * 
     * @return
     */
    @Override
    public int getMeasurementsCount() {
	if (m_ftry == null) {
	    calc();
	}
	return m_ftry == null ? 0 :  m_ftry.getDim();
    }

    /**
     * 
     * @param idx
     * @return
     */
    @Override
    public double getMeasurementValue(int idx)
    {
	if (m_ftry == null) {
	    calc();
	}
	return m_ftry == null ? Math.sqrt(Double.MAX_VALUE) : m_ftry.getE().get(idx);
    }

    /**
     * 
     * @param idx
     * @return
     */
    @Override
    public double getMeasurementWheight(int idx) {
	return 1;
    }

    /**
     * 
     * @param idx
     * @return
     */
    @Override
    public double getParameterEstimate(int idx) {
	return m_p.get(idx);
    }

    @Override
    public int getParametersCount() {
	return m_p.getLength();
    }

    /**
     * 
     * @param midx
     * @return
     */
    @Override
    public double getResidual(int midx) {
	if (m_ftry == null) {
	    calc();
	}
	return m_ftry == null ? Math.sqrt(Double.MAX_VALUE) : m_ftry.getE().get(midx);
    }

    /**
     * 
     * @return
     */
    public ISsqFunctionInstance getResult() {
	if (m_ftry == null) {
	    calc();
	}
	return m_ftry;
    }

    /**
     * 
     * @param midx
     * @return
     */
    @Override
    public double getTheoreticalValue(int midx) {
	return 0;
    }

    /**
     * 
     * @param idx
     * @return
     */
    @Override
    public double getUnboundParameterEstimate(int idx) {
	return m_p.get(idx);
    }

    /**
     * 
     * @return
     */
    @Override
    public int getUnboundParametersCount() {
	return m_p.getLength();
    }

    /**
     * 
     * @param idx
     * @param ignore
     */
    @Override
    public void ignoreMeasurement(int idx, boolean ignore) {
    }

    /**
     * 
     * @param idx
     * @return
     */
    @Override
    public boolean isBound(int idx) {
	return false;
    }

    /**
     * 
     * @param idx
     * @return
     */
    @Override
    public boolean isMeasurementIgnore(int idx) {
	return false;
    }

    /**
     * 
     * @param idx
     * @param val
     */
    @Override
    public void setParameterEstimate(int idx, double val) {
	if (m_p.get(idx) != val) {
	    m_p.set(idx, val);
	    clear();
	}
    }

    /**
     * 
     * @param idx
     * @param val
     */
    @Override
    public void setUnboundParameterEstimate(int idx, double val) {
	if (m_p.get(idx) != val) {
	    m_p.set(idx, val);
	    clear();
	}
    }
    
    public IReadDataBlock getGradient(){
        if (this.m_ftry == null)
            calc();
        if (m_ftry == null)
            return null;
        else
            return m_ftry.getSsqDerivatives().getGradient();
    }
}
