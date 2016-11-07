/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.timeseries.calendars;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IHolidayInfo {
    LocalDate getDate();
    
    /**
     * Returns the date equal or before the given date
     * @param date
     * @return 
     */
    static LocalDate getPreviousWorkingDate(LocalDate date){
        DayOfWeek dw=date.getDayOfWeek();
        if (dw== DayOfWeek.SUNDAY)
            return date.minusDays(1);
        else
            return date;
    }
    
    /**
     * Returns the date equal or after the given date
     * @param date
     * @return 
     */
    static LocalDate getNextWorkingDate(LocalDate date){
        DayOfWeek dw=date.getDayOfWeek();
        if (dw== DayOfWeek.SUNDAY)
            return date.plusDays(1);
        else
            return date;
    }

    default DayOfWeek getDayOfWeek(){
        return getDate().getDayOfWeek();
    }
}
