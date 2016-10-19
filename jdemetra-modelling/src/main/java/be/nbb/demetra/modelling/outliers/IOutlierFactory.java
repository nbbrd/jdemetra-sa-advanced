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
package be.nbb.demetra.modelling.outliers;

/**
 *
 * @author Jean Palate
 */
public interface IOutlierFactory {

    /**
     * Creates an outlier at the given position
     *
     * @param position The position of the outlier.
     * @return A new variable is returned.
     */
    IOutlierVariable create(int position);

    /**
     * Some outliers cannot be identified at the beginning of a series. This method returns the number of such periods
     * @return A positive or zero integer
     */
    int excludingZoneAtStart();

    /**
     * Some outliers cannot be identified at the end of a series. This method returns the number of such periods
     * @return A positive or zero integer
     */
    int excludingZoneAtEnd();
    /**
     * The code that represents the outlier
     * @return 
     */
    String getOutlierCode();
}
