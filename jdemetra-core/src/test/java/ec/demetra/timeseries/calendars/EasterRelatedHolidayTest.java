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
package ec.demetra.timeseries.calendars;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class EasterRelatedHolidayTest {
    
    public EasterRelatedHolidayTest() {
    }

    @Test
    public void test1() {
        EasterRelatedHoliday hol=EasterRelatedHoliday.EASTER;
        LocalDate d0=LocalDate.of(2000, Month.MARCH, 20);
        LocalDate d1=LocalDate.of(2015, Month.APRIL, 16);
        Iterable<IHolidayInfo> iterable = hol.getIterable(d0, d1);
//        iterable.forEach(info->System.out.println(info.getDate()));
        assertTrue(StreamSupport.stream(iterable.spliterator(), false).count() == 16);
    }
    
    @Test
    public void test2() {
        EasterRelatedHoliday hol=EasterRelatedHoliday.EASTER;
        LocalDate d0=LocalDate.of(2000, Month.MARCH, 20);
        LocalDate d1=LocalDate.of(2015, Month.APRIL, 5);
        Iterable<IHolidayInfo> iterable = hol.getIterable(d0, d1);
//        iterable.forEach(info->System.out.println(info.getDate()));
        assertTrue(StreamSupport.stream(iterable.spliterator(), false).count() == 15);
    }
    
    @Test
    public void test3() {
        EasterRelatedHoliday hol=EasterRelatedHoliday.EASTER;
        LocalDate d0=LocalDate.of(2015, Month.APRIL, 5);
        LocalDate d1=LocalDate.of(2020, Month.MARCH, 21);
        Iterable<IHolidayInfo> iterable = hol.getIterable(d0, d1);
//        iterable.forEach(info->System.out.println(info.getDate()));
        assertTrue(StreamSupport.stream(iterable.spliterator(), false).count() == 5);
    }
    
    @Test
    public void test4() {
        EasterRelatedHoliday hol=EasterRelatedHoliday.EASTER;
        LocalDate d1=LocalDate.of(2015, Month.APRIL, 5);
        LocalDate d0=LocalDate.of(2020, Month.MARCH, 21);
        Iterable<IHolidayInfo> iterable = hol.getIterable(d0, d1);
//        iterable.forEach(info->System.out.println(info.getDate()));
        assertTrue(StreamSupport.stream(iterable.spliterator(), false).count() == 0);
    }

    @Test
    public void test6() {
        EasterRelatedHoliday hol=EasterRelatedHoliday.ASCENSION;
        LocalDate d0=LocalDate.of(2000, Month.APRIL, 21);
        LocalDate d1=LocalDate.of(2020, Month.JULY, 21);
        Iterable<IHolidayInfo> iterable = hol.getIterable(d0, d1);
        assertTrue(StreamSupport.stream(iterable.spliterator(), false).filter(info->info.getDayOfWeek()==DayOfWeek.THURSDAY).count() == 21);
    }
    
}
