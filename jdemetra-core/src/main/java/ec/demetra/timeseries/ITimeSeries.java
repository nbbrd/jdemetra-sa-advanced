/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.timeseries;

import java.lang.annotation.Documented;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 * @param <T> Time domain
 * @param <D> Data
 */
public interface ITimeSeries <T extends IDomain, D> {
    /**
     * Retrieves the time domain of this time series
     * @return 
     */
    T domain();
    /**
     * Retrieves the content of this time series 
     * @return The content of this time series. 
     */
    D data();
}
