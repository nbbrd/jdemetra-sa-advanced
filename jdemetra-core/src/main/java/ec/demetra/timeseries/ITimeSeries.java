/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.timeseries;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 * @param <T>
 * @param <D>
 */
public interface ITimeSeries <T extends IDomain, D> {
    T domain();
    D data();
}
