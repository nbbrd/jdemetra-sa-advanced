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

import ec.demetra.timeseries.PeriodSelector;
import ec.tstoolkit.design.Development;

/**
 * Iterator that walk through a time series year by year. Years can be
 * incomplete
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class YearIterator implements java.util.Iterator<TsDataBlock> {
    /**
     * 
     * @param series
     * @return
     */
    public static YearIterator fullYears(TsData series)
    {
	TsDomain domain = series.domain();
	int ifreq = domain.getFrequency().intValue();
	int nbeg = series.getStart().getPosition();
	int nend = series.getEnd().getPosition();
	domain = domain.drop(nbeg == 0 ? 0 : ifreq - nbeg, nend);
	return new YearIterator(series, domain);

    }

    private TsDataBlock cur;
    private final TsDataBlock data;

    /**
     * 
     * @param series
     */
    public YearIterator(TsData series) {
	if (!series.isEmpty())
	    data = TsDataBlock.all(series);
	else
	    data = null;
    }

    /**
     * 
     * @param series
     * @param domain
     */
    public YearIterator(TsData series, TsDomain domain)
    {
	data = TsDataBlock.select(series, domain);
    }

    /**
     * 
     * @param series
     * @param selector
     */
    public YearIterator(TsData series, PeriodSelector selector)
    {
	data = TsDataBlock.select(series, selector);
    }

    @Override
    public boolean hasNext() {
	if (data == null)
	    return false;
	if (cur == null)
	    return true;
	int ifreq = data.start.getFrequency().intValue();
	int ibeg = cur.start.minus(data.start);
	int iend = data.data.getLength() - ifreq - ibeg;
	return iend > 0;
    }

    private void initialize() {
	int ifreq = data.start.getFrequency().intValue();
	int beg = data.start.getPosition();
	cur = new TsDataBlock(data.start.minus(beg), data.data.extract(
		-beg, ifreq, 1));
    }

    @Override
    public TsDataBlock next() {
	int ifreq = data.start.getFrequency().intValue();
	if (cur == null)
	    initialize();
	else
	    cur=cur.slide(ifreq);
	int ibeg = cur.start.minus(data.start);
	int iend = data.data.getLength() - ifreq - ibeg;
	if (ibeg >= 0 && iend >= 0)
	    return cur;
	else
	    return cur.drop(ibeg < 0 ? -ibeg : 0, iend < 0 ? -iend : 0);
    }

    /**
     *
     */
    public void reset() {
	cur = null;
    }
}
