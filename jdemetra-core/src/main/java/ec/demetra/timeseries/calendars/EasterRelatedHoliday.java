/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.timeseries.calendars;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class EasterRelatedHoliday implements IHoliday {

    public static final EasterRelatedHoliday SHROVEMONDAY = EasterRelatedHoliday.of(-48),
            SHROVETUESDAY = EasterRelatedHoliday.of(-47),
            ASHWEDNESDAY = EasterRelatedHoliday.of(-46),
            EASTER = EasterRelatedHoliday.of(0),
            EASTERMONDAY = EasterRelatedHoliday.of(1),
            EASTERFRIDAY = EasterRelatedHoliday.of(-2),
            EASTERTHURSDAY = EasterRelatedHoliday.of(-3),
            ASCENSION = EasterRelatedHoliday.of(39),
            PENTECOST = EasterRelatedHoliday.of(49),
            WHITMONDAY = EasterRelatedHoliday.of(50),
            CORPUSCHRISTI = EasterRelatedHoliday.of(60),
            JULIANSHROVEMONDAY = EasterRelatedHoliday.ofJulian(-48),
            JULIANSHROVETUESDAY = EasterRelatedHoliday.ofJulian(-47),
            JULIANASHWEDNESDAY = EasterRelatedHoliday.ofJulian(-46),
            JULIANEASTER = EasterRelatedHoliday.ofJulian(0),
            JULIANEASTERMONDAY = EasterRelatedHoliday.ofJulian(1),
            JULIANEASTERFRIDAY = EasterRelatedHoliday.ofJulian(-2),
            JULIANEASTERTHURSDAY = EasterRelatedHoliday.ofJulian(-3),
            JULIANASCENSION = EasterRelatedHoliday.ofJulian(39),
            JULIANPENTECOST = EasterRelatedHoliday.ofJulian(49),
            JULIANWHITMONDAY = EasterRelatedHoliday.ofJulian(50),
            JULIANCORPUSCHRISTI = EasterRelatedHoliday.ofJulian(60);

    /*
     * Raw estimation of the probability to get Easter at a specific date is defined below:
     * 22/3 (1/7)*1/LUNARY
     * 23/3 (2/7)*1/LUNARY
     * ...
     * 27/3 (6/7)*1/LUNARY
     * 28/3 1/LUNARY
     * ...
     * 18/4 1/LUNARY
     * 19/4 1/LUNARY + (1/7) * DEC_LUNARY/LUNARY = (7 + 1 * DEC_LUNARY)/(7 * LUNARY)
     * 20/4 (6/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY= (6 + 1 * DEC_LUNARY)/(7 * LUNARY)
     * 21/4 (5/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 22/4 (4/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 23/4 (3/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 24/4 (2/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 25/4 (1/7)*1/LUNARY + (1/7) *DEC_LUNARY/LUNARY
     */
    private static final Map<Integer, LocalDate> DIC = new HashMap<>();
    private static final Map<Integer, LocalDate> JDIC = new HashMap<>();
    private final int offset;
    private final boolean julian;

    public static EasterRelatedHoliday of(int offset) {
        return new EasterRelatedHoliday(offset, false);
    }

    public static EasterRelatedHoliday ofJulian(int offset) {
        return new EasterRelatedHoliday(offset, true);
    }

    private EasterRelatedHoliday(int offset, boolean julian) {
        this.offset = offset;
        this.julian = julian;
    }

    public LocalDate calcDate(int year) {
        LocalDate d = easter(year);
        if (offset != 0) {
            d = d.plusDays(offset);
        }
        return d;
    }

    private LocalDate easter(int year) {
        return easter(year, julian);
    }

    private static LocalDate easter(int year, boolean jul) {
        if (jul) {
            synchronized (JDIC) {
                LocalDate e = JDIC.get(year);
                if (e == null) {
                    e = EasterComputer.julianEaster(year, true);
                    JDIC.put(year, e);
                }
                return e;
            }
        } else {
            synchronized (DIC) {
                LocalDate e = DIC.get(year);
                if (e == null) {
                    e = EasterComputer.easter(year);
                    DIC.put(year, e);
                }
                return e;
            }
        }
    }

    @Override
    public Iterable<IHolidayInfo> getIterable(LocalDate start, LocalDate end) {
        return new EasterDateList(start, end);
    }

    private static class Info implements IHolidayInfo {

        Info(EasterRelatedHoliday fday, int year) {
            this.fday = fday;
            this.year = year;
        }

        @Override
        public LocalDate getDate() {
            return fday.calcDate(year);
        }

        @Override
        public DayOfWeek getDayOfWeek() {
            int val = fday.offset % 7;
            if (val == 0) {
                val = 7;
            }
            return DayOfWeek.of(val);
        }

        public void move(int n) {
            year += n;
        }

        private final EasterRelatedHoliday fday;
        private int year;
    }

    class EasterDateList extends AbstractList<IHolidayInfo> {

        public EasterDateList(LocalDate fstart, LocalDate fend) {
            int y0 = fstart.getYear(), y1 = fend.getYear();
            LocalDate xday = easter(y0, julian).plusDays(offset);
            LocalDate yday = easter(y1, julian).plusDays(offset);

            if (xday.isBefore(fstart)) {
                ++y0;
            }

            // pstart is the last valid period
            if (yday.isBefore(fend)) {
                ++y1;
            }

            if (y1 < y0) {
                n = 0;
            } else {
                n = y1 - y0;
            }
            this.ystart = y0;
        }

        private final int ystart, n;

        @Override
        public IHolidayInfo get(int index) {
            return new Info(EasterRelatedHoliday.this, ystart + index);
        }

        @Override
        public int size() {
            return n;
        }
    }

}
