/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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
package be.nbb.demetra.highfreq;

import ec.demetra.timeseries.calendars.EasterRelatedHoliday;
import ec.demetra.timeseries.calendars.FixedDateHoliday;
import ec.demetra.timeseries.calendars.IHoliday;
import ec.demetra.timeseries.calendars.IHolidayInfo;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class Holidays {

    protected final List<IHoliday> holidays = new ArrayList<>();

    public static Holidays france() {
        Holidays holidays = new Holidays();
        holidays.addDefault();
        holidays.add(new FixedDateHoliday(Month.MAY, 8));
        holidays.add(new FixedDateHoliday(Month.JULY, 14));
        holidays.add(FixedDateHoliday.ARMISTICEDAY);
        return holidays;
    }

    public static Holidays belgium() {
        Holidays holidays = new Holidays();
        holidays.addDefault();
        holidays.add(new FixedDateHoliday(Month.JULY, 21));
        holidays.add(FixedDateHoliday.ARMISTICEDAY);
        return holidays;
    }

    public Holidays() {
    }

    private void addDefault() {
        holidays.add(FixedDateHoliday.NEWYEAR);
        holidays.add(FixedDateHoliday.MAYDAY);
        holidays.add(FixedDateHoliday.ASSUMPTION);
        holidays.add(FixedDateHoliday.ALLSAINTSDAY);
        holidays.add(FixedDateHoliday.CHRISTMAS);
        holidays.add(EasterRelatedHoliday.EASTERMONDAY);
        holidays.add(EasterRelatedHoliday.ASCENSION);
        holidays.add(EasterRelatedHoliday.WHITMONDAY);
    }

    public void add(IHoliday hol) {
        holidays.add(hol);
    }

    public void fillDays(final SubMatrix D, final LocalDate start, int n) {
        LocalDate end = start.plusDays(n);
        int col = 0;
        for (IHoliday item : holidays) {
            Iterator<IHolidayInfo> iter = item.getIterable(start, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = iter.next().getDate();
                if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    long pos = start.until(date, DAYS);
                    D.set((int) pos, col, 1);
                }
            }
            if (D.getColumnsCount() > 1) {
                ++col;
            }
        }
    }

    public void fillPreviousWorkingDays(final SubMatrix D, final LocalDate start, int n, final int del) {
        LocalDate nstart = start.plusDays(del);
        LocalDate end = start.plusDays(n);
        int col = 0;
        for (IHoliday item : holidays) {
            Iterator<IHolidayInfo> iter = item.getIterable(nstart, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = iter.next().getDate().minusDays(del);
                date = IHolidayInfo.getPreviousWorkingDate(date);
                long pos = start.until(date, DAYS);
                if (pos >= 0 && pos < n) {
                    D.set((int) pos, col, 1);
                }
            }
            if (D.getColumnsCount() > 1) {
                ++col;
            }
        }
    }

    public void fillNextWorkingDays(final SubMatrix D, final LocalDate start, int n, final int del) {
        LocalDate nstart = start.minusDays(del);
        LocalDate end = nstart.plusDays(n);
        int col = 0;
        for (IHoliday item : holidays) {
            Iterator<IHolidayInfo> iter = item.getIterable(nstart, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = iter.next().getDate().plusDays(del);
                date = IHolidayInfo.getNextWorkingDate(date);
                long pos = start.until(date, DAYS);
                if (pos >= 0 && pos < n) {
                    D.set((int) pos, col, 1);
                }
            }
            if (D.getColumnsCount() > 1) {
                ++col;
            }
        }
    }

    /**
     * @return the holidays
     */
    public List<IHoliday> getHolidays() {
        return Collections.unmodifiableList(holidays);
    }
}
