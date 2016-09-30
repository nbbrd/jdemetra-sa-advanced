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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author Jean Palate
 */
public abstract class DailyPeriod {
    
    public static IDatePeriod of(LocalDate day){
        return new Day(day);
    }
    
    public static IDatePeriod of(LocalDate start, LocalDate end){
        int cmp=start.compareTo(end);
        switch (cmp){
            case 0:return new Day(start);
            case -1:return new Days(start, end);
            default: return null;
        }
    }

    private static class Day extends DailyPeriod implements IDatePeriod{
        private final LocalDate day;
        
        Day(LocalDate day){
            this.day=day;
        }

        @Override
        public boolean contains(LocalDate dt) {
            return day.equals(dt);
        }

        @Override
        public LocalDate firstDay() {
            return day;
       }

        @Override
        public LocalDate lastDay() {
            return day;
        }

        @Override
        public boolean contains(LocalDateTime dt) {
            return dt.toLocalDate().equals(day);
        }
    }

    private static class Days implements IDatePeriod{
        private final LocalDate start, end;
        
        Days(LocalDate start, LocalDate end){
            this.start=start;
            this.end=end;
        }

        @Override
        public boolean contains(LocalDate d) {
            return ! (d.isBefore(start) || d.isAfter(end));
        }

        @Override
        public LocalDate firstDay() {
            return start;
       }

        @Override
        public LocalDate lastDay() {
            return end;
        }

        @Override
        public boolean contains(LocalDateTime dt) {
            LocalDate d=dt.toLocalDate();
            return contains(d);
        }
    }
}
