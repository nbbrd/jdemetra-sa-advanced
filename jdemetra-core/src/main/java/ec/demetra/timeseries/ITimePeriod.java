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
package ec.demetra.timeseries;

import ec.tstoolkit.design.Development;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * interface that defines a time period. The atomic time unit for a IPeriod is the day. 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ITimePeriod {

    /**
     * Checks that the period contains a given date
     * 
     * @param dt
     *            The considered date.
     * @return true if the date is inside the period, false otherwise.
     */
    boolean contains(LocalDateTime dt);

    /**
     * Gets the start of the period (included)
     * @return The start of the period.
     */
    LocalDateTime start();

    /**
     * Gets the end of the period (excluded)
     * 
     * @return The end of the period (excluded)
     */
    LocalDateTime end();

}
