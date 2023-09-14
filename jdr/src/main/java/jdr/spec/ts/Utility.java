/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.ts;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Jean Palate <jean.palate@nbb.be>
 */
public final class Utility {

    private Utility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Day of(String date) {
        try {
            return Day.fromString(date);
        } catch (ParseException ex) {
            throw new RuntimeException("Unvalid date format");
        }
    }

    public static String toString(Date date) {
        return new Day(date).toString();
//        StringBuilder builder = new StringBuilder();
//        GregorianCalendar gc = new GregorianCalendar();
//        gc.setTime(date);
//        builder.append(gc.get(GregorianCalendar.YEAR)).append('-')
//                .append(gc.get(GregorianCalendar.MONTH)+1).append('-')
//                .append(gc.get(GregorianCalendar.DAY_OF_MONTH));
//        return builder.toString();
    }

    public static String toString(Day day) {
        return day == null || day == Day.BEG || day == Day.END ? "" : day.toString();
    }

    public static Parameter[] parameters(double[] values) {
        return parameters(values, null);
    }

    public static Parameter[] parameters(double[] values, boolean[] fixed) {
        Parameter[] p = new Parameter[values.length];
        for (int i = 0; i < p.length; ++i) {
            if (Double.isFinite(values[i])) {
                if (fixed != null && fixed[i]) {
                    p[i] = new Parameter(values[i], ParameterType.Fixed);
                } else {
                    p[i] = new Parameter(values[i], ParameterType.Initial);
                }
            } else {
                p[i] = new Parameter();
            }
        }
        return p;
    }

    public static String outlierName(String code, String date, int frequency) {
        StringBuilder builder = new StringBuilder();
        builder.append(code).append(" (");
        if (frequency == 0) {
            builder.append(date);
        } else {
            TsPeriod p = new TsPeriod(TsFrequency.valueOf(frequency), of(date));
            builder.append(p);
        }
        return builder.append(')').toString();
    }

    @lombok.Value
    public static final class Outlier {

        private final String code;
        private final String position;
        private final double coefficient;
    }

    @lombok.Value
    public static final class Ramp {

        private final String start, end;
        private final double coefficient;
    }

    @lombok.Value
    public static final class UserDefinedVariable {

        private final String name;
        private final String component;
        private final double coefficient;
    }

    public static final String R = "r", RPREFIX = "r@";

    public static class Dictionary {

        private final Map<String, TsData> dictionary = new LinkedHashMap<>();

        public void add(String name, TsData s) {
            dictionary.put(name, s);
        }

        public String[] names() {
            return dictionary.keySet().toArray(new String[dictionary.size()]);
        }

        public TsData get(String name) {
            return dictionary.get(name);
        }

        public ProcessingContext toContext() {
            ProcessingContext context = new ProcessingContext();
            if (!dictionary.isEmpty()) {
                TsVariables vars = new TsVariables();
                dictionary.forEach((n, s) -> vars.set(n, new TsVariable(n, s)));
                context.getTsVariableManagers().set(R, vars);
            }
            return context;
        }

        public static Dictionary fromContext(ProcessingContext context) {
            Dictionary dic = new Dictionary();
            if (context == null) {
                return dic;
            }
            String[] vars = context.getTsVariableManagers().getNames();
            for (int i = 0; i < vars.length; ++i) {
                TsVariables cur = context.getTsVariables(vars[i]);
                String[] names = cur.getNames();
                for (String name : names) {
                    TsVariable v = (TsVariable) cur.get(name);
                    TsData d = v.getTsData();
                    if (d != null) {
                        if (vars[i].equals(R)) {
                            dic.add(name, d);
                        } else {
                            StringBuilder lname = new StringBuilder();
                            lname.append(vars[i]).append('@').append(name);
                            dic.add(lname.toString(), d);
                        }
                    }
                }
            }
            return dic;
        }
    }
}
