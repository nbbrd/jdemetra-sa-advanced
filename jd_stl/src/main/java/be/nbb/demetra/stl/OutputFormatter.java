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
package be.nbb.demetra.stl;

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * @author Jean Palate
 */
public class OutputFormatter {

    public static void write(File file, String[] items, DataBlock coeff, DataBlock tstats) throws IOException {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.ROOT);
        fmt.setGroupingUsed(false);
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            for (int i = 0; i < items.length; ++i) {
                writer.append(items[i]);
                writer.append('\t');
                writer.append(fmt.format(coeff.get(i)));
                writer.append('\t');
                writer.append(fmt.format(tstats.get(i)));
                writer.newLine();
            }
        }
    }

    public static void writeArima(File file, IArimaModel arima) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            writer.append(arima.toString());
        }
    }

    public static void writeEstimation(File file, RegArimaEstimation<ArimaModel> estimation, IParametricMapping<ArimaModel> mapping, boolean log) throws IOException {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.ROOT);
        fmt.setMinimumFractionDigits(6);
        fmt.setGroupingUsed(false);
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            IReadDataBlock p = mapping.map(estimation.model.getArima());
            writer.append("Parameters");
            writer.newLine();
            LikelihoodStatistics stats = estimation.statistics(p.getLength(), log ? -estimation.model.getY().sum() : 0);
            writer.append(p.toString());
            writer.newLine();
            writer.append(stats.toString());
            writer.newLine();
            writer.append("SER :");
            writer.append(fmt.format(estimation.likelihood.getSer()));
        }
    }

    public static void writeLb(BufferedWriter writer, String title, LjungBoxTest2 lb) throws IOException {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.ROOT);
        fmt.setGroupingUsed(false);
        writer.append(title);
        writer.newLine();
        writer.append(fmt.format(lb.getValue()));
        writer.append('\t');
        writer.append(fmt.format(lb.getPValue()));
        writer.newLine();
        writer.append(lb.getAutoCorrelations().toString());
        writer.newLine();
    }

}
