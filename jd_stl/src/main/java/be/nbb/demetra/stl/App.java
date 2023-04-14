/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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

import ec.tstoolkit.data.AutoRegressiveSpectrum;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

/**
 *
 * @author Jean Palate
 */
public class App {

    private static Matrix data;
    private static String output;
    private static boolean verbose = false;
    private static boolean mul = false;
    private static Matrix components;
    private static int ni = 1, no = 1;
    private static int[] tspec, sspec1, sspec2, sspec3;
    private static double[] lb1, lb2, lb3;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        if (verbose) {
            System.out.println("Reading data");
        }
        if (!decodeArgs(args)) {
            return;
        }
        for (int col = 0; col < data.getColumnsCount(); ++col) {
            try {
                if (verbose) {
                    System.out.println("Series " + (col + 1));
                }
                if (verbose) {
                    System.out.println("Computing the components");
                }
                computeComponents(col);
                if (verbose) {
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
            cmd = cmd.toLowerCase(Locale.ROOT);

            switch (cmd) {
                case "-y": {
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
                case "-o":
                case "-output": {
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
                case "-t": {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    tspec = decode(str);
                    if (tspec == null || tspec.length != 3) {
                        System.out.println(cmd + " is invalid");
                        return false;
                    }
                    break;
                }
                case "-s1":
                case "-s": {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    sspec1 = decode(str);
                    if (sspec1 == null || sspec1.length != 7) {
                        System.out.println(cmd + " is invalid");
                        return false;
                    }
                    break;
                }
                case "-s2": {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    sspec2 = decode(str);
                    if (sspec2 == null || sspec2.length != 7) {
                        System.out.println(cmd + " is invalid");
                        return false;
                    }
                    break;
                }
                case "-s3": {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    sspec3 = decode(str);
                    if (sspec3 == null || sspec3.length != 7) {
                        System.out.println(cmd + " is invalid");
                        return false;
                    }
                    break;
                }
                case "-lb":
                case "-lb1": {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    lb1 = ddecode(str);
                    if (lb1 == null) {
                        System.out.println(cmd + " is invalid");
                        return false;
                    }
                    break;
                }
                case "-lb2": {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    lb2 = ddecode(str);
                    if (lb2 == null) {
                        System.out.println(cmd + " is invalid");
                        return false;
                    }
                    break;
                }
                case "-lb3": {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    lb3 = ddecode(str);
                    if (lb3 == null) {
                        System.out.println(cmd + " is invalid");
                        return false;
                    }
                    break;
                }
                case "-mul": {
                    mul = true;
                    break;
                }
                case "-ni": {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    ni = Integer.decode(str);
                    break;
                }
                case "-no": {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    ni = Integer.decode(str);
                    break;
                }
                case "-v": {
                    verbose = true;
                    break;
                }
                default:
                    System.out.println(cmd + " is not supported");
                    return false;
            }
        }
        return true;
    }

    private static int[] decode(String s) {
        s = s.trim();
        int n = s.length();
        if (s.charAt(0) != '(' || s.charAt(n - 1) != ')') {
            return null;
        }
        s = s.substring(1, n - 1);
        String[] items = split(s);
        int[] rslt = new int[items.length];
        for (int i = 0; i < rslt.length; ++i) {
            String cur = items[i];
            if (cur.length() == 0) {
                rslt[i] = -1;
            } else {
                rslt[i] = Integer.parseInt(cur);
            }
        }
        return rslt;
    }

    static String[] split(String fullName) {
        int nsep = 0;
        int len = fullName.length();
        for (int i = 0; i < len; ++i) {
            if (fullName.charAt(i) == ',') {
                ++nsep;
            }
        }
        if (nsep == 0) {
            return new String[]{fullName};
        } else {
            String[] s = new String[nsep + 1];
            int pos = 0;
            int end = 0, beg = 0;
            while (pos < nsep) {
                if (fullName.charAt(end) == ',') {
                    s[pos++] = fullName.substring(beg, end);
                    beg = end + 1;
                    end = beg;
                } else {
                    ++end;
                }
            }
            s[pos] = fullName.substring(beg).trim();
            return s;
        }

    }

    private static double[] ddecode(String s) {
        s = s.trim();
        int n = s.length();
        if (s.charAt(0) != '(' || s.charAt(n - 1) != ')') {
            return null;
        }
        s = s.substring(1, n - 1);
        String[] items = split(s);
        if (items.length != 2) {
            return null;
        }
        double[] rslt = new double[2];

        for (int i = 0; i < 2; ++i) {
            rslt[i] = Double.parseDouble(items[i]);
        }
        return rslt;
    }

    private static DataBlock y(int col) {
        if (!mul) {
            return data.column(col);
        } else {
            DataBlock y = data.column(col).deepClone();
            y.apply(x -> Math.log(x));
            return y;
        }
    }

    private static File generateFile(String name, int col) {
        File path = new File(output == null ? "." : output);
        if (!path.exists()) {
            path.mkdirs();
        }
        return new File(path, name + ("-") + (col + 1) + ".txt");
    }

    private static int periodicity() {
        int p = 0;
        if (sspec1 != null && sspec1[0] > p) {
            p = sspec1[0];
        }
        if (sspec2 != null && sspec2[0] > p) {
            p = sspec2[0];
        }
        if (sspec3 != null && sspec3[0] > p) {
            p = sspec3[0];
        }
        return p;
    }

    private static int swindow(int p) {

        if (sspec1 != null && sspec1[0] == p) {
            return sspec1[1];
        }
        if (sspec2 != null && sspec2[0] == p) {
            return sspec2[1];
        }
        if (sspec3 != null && sspec3[0] == p) {
            return sspec3[1];
        }
        return p;
    }

    private static void generateARSpectrum(int col) throws IOException {
        // AR spectrum of the linearized, of the SA, of C, of I
        int nar = (int) (periodicity() * 2.5);
        int n = 3;
        DataBlock y = components.column(0).deepClone();
        DataBlock sa = components.column(1).deepClone();
        DataBlock irr = components.column(components.getColumnsCount() - 1);
        sa.difference();
        sa = sa.drop(1, 0);
        y.difference();
        y = y.drop(1, 0);
        irr = irr.drop(1, 0);
        DataBlock c = null;

        AutoRegressiveSpectrum arylin = new AutoRegressiveSpectrum(AutoRegressiveSpectrum.Method.Durbin);
        AutoRegressiveSpectrum arsa = new AutoRegressiveSpectrum(AutoRegressiveSpectrum.Method.Durbin);
        AutoRegressiveSpectrum arc = new AutoRegressiveSpectrum(AutoRegressiveSpectrum.Method.Durbin);
        AutoRegressiveSpectrum ari = new AutoRegressiveSpectrum(AutoRegressiveSpectrum.Method.Durbin);
        arylin.process(y, nar);
        arsa.process(sa, nar);
        if (c != null) {
            arc.process(c, nar);
        }
        ari.process(irr, nar);
        int nf = 4 * periodicity();
        double rd = Math.PI / (1 + nf);
        Matrix rslt = new Matrix(nf, n + 1);
        double cur = rd;
        for (int i = 1; i <= nf; ++i) {
            int j = 0;
            rslt.set(i - 1, j++, cur);
            rslt.set(i - 1, j++, arylin.value(cur));
            rslt.set(i - 1, j++, arsa.value(cur));
            if (c != null) {
                rslt.set(i - 1, j++, arc.value(cur));
            }
            rslt.set(i - 1, j, ari.value(cur));
            cur += rd;
        }
        File cmp = generateFile("arspectrum", col);
        MatrixSerializer.write(rslt, cmp);
    }

    private static void generateOutput(int col) {
        try {
            // components
            File cmp = generateFile("components", col);
            MatrixSerializer.write(components, cmp);
            generateARSpectrum(col);
            generateLjungBox(col);

        } catch (IOException ex) {
        }
    }

    private static void generateLjungBox(int col) throws IOException {
        if (lb1 != null) {
            generateLjungBox(col, 1, lb1);
        }
        if (lb2 != null) {
            generateLjungBox(col, 2, lb2);
        }
        if (lb3 != null) {
            generateLjungBox(col, 3, lb3);
        }
    }

    private static void generateLjungBox(int col, int test, double[] lb) throws IOException {
        double period = lb[0];
        DataBlock y = components.column(0).deepClone();
        DataBlock sa = components.column(1).deepClone();
        DataBlock irr = components.column(components.getColumnsCount() - 1);
        int nlag = (int) Math.round(lb[1]);
        sa.difference(1.0, nlag);
        sa = sa.drop(nlag, 0);
        y.difference(1.0, nlag);
        y = y.drop(nlag, 0);
        irr = irr.drop(nlag, 0);
        DataBlock c = null;

        File lbs = generateFile("lbs" + test, col);
        // compute seasonal Ljung-Box
        int[] lags = new int[4];
        for (int i = 0; i < lags.length; ++i) {
            lags[i] = (int) (period * (i + 1));

        }
        LjungBoxTest2 stest = new LjungBoxTest2();
        stest.setLags(lags);

        try (BufferedWriter writer = Files.newBufferedWriter(lbs.toPath())) {
            stest.test(y);
            OutputFormatter.writeLb(writer, "y", stest);
            stest.test(sa);
            OutputFormatter.writeLb(writer, "sa", stest);
            stest.test(irr);
            OutputFormatter.writeLb(writer, "irr", stest);
        }
    }

    private static LoessSpecification trend() throws Exception {
        int period = periodicity(), swin = swindow(period);
        if (period == 0 || swin == 0) {
            throw new Exception("Incomplete specification");
        }
        if (tspec == null || tspec[0] <= 0) {
            return LoessSpecification.defaultTrend(period, swin);
        } else {
            if (tspec[1] < 0) {
                tspec[1] = 1;
            }
            if (tspec[2] < 0) {
                return LoessSpecification.of(tspec[0], tspec[1]);
            } else {
                return LoessSpecification.of(tspec[0], tspec[1], tspec[2], null);
            }
        }
    }

    private static SeasonalSpecification seas(int[] sspec) {
        boolean def = true;
        for (int i = 2; i < sspec.length; ++i) {
            if (sspec[i] >= 0) {
                def = false;
                break;
            }
        }
        if (def) {
            return new SeasonalSpecification(sspec[0], sspec[1]);
        }
        // high-pass filter
        if (sspec[2] < 0) {
            sspec[2] = 0;
        }
        LoessSpecification high;
        if (sspec[3] < 0) {
            high = LoessSpecification.of(sspec[1], sspec[2]);
        } else {
            high = LoessSpecification.of(sspec[1], sspec[2], sspec[3], null);
        }
        // low-pass filter
        LoessSpecification low;
        if (sspec[4] < 0) {
            sspec[4]=sspec[0]+1;
        }
        if (sspec[5] < 0) {
            sspec[5]=1;
        }
        if (sspec[6]<0){
            low = LoessSpecification.of(sspec[4], sspec[5]);
        } else {
            low = LoessSpecification.of(sspec[4], sspec[5], sspec[6], null);
        }
        

        return new SeasonalSpecification(sspec[0], high, low);
    }

    private static StlPlusSpecification generateSpec() throws Exception {
        StlPlusSpecification spec = new StlPlusSpecification(false);
        spec.setMultiplicative(mul);
        spec.setNumberOfOuterIterations(no);
        spec.setNumberOfInnerIterations(ni);

        spec.setTrendSpec(trend());
        if (sspec1 != null) {
            spec.add(seas(sspec1));
        }
        if (sspec2 != null) {
            spec.add(seas(sspec2));
        }
        if (sspec3 != null) {
            spec.add(seas(sspec3));
        }
        return spec;
    }

    private static void computeComponents(int col) throws Exception {
        StlPlusSpecification spec = generateSpec();
        StlPlus engine = spec.build();
        engine.process(data.column(col));
        int ns = spec.getSeasonalSpecs().size();
        int nc = ns + 4;
        components = new Matrix(data.getRowsCount(), nc);
        components.column(0).copy(data.column(col));
        components.column(1).copy(data.column(col));
        components.column(2).copyFrom(engine.getTrend(), 0);
        components.column(nc - 1).copyFrom(engine.getIrr(), 0);
        for (int i = 0; i < ns; ++i) {
            components.column(3 + i).copyFrom(engine.getSeason(i), 0);
            if (mul) {
                components.column(1).div(components.column(3 + i));
            } else {
                components.column(1).sub(components.column(3 + i));
            }
        }

    }

}
