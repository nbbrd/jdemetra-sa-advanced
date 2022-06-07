/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.timeseries;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DailyPeriodTest {
    
    public DailyPeriodTest() {
    }

    @Test
    public void testDay() {
        LocalDate ld0=LocalDate.now();
        DailyPeriod d0 = DailyPeriod.of(ld0);
        System.out.println(d0.toString());
        LocalDate ld1=LocalDate.now();
        DailyPeriod d1 = DailyPeriod.of(ld1);
        assertTrue(d0.equals(d1));
        assertTrue(d0.hashCode() == d1.hashCode());
        assertTrue(d0.toString().equals(d1.toString()));
        assertTrue(d0.firstDay().equals(d0.lastDay()));
    }
    
    @Test
    public void testDays() {
        LocalDate ld0=LocalDate.now();
        LocalDate ld1=LocalDate.now().plusDays(10);
        DailyPeriod d0 = DailyPeriod.of(ld0, ld1);
        System.out.println(d0.toString());
        IDatePeriod d1 = DailyPeriod.of(ld0, ld0.plusDays(10));
        assertTrue(d0.equals(d1));
        assertTrue(d0.hashCode() == d1.hashCode());
        assertTrue(d0.toString().equals(d1.toString()));
        assertTrue(d0.firstDay().until(d0.lastDay()).getDays()==10);
    }
}
