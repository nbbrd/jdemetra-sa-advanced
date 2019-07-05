/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.mtd;

import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author palatej
 */
@lombok.Value
public class MovingTradingDaysCorrection {

    private Matrix rawCoefficients, smoothedCoefficients;
    private TsData tdEffect, fullTdEffect, partialLinearizedSeries;

}
