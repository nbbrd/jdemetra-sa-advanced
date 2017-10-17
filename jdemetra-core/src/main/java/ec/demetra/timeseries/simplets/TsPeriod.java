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
package ec.demetra.timeseries.simplets;

import ec.demetra.timeseries.IDatePeriod;
import ec.demetra.timeseries.TsException;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Immutable;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.Year;
import java.time.temporal.ChronoUnit;

@Development(status = Development.Status.Beta)
@Immutable
public class TsPeriod implements Serializable, IDatePeriod,
        Comparable<TsPeriod> {

    /**
     *
     */
    private static final long serialVersionUID = 7847770315060071968L;

    /**
     * Gets a  description (independent of the year) of the period
     * corresponding to a frequency and to a 0-based position. 
     * For example:
     * January.. for monthly periods
     * Q1... for quarterly periods 
     * @param freq The frequency of the period
     * @param pos Its 0-based position in the year
     * @return The corresponding text
     */
    public static String formatPeriod(TsFrequency freq, int pos) {
        if (freq == TsFrequency.Monthly) {
            return Month.of(pos+1).toString();
        } else if (freq == TsFrequency.Yearly || freq == TsFrequency.Undefined) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            switch (freq) {
                case Quarterly:
                    builder.append('Q');
                    break;
                case HalfYearly:
                    builder.append('H');
                    break;
                default:
                    builder.append('P');
                    break;

            }
            builder.append(pos + 1);
            return builder.toString();
        }
    }

    private final TsFrequency m_freq;
    private final int m_id;


    /**
     * Creates a new TsPeriod, having a given frequency and containing a given
     * date.
     * 
     * @param frequency
     *            Frequency of the period.
     * @param date
     *            A date in the period
     */
    public TsPeriod(final TsFrequency frequency, final LocalDate date) {
        m_freq = frequency;
        m_id=calcId(m_freq.intValue(), date);
    }
    
    /***
     * Creates a period corresponding to the given year
     * @param y Year
     * @return The year 
     */
    public static TsPeriod year(int y){
        return new TsPeriod(TsFrequency.Yearly, y, 0);
    }

    TsPeriod(final TsFrequency frequency, final int id) {
        m_freq = frequency;
        m_id = id;
    }

    /**
     * Creates a new TsPeriod.
     * 
     * @param frequency
     *            Frequency of the period
     * @param year
     *            Year of the period
     * @param position
     *            0-based position of the period in the year
     */
    public TsPeriod(final TsFrequency frequency, final int year,
            final int position) {
        m_freq = frequency;
        int ifreq = m_freq.intValue();
        if ((position < 0) || (position >= ifreq)) {
            throw new TsException(TsException.INVALID_PERIOD);
        }
        m_id = calcId(ifreq, year, position);
    }

    /**
     * Creates a new TsPeriod, having a given frequency and containing a given
     * period.
     * 
     * @param frequency
     *            The frequency of the new object
     * @param p
     *            The period that is contained in the new object.
     */
    public TsPeriod(final TsFrequency frequency, final TsPeriod p) {
        int freq = frequency.intValue(), pfreq = p.m_freq.intValue();
        if (pfreq % freq != 0) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        m_freq = frequency;
        // Attention: integer division is 0-rounding
        int q=p.m_id * freq;
        int id = q / pfreq;
        if (q<0 && q%pfreq != 0)
            --id;
        m_id=id;
    }

    /**
     * Compare to periods. The periods can have different frequencies.
     * 
     * @param other
     *            The compared period.
     * @return 0 is returned if the periods are equal. 1 is returned if the
     *         second period is strictly before the current period. -1 is
     *         returned if the second period is strictly after the current
     *         period.
     */
    @Override
    public int compareTo(final TsPeriod other) {
        if (other.m_freq == m_freq) {
            if (m_id == other.m_id) {
                return 0;
            } else if (m_id < other.m_id) {
                return -1;
            } else {
                return 1;
            }
        }
        throw new TsException(TsException.INCOMPATIBLE_FREQ);
    }

    /**
     * Verifies that a date belongs to the period.
     * 
     * @param dt
     *            Tested date
     * @return true if the given date is inside the period, false otherwise.
     */
    @Override
    public boolean contains(final LocalDate dt) {
        return calcId(m_freq.intValue(), dt) == m_id;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TsPeriod && equals((TsPeriod) obj));
    }
    
    public boolean equals(TsPeriod other) {
        return (m_freq == other.m_freq) && (m_id == other.m_id);
    }

    /**
     * Returns the first day of the period
     * @return The first day
     */
    @Override
    public LocalDate firstDay() {
        int ifreq = m_freq.intValue();
        int c = 12 / ifreq;
        return LocalDate.of(getYear(), 1+getPosition() * c, 1);
    }

    /**
     * Gets the frequency of the period.
     * 
     * @return The frequency.
     */
    public TsFrequency getFrequency() {
        return m_freq;
    }
    
    /**
     * Returns the last higher-frequency period contained in this period 
     * @param freq The higher frequency
     * @return The last period with the given frequency contained in this period
     * @throws TsException if the frequencies are incompatible
     */
    public TsPeriod lastPeriod(TsFrequency freq){
        if (freq == this.m_freq)
            return this;
        int c=freq.ratio(m_freq);
        return new TsPeriod(freq, this.m_id*c+c-1);
    }

    /**
     * Returns the first higher-frequency period contained in this period 
     * @param freq The higher frequency
     * @return The first period with the given frequency contained in this period
     * @throws TsException if the frequencies are incompatible
     */
    public TsPeriod firstPeriod(TsFrequency freq){
        if (freq == this.m_freq)
            return this;
        int c=freq.ratio(m_freq);
        return new TsPeriod(freq, this.m_id*c);
    }

    /**
     * Gets a description (independent of the year) of the period corresponding
     * to a frequency and a position. 
     * @return The description
     * @see #formatShortPeriod(TSFrequency, int).
     */
    public String getPeriodString() {
        return formatPeriod(m_freq, getPosition());
    }

    /**
     * Gets the 0-based position of the period in the year (for instance
     * February has position 1 in a monthly frequency).
     * 
     * @return 0-based position of the period in the year. The returned value is
     *         in the range [0, getFrequency().intValue()[
     */
    public int getPosition() {
        int ifreq = m_freq.intValue();
        if (m_id >= 0) {
            return m_id % ifreq;
        } else {
            return ifreq - 1 + (1 + m_id) % ifreq;
        }
    }

    /**
     * Gets the year the period belongs to.
     * 
     * @return Year of the period.
     */
    public int getYear() {
        if (m_id >= 0) {
            return 1970 + m_id / m_freq.intValue();
        } else {
            return 1969 + (1 + m_id) / m_freq.intValue();
        }
    }

    @Override
    public int hashCode() {
        return m_id;
    }

    /**
     * Number of periods since 1/1/70 (reference period)
     * @return 
     */
    public int id() {
        return m_id;
    }
    
    static int calcId(final int freq, LocalDate date){
        int year=date.getYear();
        int month=date.getMonthValue()-1; // 0 based
        if (freq == 12)
            return calcId(freq, year, month);
        else
            return calcId(freq, year, month*freq/12);
    }
    
    static int calcId(final int freq, final int year, final int placeinyear) {
        return (year - 1970) * freq + placeinyear;
    }

    /**
     * Checks that the period is after a given day.
     * @param day The given day
     * @return true if the start of the period is strictly after the given date,
     *         false otherwise
     */
    public boolean isAfter(final LocalDate day) {
        return firstDay().compareTo(day) > 0;
    }

    /**
     * Verifies that a period is after another one
     * 
     * @param rp
     *            The reference period
     * @return Is equivalent to compareTo(rp) &gt 0;
     * @see #compareTo(TsPeriod)
     */
    public boolean isAfter(final TsPeriod rp) {
        return compareTo(rp) > 0;
    }

    /**
     * Checks that the period is before a given day.
     * 
     * @param day
     *            The reference day.
     * @return true if the end of the period is strictly before the given day,
     *         false otherwise
     */
    public boolean isBefore(final LocalDate day) {
        return lastDay().compareTo(day) < 0;
    }

    /**
     * Verifies that a period is before another one.
     * 
     * @param rp
     *            The reference period
     * @return Is equivalent to compareTo(rp) &lt 0;
     * @see #compareTo(TsPeriod)
     */
    public boolean isBefore(final TsPeriod rp) {
        return compareTo(rp) < 0;
    }

    /**
     * Checks that the current period is inside a given period 
     * 
     * @param p
     *            The containing period.
     * @return true if the current period is inside p, else otherwise
     */
    public boolean isInside(final TsPeriod p) {
        int ifreq = m_freq.intValue(), pfreq = p.m_freq.intValue();
        if (pfreq > ifreq) {
            return false;
        }
        // express in months.
        if (ifreq == pfreq) {
            return m_id == p.m_id;
        }
        int id0 = m_id * 12 / ifreq;
        int id1 = p.m_id * 12 / pfreq;
        if (id0 < id1) {
            return false;
        }
        return id0 + 12 / ifreq <= id1 + 12 / pfreq;
    }

    /**
     * Verifies that a period is not after the second one
     * 
     * @param rp
     *            The reference period
     * @return Is equivalent to compareTo(rp) &lt= 0;
     * @see #compareTo(TsPeriod)
     */
    public boolean isNotAfter(final TsPeriod rp) {
        return compareTo(rp) <= 0;
    }

    /**
     * Verifies that a period is not before the second one
     * 
     * @param rp
     *            The reference period
     * @return Is equivalent to compareTo(rp) &gt= 0;
     * @see #compareTo(TsPeriod)
     */
    public boolean isNotBefore(final TsPeriod rp) {
        // if (lp == null)
        // throw new ArgumentNullException("lp");
        return compareTo(rp) >= 0;
    }

    @Override
    public LocalDate lastDay() {
        int ifreq = m_freq.intValue();
        int c = 12 / ifreq;
        int y = getYear();
        Month month = Month.of(getPosition() * c + c) ;
        return LocalDate.of(y, month, month.length(Year.isLeap(y)));
    }
  
    public Period length(){
        return Period.between(firstDay(), lastDay().plus(1, ChronoUnit.DAYS));
    }

    /**
     * Subtracts a number of periods to the current one. A new object is
     * created. The current object is not modified.
     * 
     * @param nperiods The number of periods
     * @return Returns this - nperiods.
     */
    public TsPeriod minus(int nperiods) {
        return new TsPeriod(m_freq, m_id - nperiods);
    }

    /**
     * Number of periods between two periods with the same frequency.
     * 
     * @return Number of period between the current Object and p. > 0 if the
     *         current Object is after p, = 0 if both objects are equals, < 0 if
     *         the current Object is before p.
     * @param p
     *            The period used in the comparison.
     */
    public int minus(final TsPeriod p) {
        if (m_freq != p.m_freq) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        return m_id - p.m_id;
    }

    /**
     * Adds a number of periods to the current one. A new object is created. The
     * current object is not modified.
     * 
     * @param nperiods
     * @return Returns this + nperiods.
     */
    public TsPeriod plus(int nperiods) {
        return new TsPeriod(m_freq, m_id + nperiods);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(32);
        int p = getPosition();
        int y = getYear();

        int ifreq = m_freq.intValue();

        switch (ifreq) {
            case 2:
            case 3:
            case 4: {
                switch (p) {
                    case 0:
                        buffer.append('I');
                        break;
                    case 1:
                        buffer.append("II");
                        break;
                    case 2:
                        buffer.append("III");
                        break;
                    case 3:
                        buffer.append("IV");
                        break;
                }
                buffer.append('-');
                break;
            }
            case 6:
            case 12:
                buffer.append(1 + p).append('-');
                break;

        }
        buffer.append(y);
        return buffer.toString();
    }
    
}
