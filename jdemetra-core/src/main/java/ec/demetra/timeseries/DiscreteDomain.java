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
package ec.demetra.timeseries;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
public class DiscreteDomain implements IDomain{
    
    private final LocalDateTime[] dates;

    public static DiscreteDomain create(LocalDateTime[] dates){
        LocalDateTime[] ndates=dates.clone();
        Arrays.parallelSort(ndates);
        return new DiscreteDomain(ndates);
    }
    
    private DiscreteDomain(LocalDateTime[] dates){
        this.dates=dates;
    }

    @Override
    public LocalDateTime getTime(int idx) {
        return dates[idx];
    }

    @Override
    public int getLength() {
        return dates.length;
    }

    @Override
    public int search(LocalDateTime time) {
        return Arrays.binarySearch(dates, time);
    }

    @Override
    public Period getPeriod() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
