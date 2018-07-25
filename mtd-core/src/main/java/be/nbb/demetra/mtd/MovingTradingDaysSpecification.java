/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.mtd;

import ec.satoolkit.x11.AsymmetricEndPoints;
import ec.satoolkit.x11.SeasonalFilterFactory;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;

/**
 *
 * @author palatej
 */
@lombok.Data
public class MovingTradingDaysSpecification  {

    private int windowLength = 11;
    private SymmetricFilter smoother = SeasonalFilterFactory.S3X3;
    @lombok.Setter(lombok.AccessLevel.NONE)
    private AsymmetricEndPoints endPoints = SeasonalFilterFactory.endPoints(2);
    private boolean reestimate = false;

    public void setWindowLength(int len) {
        if (len % 2 != 1) {
            throw new IllegalArgumentException("Should be odd");
        }
        this.windowLength = len;
    }

    public void setSmoother(SymmetricFilter sfilter) {
        this.smoother = sfilter;
        this.endPoints = null;
    }

    public void setSmoother(SymmetricFilter sfilter, AsymmetricEndPoints endPoints) {
        if (endPoints.getEndPointsCount() != sfilter.getLength() / 2) {
            throw new IllegalArgumentException();
        }
        this.smoother = sfilter;
        this.endPoints = endPoints;
    }

}
