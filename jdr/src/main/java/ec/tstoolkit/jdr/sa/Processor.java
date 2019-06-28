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
        PreprocessingModel regarima = (PreprocessingModel) rslts.get(PREPROCESSING);
        SeatsResults seats = rslts.get(DECOMPOSITION, SeatsResults.class);
        ISeriesDecomposition finals = (ISeriesDecomposition) rslts.get(FINAL);

        SaDiagnostics diags = SaDiagnostics.of(regarima, seats, finals);
        return new TramoSeatsResults(rslts, diags);

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
        PreprocessingModel regarima = (PreprocessingModel) rslts.get(PREPROCESSING);
        X11Results x11 = rslts.get(X13ProcessingFactory.DECOMPOSITION, X11Results.class);
        ISeriesDecomposition finals = (ISeriesDecomposition) rslts.get(FINAL);

        SaDiagnostics diags = SaDiagnostics.of(regarima, x11, finals);
        return new X13Results(rslts, diags);

    }

    private static StatisticalTest qs(TsData s, boolean mul) {
        if (s == null) {
            return null;
        }
        TsData sac = s;
        if (mul) {
            sac = sac.log();
        }
        int ifreq = sac.getFrequency().intValue();
        DifferencingResults dsa = DifferencingResults.create(sac, -1, true);
        return StatisticalTest.create(QSTest.compute(dsa.getDifferenced().internalStorage(), ifreq, 2));

    }

    private static StatisticalTest f(TsData s, boolean mul) {
        if (s == null) {
            return null;
        }
        TsData sac = s;
        if (mul) {
            sac = sac.log();
        }
        int ifreq = sac.getFrequency().intValue();
        TsData salast = sac;
        salast = sac.drop(Math.max(0, sac.getLength() - ifreq * NY - 1), 0);
        return StatisticalTest.create(processAr(salast));
    }

    private static boolean isMultiplicative(CompositeResults rslts) {
        DecompositionMode mul = rslts.getData(ModellingDictionary.MODE, DecompositionMode.class);
        return mul != null && mul.isMultiplicative();
    }

    private static boolean isSignificant(TsData i) {
        if (i == null) {
            return false;
        }
        DescriptiveStatistics idesc = new DescriptiveStatistics(i);
        double se = idesc.getStdev();
        return se > E_LIMIT1;
    }

    private static boolean isSignificant(TsData s, TsData ref, double limit) {
        DescriptiveStatistics sdesc = new DescriptiveStatistics(s);
        DescriptiveStatistics refdesc = new DescriptiveStatistics(ref);
        double se = sdesc.getStdev();
        double refe = refdesc.getRmse();
        return refe == 0 || se / refe > limit;
    }

    private static ec.tstoolkit.stats.StatisticalTest processAr(TsData s) {
        try {
            RegModel reg = new RegModel();
            DataBlock y = new DataBlock(s);
            reg.setY(y.drop(1, 0));
            TsDomain edomain = s.getDomain().drop(1, 0);
            SeasonalDummies dummies = new SeasonalDummies(edomain.getFrequency());
            List<DataBlock> regs = RegressionUtilities.data(dummies, edomain);
            reg.addX(y.drop(0, 1));
            for (DataBlock r : regs) {
                reg.addX(r);
            }
            reg.setMeanCorrection(true);
            int nseas = dummies.getDim();
            //       BackFilter ur = context.description.getArimaComponent().getDifferencingFilter();
            Ols ols = new Ols();
            if (!ols.process(reg)) {
                return null;
            }
            JointRegressionTest test = new JointRegressionTest(.01);
            test.accept(ols.getLikelihood(), 0, 2, nseas, null);
            return test.getTest();
        } catch (Exception err) {
            return null;
        }
    }
}
