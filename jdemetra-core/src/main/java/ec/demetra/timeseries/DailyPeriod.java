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

import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.design.ImmutableObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public abstract class DailyPeriod implements IDatePeriod{

    public static DailyPeriod of(LocalDate day) {
        return new Day(day);
    }

    @ImmutableObject
    public static DailyPeriod of(LocalDate start, LocalDate end) {
        int cmp = start.compareTo(end);
        if(cmp == 0) {
                return new Day(start);
        }else if (cmp<0){
                return new Days(start, end);
        }else
                throw new IllegalArgumentException("DailyPeriod: start after end");
    }
    
    public abstract int lengthInDays();

    @Immutable
    private static class Day extends DailyPeriod  {

        private final LocalDate day;

        Day(LocalDate day) {
            this.day = day;
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
        
        @Override
        public int lengthInDays(){
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof Day && equals((Day) obj));
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + Objects.hashCode(this.day);
            return hash;
        }

        public boolean equals(Day other) {
            return day.compareTo(other.day) == 0;
        }

        @Override
        public String toString() {
            return day.toString();
        }
    }

    @Immutable
    private static class Days extends DailyPeriod {

        private final LocalDate start, end;

        Days(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean contains(LocalDate d) {
            return !(d.isBefore(start) || d.isAfter(end));
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
            LocalDate d = dt.toLocalDate();
            return contains(d);
        }

        @Override
        public int lengthInDays(){
            return start.until(end).getDays();
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof Days && equals((Days) obj));
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + Objects.hashCode(this.start);
            hash = 79 * hash + Objects.hashCode(this.end);
            return hash;
        }

        public boolean equals(Days other) {
            return start.compareTo(other.start) == 0 && end.compareTo(other.end) == 0;
        }

        @Override
        public String toString() {
            StringBuilder builder=new StringBuilder();
            builder.append(start.toString()).append(('-')).append(end.toString());
            return builder.toString();
        }

    }
}
