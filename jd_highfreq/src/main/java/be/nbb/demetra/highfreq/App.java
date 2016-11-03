/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package be.nbb.demetra.highfreq;

import be.nbb.demetra.modelling.arima.outliers.OutliersDetectionModule;
import be.nbb.demetra.modelling.outliers.AdditiveOutlier;
import be.nbb.demetra.modelling.outliers.IOutlierVariable;
import be.nbb.demetra.modelling.outliers.SwitchOutlier;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.implementations.arima.SsfUcarima;
import ec.demetra.ssf.univariate.DefaultSmoothingResults;
import ec.demetra.ssf.univariate.SsfData;
import ec.demetra.ucarima.TrendCycleDecomposer;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.estimation.GlsArimaMonitor;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jean Palate
 */
public class App {

    private static final double[] periods = {52, 365.25 / 7, 365, 365.25};

    private static Matrix data, regressors;
    private static int iperiod = 1;
    private static double tlength;
    private static String output;
    private static ArimaModel arima;
    private static UcarimaModel ucm;
    private static UcarimaModel tc_ucm;
    private static boolean silent = false, verbose = false;
    private static boolean ami = false;

    private static IParametricMapping<ArimaModel> mapping;
    private static RegArimaEstimation<ArimaModel> estimation;
    private static Matrix components;

    private static boolean isDaily() {
        return iperiod > 1;
    }

    private static boolean isWeekly() {
        return iperiod <= 1;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        if (!silent) {
            System.out.println("Reading data");
        }
        if (!decodeArgs(args)) {
            return;
        }
        if (!createMapping()) {
            return;
        }
        for (int col = 0; col < data.getColumnsCount(); ++col) {
            try {
                if (!silent) {
                    System.out.println("Series " + (col + 1));
                }
                if (!createModel()) {
                    return;
                }
                if (!silent) {
                    System.out.println("Estimating the model");
                }
                if (!estimateModel(col)) {
                    return;
                }
                if (ami) {
                    if (!silent) {
                        System.out.println("Estimating outliers");
                    }
                    estimateOutliers();
                }

                arima = estimation.model.getArima();
                if (verbose) {
                    System.out.println(arima);
                }
                if (!silent) {
                    System.out.println("Decomposing the model");
                }
                if (!decomposeModel()) {
                    return;
                }
                if (verbose) {
                    System.out.println(ucm);
                }
                if (!silent) {
                    System.out.println("Computing the components");
                }
                computeComponents(col);
                if (tlength > 0) {
                    if (!silent) {
                        System.out.println("Computing trend/cycle");
                    }
                    computeTC();
                }
                if (verbose) {
                    System.out.println(components);
                }
                if (!silent) {
                    System.out.println("Generating output");
                }
                generateOutput(col);

            } catch (Exception err) {
                System.out.println(err.getMessage());
            }
        }
    }

    private static boolean decodeArgs(String[] args) {
        //
        int cur = 0;
        while (cur < args.length) {
            String cmd = args[cur++];
            if (cmd.length() == 0) {
                return false;
            }

            switch (cmd) {
                case "-y":
                case "-Y": {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    try {
                        data = MatrixSerializer.read(new File(str));
                    } catch (IOException ex) {
                        System.out.println("Invalid data");
                        return false;
                    }
                    break;
                }
                case "-x":
                case "-X": {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    try {
                        regressors = MatrixSerializer.read(new File(str));
                    } catch (IOException ex) {
                        System.out.println("Invalid regressors");
                        return false;
                    }
                    break;
                }
                case "-p":
                case "-P": {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    switch (str) {
                        case "w":
                            iperiod = 0;
                            break;
                        case "W":
                            iperiod = 1;
                            break;
                        case "d":
                            iperiod = 2;
                            break;
                        case "D":
                            iperiod = 3;
                            break;
                        default:
                            System.out.println("Invalid period. Should be w, W, d or D");
                    }
                    break;
                }
                case "-t":
                case "-T": {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    try {
                        tlength = Double.parseDouble(str);
                    } catch (Exception e) {
                        return false;
                    }
                    break;
                }
                case "-o":
                case "-O": {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    output = str;
                    break;
                }
                case "-s":
                case "-S": {
                    silent = true;
                    break;
                }
                case "-v":
                case "-V": {
                    verbose = true;
                    break;
                }
                case "-ami":
                case "-AMI": {
                    ami = true;
                    break;
                }
                default:
                    System.out.println(cmd + " is not supported");
                    return false;
            }
        }
        return true;
    }

    private static boolean createModel() {
        int np = isWeekly() ? 2 : 3;
        double[] p0 = new double[np];
        for (int i = 0; i < np; ++i) {
            p0[i] = .9;
        }
        arima = mapping.map(new DataBlock(p0));
        return true;
    }

    private static boolean createMapping() {
        switch (iperiod) {
            case 0: {
                WeeklyMapping wmapping = new WeeklyMapping();
                wmapping.setAdjust(false);
                mapping = wmapping;
                return true;
            }
            case 1: {
                WeeklyMapping wmapping = new WeeklyMapping();
                wmapping.setAdjust(true);
                mapping = wmapping;
                return true;
            }
            case 2: {
                DailyMapping dmapping = new DailyMapping();
                dmapping.setAdjust(false);
                mapping = dmapping;
                return true;
            }
            case 3: {
                DailyMapping dmapping = new DailyMapping();
                dmapping.setAdjust(true);
                mapping = dmapping;
                return true;
            }
            default:
                return false;
        }
    }

    private static boolean estimateModel(int col) {
        GlsArimaMonitor monitor = new GlsArimaMonitor();

        monitor.setMapping(mapping);
        RegArimaModel<ArimaModel> regarima = new RegArimaModel<>(arima, data.column(col));
        if (regressors != null) {
            for (int i = 0; i < regressors.getColumnsCount(); ++i) {
                regarima.addX(regressors.column(i));
            }
        }
        estimation = monitor.process(regarima);
        return estimation != null;
    }

    private static boolean decomposeModel() {
        ModelDecomposer decomposer = new ModelDecomposer();
        TrendCycleSelector tsel = new TrendCycleSelector();
        if (isWeekly()) {
            AllSelector ssel = new AllSelector();
            decomposer.add(tsel);
            decomposer.add(ssel);
        } else {
            SeasonalSelector ssel = new SeasonalSelector(7);
            decomposer.add(tsel);
            decomposer.add(ssel);
            decomposer.add(new AllSelector());
        }
        ucm = decomposer.decompose(arima);
        ucm.setVarianceMax(-1);
        ucm.compact(ucm.getComponentsCount() - 2, 2);
        return ucm.isValid();
    }

    private static void computeComponents(int col) {
        DataBlock mlin = data.column(col);
        if (estimation.likelihood.getB() != null) {
            mlin = estimation.model.calcRes(new ReadDataBlock(estimation.likelihood.getB()));
        }
        SsfUcarima ssf = SsfUcarima.create(ucm);
        DataBlockStorage sr = DkToolkit.fastSmooth(ssf, new SsfData(mlin));//, (pos, a, e)->a.add(0,e));
        int ncmps = (tlength > 0) ? 3 : 2;
        components = new Matrix(mlin.getLength(), ncmps + ucm.getComponentsCount());
        components.column(0).copy(mlin);
        for (int i = 1; i < ucm.getComponentsCount(); ++i) {
            components.column(ncmps + i).copy(sr.item(ssf.getComponentPosition(i)));
        }
        // computes sa, t
        components.column(1).copy(mlin);
        // sa=y-s
        components.column(1).sub(components.column(ncmps + 1));
        if (isDaily()) {
            components.column(1).sub(components.column(ncmps + 2));
        }
        components.column(2).copy(components.column(1));
        components.column(2).sub(components.column(components.getColumnsCount() - 1));

    }

    private static void computeTC() {
        TrendCycleDecomposer tcdecomposer = new TrendCycleDecomposer();
        tcdecomposer.setTau(tlength);
        if (isWeekly()) {
            tcdecomposer.setDifferencing(2);
        } else {
            tcdecomposer.setDifferencing(3);
        }
        tcdecomposer.decompose(ucm.getComponent(0));
        tc_ucm = new UcarimaModel(ucm.getComponent(0), new ArimaModel[]{tcdecomposer.getTrend(), tcdecomposer.getCycle()});
        SsfUcarima ssf = SsfUcarima.create(tc_ucm);
        DefaultSmoothingResults sr = DkToolkit.smooth(ssf, new SsfData(components.column(2)), false);
        components.column(2).copy(sr.getComponent(ssf.getComponentPosition(0)));
        components.column(3).copy(sr.getComponent(ssf.getComponentPosition(1)));
    }

    private static File generateFile(String name, int col) {
        File path = new File(output == null ? "." : output);
        if (!path.exists()) {
            path.mkdirs();
        }
        return new File(path, name + ("-") + (col + 1) + ".txt");
    }

    private static void generateOutput(int col) {
        try {
            File cmp = generateFile("components", col);
            MatrixSerializer.write(components, cmp);
        } catch (IOException ex) {
        }
    }

    private static void estimateOutliers() {
        OutliersDetectionModule outliersDetector = new OutliersDetectionModule();
        if (!silent) {
            Consumer<IOutlierVariable> hook = o -> System.out.println("add outlier:" + o.getCode() + (o.getPosition() + 1));
            outliersDetector.setAddHook(hook);
        }
        GlsArimaMonitor monitor = new GlsArimaMonitor();
        monitor.setMapping(mapping);
        outliersDetector.setMonitor(monitor);
        outliersDetector.addOutlierFactory(new SwitchOutlier.Factory());
        outliersDetector.addOutlierFactory(new AdditiveOutlier.Factory());
        
        outliersDetector.process(estimation.model);
        RegArimaModel<ArimaModel> regarima = outliersDetector.getRegarima();
        estimation = monitor.optimize(regarima);
        arima = regarima.getArima();
    }
}
