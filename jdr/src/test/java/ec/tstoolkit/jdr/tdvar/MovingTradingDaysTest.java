/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.tdvar;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class MovingTradingDaysTest {
    
    public MovingTradingDaysTest() {
    }

    @Test
    public void testSomeMethod() {
        MovingTradingDays.Results rslt = MovingTradingDays.process(data.Data.P, 11, 5, "tramo", "TR5a", true);
        assertTrue(rslt != null);
    }
    
}
