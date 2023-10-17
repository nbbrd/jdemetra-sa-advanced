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

package ec.demetra.timeseries.simplets;

import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsDataIterator implements java.util.Iterator<TsObservation> {

    private TsDataBlock data;
    private int cur = 0;
    private boolean skip;

    /**
     * 
     * @param TSData
     */
    public TsDataIterator(TsData TSData) {
	data = TsDataBlock.all(TSData);
	skip = true;
    }

    /**
     * 
     * @param block
     */
    public TsDataIterator(TsDataBlock block) {
	data = block;
	skip = false;
    }

    @Override
    public boolean hasNext() {
	if (skip) {
	    for (; cur < data.data.getLength(); ++cur)
		if (Double.isFinite(data.data.get(cur)))
		    return true;
	    return false;
	} else
	    return cur < data.data.getLength();
    }

    /**
     * 
     * @return
     */
    public boolean isSkippingMissings()
    {
	return skip;
    }

    @Override
    public TsObservation next() {
	// hasMoreElements was called before that call...
	double v = data.data.get(cur);
	TsObservation obs = new TsObservation(data.start.plus(cur
		* data.data.getIncrement()), v);
	++cur;
	return obs;
    }

    /**
     *
     */
    public void reset() {
	cur = 0;
    }

    /**
     * 
     * @param val
     */
    public void setSkippingMissings(boolean val) {
	skip = val;
    }
}
