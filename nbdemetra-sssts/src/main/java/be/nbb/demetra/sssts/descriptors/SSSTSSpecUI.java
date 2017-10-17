/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package be.nbb.demetra.sssts.descriptors;

import be.nbb.demetra.sssts.PreprocessingSpecification;
import be.nbb.demetra.sssts.SSSTSSpecification;
import ec.tstoolkit.descriptors.EnhancedPropertyDescriptor;
import ec.tstoolkit.descriptors.IObjectDescriptor;
import ec.tstoolkit.modelling.arima.Method;
import ec.ui.descriptors.benchmarking.SaBenchmarkingSpecUI;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class SSSTSSpecUI implements IObjectDescriptor<SSSTSSpecification> {

    final SSSTSSpecification core;

    public SSSTSSpecUI(SSSTSSpecification spec) {
        core = spec;
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = ppDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = bsmDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = benchDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }

    @Override
    public String getDisplayName() {
        return "Mixed Airline model";
    }

    public PreprocessingSpecUI getPreprocessing() {
        if (core.getPreprocessingSpec() == null) {
            core.setPreprocessingSpec(new PreprocessingSpecification());
            core.getPreprocessingSpec().method = Method.None;
        }
        return new PreprocessingSpecUI(core.getPreprocessingSpec());
    }

    public SeasonalSpecUI2 getMa() {
        return new SeasonalSpecUI2(core.getDecompositionSpec());
    }
    public SaBenchmarkingSpecUI getBenchmarking() {
        return new SaBenchmarkingSpecUI(core.getBenchmarkingSpec(), false);
    }

    private EnhancedPropertyDescriptor benchDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("benchmarking", this.getClass(), "getBenchmarking", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, BENCH_ID);
            desc.setDisplayName("BENCHMARKING");
            return edesc;
        }
        catch (IntrospectionException ex) {
            return null;
        }
    }
    
   private static final int PP_ID = 1, MA_ID = 2, BENCH_ID=3;

    private EnhancedPropertyDescriptor ppDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("preprocessing", this.getClass(), "getPreprocessing", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, PP_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(PP_NAME);
            desc.setShortDescription(PP_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor bsmDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("maSpec", this.getClass(), "getMa", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, MA_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(MA_NAME);
            desc.setShortDescription(MA_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }
    private static final String PP_NAME = "Pre-processing",
            MA_NAME = "Airline + Noise";
    private static final String PP_DESC = "Pre-processing",
            MA_DESC = "Airline + seasonal noise";

    @Override
    public SSSTSSpecification getCore() {
        return core;
    }
}
