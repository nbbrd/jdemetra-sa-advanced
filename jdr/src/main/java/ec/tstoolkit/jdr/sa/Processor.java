/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.sa;

import ec.satoolkit.DecompositionMode;
import static ec.satoolkit.GenericSaProcessingFactory.DECOMPOSITION;
import static ec.satoolkit.GenericSaProcessingFactory.FINAL;
import static ec.satoolkit.GenericSaProcessingFactory.PREPROCESSING;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.diagnostics.QSTest;
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x11.X11Results;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.modelling.DifferencingResults;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.arima.JointRegressionTest;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.regression.RegressionUtilities;
import ec.tstoolkit.timeseries.regression.SeasonalDummies;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.List;
import jdr.spec.ts.Utility;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Processor {

    private static final double E_LIMIT1 = .01, E_LIMIT2 = 0.005;

    public static TramoSeatsResults tramoseats(TsData s, TramoSeatsSpecification spec, Utility.Dictionary dic) {
        ProcessingContext context = null;
        if (dic != null) {
            context = dic.toContext();
        }
        return tramoseatsWithContext(s, spec, context);
    }

    public static TramoSeatsResults tramoseatsWithContext(TsData s, TramoSeatsSpecification spec, ProcessingContext context) {
        s=s.cleanExtremities();
        CompositeResults rslts = TramoSeatsProcessingFactory.process(s, spec, context);
        return new TramoSeatsResults(rslts);

    }

    static final int NY = 8;

    public static X13Results x13(TsData s, X13Specification spec, Utility.Dictionary dic) {
        ProcessingContext context = null;
        if (dic != null) {
            context = dic.toContext();
        }
        return x13WithContext(s, spec, context);
    }

    public static X13Results x13WithContext(TsData s, X13Specification spec, ProcessingContext context) {
        s=s.cleanExtremities();
        CompositeResults rslts = X13ProcessingFactory.process(s, spec, context);
        return new X13Results(rslts);

    }

}
