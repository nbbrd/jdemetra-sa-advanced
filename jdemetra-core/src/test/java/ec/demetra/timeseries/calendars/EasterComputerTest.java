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

import ec.demetra.timeseries.calendars.EasterComputer;
import ec.tstoolkit.timeseries.Day;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import static java.time.temporal.ChronoUnit.DAYS;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class EasterComputerTest {
    
    public EasterComputerTest() {
    }

    @Test
    @Ignore
    public void demoEaster() {
        int[] count = new int[35];
        for (int i = 0; i < 5700000; ++i) {
            LocalDate easter = EasterComputer.easter2(1900 + i);
            LocalDate lbound = LocalDate.of(1900 + i, Month.MARCH, 22);
            count[(int)lbound.until(easter, DAYS)]++;
        }

        for (int i = 0; i < 35; ++i) {
            System.out.println(count[i] / 5700000.0);
        }
    }

    @Test
    public void testEaster2() {
        for (int i = 1900; i < 4100; ++i) {
            LocalDate easter = EasterComputer.easter(i);
            LocalDate easter2 = EasterComputer.easter2(i);
//            System.out.print(easter);
//            System.out.print("  ");
//            System.out.println(easter2);
            Assert.assertEquals(easter, easter2);
        }
    }

    @Test
    public void testJulianEaster() {
        Assert.assertEquals(EasterComputer.julianEaster(2008, true), LocalDate.of(2008, Month.APRIL, 27));
        Assert.assertEquals(EasterComputer.julianEaster(2009, true), LocalDate.of(2009, Month.APRIL, 19));
        Assert.assertEquals(EasterComputer.julianEaster(2010, true), LocalDate.of(2010, Month.APRIL, 4));
        Assert.assertEquals(EasterComputer.julianEaster(2011, true), LocalDate.of(2011, Month.APRIL, 24));
        Assert.assertEquals(EasterComputer.julianEaster2(2008, true), LocalDate.of(2008, Month.APRIL, 27));
        Assert.assertEquals(EasterComputer.julianEaster2(2009, true), LocalDate.of(2009, Month.APRIL, 19));
        Assert.assertEquals(EasterComputer.julianEaster2(2010, true), LocalDate.of(2010, Month.APRIL, 4));
        Assert.assertEquals(EasterComputer.julianEaster2(2011, true), LocalDate.of(2011, Month.APRIL, 24));
    }

    @Test
    public void testJulianEaster2() {
        for (int i=2000; i<2100; ++i){
            assertTrue(EasterComputer.julianEaster(i, true).getDayOfWeek()==DayOfWeek.SUNDAY);
        }
    }
    
    @Test
    public void testOld(){
        for (int i=2000; i<2100; ++i){
            LocalDate easter = EasterComputer.easter(i);
            Day deaster = ec.tstoolkit.timeseries.calendars.Utilities.easter(i);
            assertTrue(easter.toEpochDay() == deaster.getId());
        }
        
    }
    
}
