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

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.ucarima.UcarimaModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
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
    
    public static void writeUcm(File file, UcarimaModel ucm) throws IOException{
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.append(ucm.toString());
        }
    }

   public static void writeArima (File file, IArimaModel arima) throws IOException{
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.append(arima.toString());
        }
    }

   public static void writeUcmPolynomials(File file, UcarimaModel ucm) throws IOException{
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.ROOT);
        fmt.setGroupingUsed(false);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i=0; i<ucm.getComponentsCount(); ++i){
                writer.append("component "+(i+1));
                writer.newLine();
                ArimaModel arima = ucm.getComponent(i);
                writer.append(fmt.format(arima.getInnovationVariance()));
                writer.newLine();
                writer.append(ReadDataBlock.toString(arima.getStationaryAR().getPolynomial()));
                writer.newLine();
                writer.append(ReadDataBlock.toString(arima.getNonStationaryAR().getPolynomial()));
                writer.newLine();
                writer.append(ReadDataBlock.toString(arima.getMA().getPolynomial()));
                writer.newLine();
            }
        }
    }
}
