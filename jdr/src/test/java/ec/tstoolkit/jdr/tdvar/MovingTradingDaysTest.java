/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.tdvar;

import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import static junit.framework.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class MovingTradingDaysTest {

    public MovingTradingDaysTest() {
    }

    @Test
    public void testMovingMethod() {
        MovingTradingDays.Results rslt = MovingTradingDays.movingWindow(data.Data.P, 11, 5, "tramo", "TR5a", true);
        assertTrue(rslt != null);
        MovingTradingDays.Results2 rslt2 = MovingTradingDays.timeVarying(data.Data.P, false, "tramo", "TR5a");
        assertTrue(rslt2 != null);
        TsDataTable table = new TsDataTable();
        table.add(rslt.getData("mtd.tde", TsData.class));
        table.add(rslt2.getData("tvtd.tde", TsData.class));
//        System.out.println(table);
    }
}
