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

/**
 *
 * @author Jean Palate
 */
public interface ITrendCycleDecomposer {
    /**
     * Tries to decompose an ARIMA model in low/high-frequency models
     * @param trendcycle The trend-cycle model
     * @return True if the decomposition was successful, false otherwise
     */
    boolean decompose(IArimaModel trendcycle);
    
    /**
     * Retrieves the trend
     * @return 
     */
    ArimaModel getTrend();
    
    /**
     * Retrieves the cycle
     * @return 
     */
    ArimaModel getCycle();
}
