/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.mtd;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class MovingTradingDaysEstimatorTest {

    public MovingTradingDaysEstimatorTest() {
    }

    @Test
    public void testABS() {
        PreprocessingModel model = TramoSpecification.TR5.build().process(data.Data.ABS09, null);
        MovingTradingDaysSpecification spec = new MovingTradingDaysSpecification();
        spec.setReestimate(true);
        MovingTradingDaysEstimator mde = new MovingTradingDaysEstimator(spec);
        mde.process(model);
        System.out.println("Automatic");
        System.out.println(mde.getTdCoefficients());
        System.out.println(new DataBlock(mde.getStartTdCoefficients()));
//        System.out.println(mde.getTdEffect());
    }

    @Test
    public void testABSairline() {
        TramoSpecification spec = TramoSpecification.TR5.clone();
        spec.setUsingAutoModel(false);
        spec.getArima().airline();
        PreprocessingModel model = spec.build().process(data.Data.ABS09, null);
        MovingTradingDaysSpecification mspec = new MovingTradingDaysSpecification();
        mspec.setReestimate(true);
        MovingTradingDaysEstimator mde = new MovingTradingDaysEstimator(mspec);
        mde.process(model);
        System.out.println("Airline");
        System.out.println(mde.getTdCoefficients());
        System.out.println(new DataBlock(mde.getStartTdCoefficients()));
//        System.out.println(mde.getTdEffect());
    }
}
