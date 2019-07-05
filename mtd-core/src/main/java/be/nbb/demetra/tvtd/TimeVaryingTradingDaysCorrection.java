/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.tvtd;

import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder
public class TimeVaryingTradingDaysCorrection {
    
    public static TimeVaryingTradingDaysCorrection of(TimeVaryingTradingDaysEstimator src){
        return builder()
                .aic(src.getAic())
                .aic0(src.getAic0())
                .arima(src.getArima())
                .arima0(src.getArima0())
                .tdCoefficients(src.getTdCoefficients())
                .stdeTdCoefficients(src.getStdeTdCoefficients())
                .tdEffect(src.getTdEffect())
                .partialLinearizedSeries(src.getPartialLinearizedSeries())
                .fullTdEffect(src.getFullTdEffect())
                .build();
    }

    private Matrix tdCoefficients, stdeTdCoefficients;
    private TsData tdEffect, fullTdEffect, partialLinearizedSeries;
    private SarimaModel arima0, arima;
    private double aic0, aic;

}
