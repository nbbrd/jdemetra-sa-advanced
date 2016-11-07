/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.timeseries.calendars;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FixedDateHoliday implements IHoliday {

    private final MonthDay monthDay;

    public FixedDateHoliday(MonthDay monthDay) {
        this.monthDay = monthDay;
    }

    public FixedDateHoliday(Month month, int day) {
        this.monthDay = MonthDay.of(month, day);
    }

    @Override
    public Iterable<IHolidayInfo> getIterable(LocalDate start, LocalDate end) {
        return new FixedDateHolidayIterable(this, start, end);
    }

    public LocalDate calcDate(int year) {
        return LocalDate.of(year, monthDay.getMonth(), monthDay.getDayOfMonth());
    }

    private static class Info implements IHolidayInfo {

        Info(FixedDateHoliday fday, int year) {
            this.fday = fday;
            this.year = year;
        }

        @Override
        public LocalDate getDate() {
            return fday.calcDate(year);
        }

        public void move(int n) {
            year += n;
        }

        private final FixedDateHoliday fday;
        private int year;
    }

    public static final FixedDateHoliday CHRISTMAS = new FixedDateHoliday(Month.DECEMBER, 25),
            NEWYEAR = new FixedDateHoliday(Month.JANUARY, 1),
            EPIPHANY = new FixedDateHoliday(Month.JANUARY, 6),
            ASSUMPTION = new FixedDateHoliday(Month.AUGUST, 15),
            MAYDAY = new FixedDateHoliday(Month.MAY, 1),
            ALLSAINTSDAY = new FixedDateHoliday(Month.NOVEMBER, 1),
            ARMISTICEDAY = new FixedDateHoliday(Month.NOVEMBER, 11),
            HALLOWEEN = new FixedDateHoliday(Month.OCTOBER, 31);

    private static class FixedDateHolidayIterable implements Iterable<IHolidayInfo> {

        FixedDateHolidayIterable(FixedDateHoliday fday, LocalDate fstart, LocalDate fend) {
            this.fday = fday;
            int ystart = fstart.getYear(), yend = fend.getYear();
            LocalDate xday = fday.calcDate(ystart);
            LocalDate yday = fday.calcDate(yend);

            int y0 = fstart.getYear();
            int y1 = fend.getYear();
            // y0 is the last year strictly before the valid time span
            if (!xday.isBefore(fstart)) {
                --y0;
            }

            // y1 is the last valid year
            if (!yday.isBefore(fend)) {
                --y1;
            }
            ybeg = y0;
            n = y1 - y0;
        }

        private final FixedDateHoliday fday;
        private final int ybeg, n;

        @Override
        public Iterator<IHolidayInfo> iterator() {
            return new Iterator<IHolidayInfo>() {
                Info info = new Info(fday, ybeg);
                int cur = -1;

                @Override
                public boolean hasNext() {
                    return cur < n - 1;
                }

                @Override
                public Info next() {
                    info.move(1);
                    ++cur;
                    return info;
                }
            };
        }
    }

    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append(monthDay.getMonth().getDisplayName(TextStyle.SHORT, Locale.ROOT));
        builder.append('-').append(monthDay.getDayOfMonth());
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof FixedDateHoliday && equals((FixedDateHoliday) obj));
    }

    public boolean equals(FixedDateHoliday obj) {
        return monthDay.equals(obj.monthDay);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.monthDay);
        return hash;
    }
}
