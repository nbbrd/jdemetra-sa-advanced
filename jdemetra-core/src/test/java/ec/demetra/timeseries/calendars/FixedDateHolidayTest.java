/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.timeseries.calendars;

import java.time.LocalDate;
import java.time.Month;
import java.util.stream.StreamSupport;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FixedDateHolidayTest {
    
    public FixedDateHolidayTest() {
    }

    @Test
    public void test1() {
        FixedDateHoliday hol=new FixedDateHoliday(Month.JULY, 21);
        LocalDate d0=LocalDate.of(2000, Month.JULY, 20);
        LocalDate d1=LocalDate.of(2020, Month.JULY, 22);
        Iterable<IHolidayInfo> iterable = hol.getIterable(d0, d1);
        //iterable.forEach(info->System.out.println(info.getDate()));
        assertTrue(StreamSupport.stream(iterable.spliterator(), false).count() == 21);
    }
    
    @Test
    public void test2() {
        FixedDateHoliday hol=new FixedDateHoliday(Month.JULY, 21);
        LocalDate d0=LocalDate.of(2000, Month.JULY, 21);
        LocalDate d1=LocalDate.of(2020, Month.JULY, 22);
        Iterable<IHolidayInfo> iterable = hol.getIterable(d0, d1);
//        iterable.forEach(info->System.out.println(info.getDate()));
        assertTrue(StreamSupport.stream(iterable.spliterator(), false).count() == 21);
    }
    
    @Test
    public void test3() {
        FixedDateHoliday hol=new FixedDateHoliday(Month.JULY, 21);
        LocalDate d0=LocalDate.of(2000, Month.JULY, 21);
        LocalDate d1=LocalDate.of(2020, Month.JULY, 21);
        Iterable<IHolidayInfo> iterable = hol.getIterable(d0, d1);
        assertTrue(StreamSupport.stream(iterable.spliterator(), false).count() == 20);
    }

    @Test
    public void test4() {
        FixedDateHoliday hol=new FixedDateHoliday(Month.JULY, 21);
        LocalDate d0=LocalDate.of(2000, Month.JULY, 21);
        Iterable<IHolidayInfo> iterable = hol.getIterable(d0, d0);
        assertTrue(StreamSupport.stream(iterable.spliterator(), false).count() == 0);
    }

    @Test
    public void test5() {
        FixedDateHoliday hol=new FixedDateHoliday(Month.JULY, 21);
        LocalDate d0=LocalDate.of(2000, Month.OCTOBER, 21);
        LocalDate d1=LocalDate.of(2020, Month.MAY, 21);
        Iterable<IHolidayInfo> iterable = hol.getIterable(d0, d1);
        assertTrue(StreamSupport.stream(iterable.spliterator(), false).count() == 19);
    }

    @Test
    public void test6() {
        FixedDateHoliday hol=new FixedDateHoliday(Month.JULY, 21);
        LocalDate d0=LocalDate.of(2000, Month.MAY, 21);
        LocalDate d1=LocalDate.of(2020, Month.MAY, 21);
        Iterable<IHolidayInfo> iterable = hol.getIterable(d0, d1);
//        iterable.forEach(info->System.out.println(info.getDate()));
        assertTrue(StreamSupport.stream(iterable.spliterator(), false).count() == 20);
    }
}
