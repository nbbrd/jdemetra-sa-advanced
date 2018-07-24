/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.mtd;

import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class MovingTradingDaysSaProcessorTest {

    public MovingTradingDaysSaProcessorTest() {
    }

    @Test
    public void testABS_Tramo() {

        TramoSpecification pspec = TramoSpecification.TR5.clone();
//        RegArimaSpecification pspec = RegArimaSpecification.RG5.clone();
        MovingTradingDaysSpecification mspec = new MovingTradingDaysSpecification();
        mspec.setReestimate(false);
        X11Specification xspec = new X11Specification();
        xspec.setForecastHorizon(-3);
        xspec.setBackcastHorizon(-3);
        MovingTradingDaysSaResults rslt = MovingTradingDaysSaProcessor.process(data.Data.ABS09, pspec, mspec, xspec, null);
        TsDataTable all = new TsDataTable();
        all.add(rslt.getFinalDecomposition().getSeries(ComponentType.Series, ComponentInformation.Value));
        all.add(rslt.getFinalDecomposition().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        all.add(rslt.getFinalDecomposition().getSeries(ComponentType.Trend, ComponentInformation.Value));
        all.add(rslt.getFinalDecomposition().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        all.add(rslt.getFinalDecomposition().getSeries(ComponentType.Irregular, ComponentInformation.Value));
//        System.out.println(all);
        TsDataTable fall = new TsDataTable();
        fall.add(rslt.getFinalDecomposition().getSeries(ComponentType.Series, ComponentInformation.Forecast));
        fall.add(rslt.getFinalDecomposition().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        fall.add(rslt.getFinalDecomposition().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        fall.add(rslt.getFinalDecomposition().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        fall.add(rslt.getFinalDecomposition().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
//        System.out.println(fall);
    }

    @Test
    public void testABS_MTD_RegArima() {

        RegArimaSpecification pspec = RegArimaSpecification.RG5.clone();
        MovingTradingDaysSpecification mspec = new MovingTradingDaysSpecification();
        mspec.setReestimate(false);
        X11Specification xspec = new X11Specification();
        xspec.setForecastHorizon(-3);
        xspec.setBackcastHorizon(-3);
        MovingTradingDaysSaResults rslt = MovingTradingDaysSaProcessor.process(data.Data.ABS09, pspec, mspec, xspec, null);
        TsDataTable all = new TsDataTable();
        all.add(rslt.getFinalDecomposition().getSeries(ComponentType.Series, ComponentInformation.Value));
        all.add(rslt.getFinalDecomposition().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        all.add(rslt.getFinalDecomposition().getSeries(ComponentType.Trend, ComponentInformation.Value));
        all.add(rslt.getFinalDecomposition().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        all.add(rslt.getFinalDecomposition().getSeries(ComponentType.Irregular, ComponentInformation.Value));
//        System.out.println(all);
        TsDataTable fall = new TsDataTable();
        fall.add(rslt.getFinalDecomposition().getSeries(ComponentType.Series, ComponentInformation.Forecast));
        fall.add(rslt.getFinalDecomposition().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        fall.add(rslt.getFinalDecomposition().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        fall.add(rslt.getFinalDecomposition().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        fall.add(rslt.getFinalDecomposition().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
//        System.out.println(fall);
    }

    @Test
    public void testABS_X13() {

        X13Specification spec = X13Specification.RSA5.clone();
        X11Specification xspec = spec.getX11Specification();
        xspec.setForecastHorizon(-3);
        xspec.setBackcastHorizon(-3);
        CompositeResults rslt = X13ProcessingFactory.process(data.Data.ABS09, spec, null);
        TsDataTable all = new TsDataTable();
        
        all.add(rslt.getData("y", TsData.class));
        all.add(rslt.getData("sa", TsData.class));
        all.add(rslt.getData("t", TsData.class));
        all.add(rslt.getData("s", TsData.class));
        all.add(rslt.getData("i", TsData.class));
//        System.out.println(all);
        TsDataTable fall = new TsDataTable();
        fall.add(rslt.getData("y_f", TsData.class));
        fall.add(rslt.getData("sa_f", TsData.class));
        fall.add(rslt.getData("t_f", TsData.class));
        fall.add(rslt.getData("s_f", TsData.class));
        fall.add(rslt.getData("i_f", TsData.class));
//        System.out.println(fall);
    }
}
