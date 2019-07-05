/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.tvtd;

import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.x11.Mstatistics;
import ec.satoolkit.x11.X11Results;
import ec.tstoolkit.modelling.arima.PreprocessingModel;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder
public class TimeVaryingTradingDaysSaResults {
    private PreprocessingModel preprocessing;
    private TimeVaryingTradingDaysCorrection timeVaryingTradingDaysCorrection;
    private X11Results decomposition;
    private Mstatistics mstatistics;
    private DefaultSeriesDecomposition finalDecomposition;
}
