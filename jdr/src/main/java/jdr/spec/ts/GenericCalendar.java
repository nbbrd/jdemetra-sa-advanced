/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.ts;

import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.DayClustering;
import ec.tstoolkit.timeseries.calendars.GenericTradingDays;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class GenericCalendar {
@lombok.experimental.UtilityClass
public class GenericCalendars {

    public Matrix td(TsDomain domain, int[] groups, boolean contrasts) {
        DayClustering dc = DayClustering.create(groups);
        if (contrasts) {
            GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
            Matrix m = new Matrix(domain.getLength(), dc.getGroupsCount() - 1);
            gtd.data(domain, m.columnList());
            return m;
        } else {
            GenericTradingDays gtd = GenericTradingDays.of(dc);
            Matrix m = new Matrix(domain.getLength(), dc.getGroupsCount());
            gtd.data(domain, m.columnList());
            return m;
        }
    }
}
    
}
