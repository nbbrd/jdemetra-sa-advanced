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

import java.time.LocalDate;
import java.time.Month;

/**
 *
 * @author Jean Palate
 */
public class EasterComputer {

    /**
     * Meeus/Jones/Butcher's algorithm for computing Easter The easter and
     * easter2 methods give the same results up to 4150.
     *
     * @param y Year
     * @return The Easter day for the given year
     */
    public static LocalDate easter(int y) {
        int a = y % 19;
        int b = y / 100;
        int c = y % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = (h + l - 7 * m + 114) % 31 + 1;
        return LocalDate.of(y, Month.of(month), day);
    }

    /**
     * Ron Mallen's algorithm for computing Easter. Valid till 4000. The easter
     * and easter2 methods give the same results up to 4150
     *
     * @param y Year
     * @return The Easter day for the given year
     */
    public static LocalDate easter2(int y) {
        int firstdig = y / 100;
        int remain19 = y % 19;
        // calculate PFM date (Paschal Full Moon)
        int temp = (firstdig - 15) / 2 + 202 - 11 * remain19;
        if (firstdig == 21 || firstdig == 24 || firstdig == 25
                || (firstdig >= 27 && firstdig <= 32) || firstdig == 34
                || firstdig == 35 || firstdig == 38) {
            --temp;
        } else if (firstdig == 33 || firstdig == 36 || firstdig == 37
                || firstdig == 39 || firstdig == 40) {
            temp -= 2;
        }
        temp = temp % 30;
        int ta = temp + 21;
        if (temp == 29) {
            --ta;
        }
        if (temp == 28 && remain19 > 10) {
            --ta;
        }
        // find the next sunday
        int tb = (ta - 19) % 7;
        int tc = (40 - firstdig) % 4;
        if (tc == 3) {
            ++tc;
        }
        if (tc > 1) {
            ++tc;
        }
        temp = y % 100;
        int td = (temp + temp / 4) % 7;
        int te = ((20 - tb - tc - td) % 7) + 1;
        int d = ta + te;
        // return the date
        if (d > 31) {
            return LocalDate.of(y, Month.APRIL, d - 31);
        } else {
            return LocalDate.of(y, Month.MARCH, d);
        }
    }

    /**
     * Returns the Julian Easter Meeus algorithm
     *
     * @param year Considered year
     * @param gregorian Gregorian (true) or Julian (false) day
     * @return Easter day
     */
    public static LocalDate julianEaster(int year, boolean gregorian) {
        int a = year % 19;
        int b = year % 4;
        int c = year % 7;
        int d = (19 * a + 15) % 30;
        int e = (2 * b + 4 * c + 6 * d + 6) % 7;
        int z = 4 + d + e;
        int month, day;
        int de = d + e;
        if (de < 10) {
            month = 3;
            day = 22 + de;
        } else {
            month = 4;
            day = de - 9;
        }
        LocalDate easter = LocalDate.of(year, Month.of(month), day);
        if (gregorian) {
            return easter.plusDays(13);
        } else {
            return easter;
        }
    }

    public static LocalDate julianEaster2(int year, boolean gregorian) {
        int g = year % 19;
        int i = (19 * g + 15) % 30;
        int j = (year + year / 4 + i) % 7;
        int l = i - j;
        int month = 3 + (l + 40) / 44;
        int day = l + 28 - 31 * (month / 4);
        LocalDate easter = LocalDate.of(year, Month.of(month), day);
        if (gregorian) {
            return easter.plusDays(13);
        } else {
            return easter;
        }
    }
}
