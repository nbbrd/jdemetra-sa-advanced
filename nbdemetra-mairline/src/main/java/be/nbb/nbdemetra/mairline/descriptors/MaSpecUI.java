/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.nbdemetra.mairline.descriptors;

import be.nbb.demetra.mairline.MaSpecification;
import ec.tstoolkit.descriptors.EnhancedPropertyDescriptor;
import ec.tstoolkit.descriptors.IPropertyDescriptors;
import ec.tstoolkit.utilities.Arrays2;
import ec.tstoolkit.utilities.IntList;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class MaSpecUI implements IPropertyDescriptors {

    final MaSpecification core;

    public MaSpecUI(MaSpecification spec) {
        core = spec;
    }

    public String getNoisyPeriods() {
        int[] periods = core.noisyPeriods;
        StringBuilder builder = new StringBuilder();
        if (periods != null && periods.length > 0) {
            builder.append(periods[0]+1);
            for (int i = 1; i < periods.length; ++i) {
                builder.append(' ').append(periods[i] + 1);
            }
        }
        return builder.toString();
        // return core.noisyPeriods;
    }

    public void setNoisyPeriods(String values) {
        IntList periods = new IntList();
        ec.tstoolkit.utilities.StringFormatter.read(periods, values);
        if (periods.isEmpty()) {
            core.noisyPeriods = null;
        } else {
            periods.toArray();
            int[] p = periods.toArray();
            for (int i = 0; i < p.length; ++i) {
                p[i]--;
            }
            core.noisyPeriods = p;
        }
//        if (Arrays2.isNullOrEmpty(values))
//            core.noisyPeriods=null;
//        else
//            core.noisyPeriods=values;
    }

    public boolean isAllPeriods() {
        return core.allPeriods;
    }

    public void setAllPeriods(boolean all) {
        core.allPeriods = all;
    }
    
    public double getStep(){
        return core.step;
    }
    
    public void setStep(double value){
        core.step=value;
    }
    
    public MaSpecification.EstimationMethod getEstimationMethod(){
        return core.method;
    }
    
    public void setEstimationMethod( MaSpecification.EstimationMethod method){
        core.method=method;
    }

    private EnhancedPropertyDescriptor npDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("noisyPeriods", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, NP_ID);
            desc.setDisplayName(NP_NAME);
            desc.setShortDescription(NP_DESC);
            edesc.setReadOnly(core.allPeriods);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor aDesc() {
        if (core.noisyPeriods != null)
            return null;
        try {
            PropertyDescriptor desc = new PropertyDescriptor("allPeriods", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, A_ID);
            desc.setDisplayName(A_NAME);
            desc.setShortDescription(A_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor mDesc() {
        if (core.noisyPeriods != null || core.allPeriods)
            return null;
        try {
            PropertyDescriptor desc = new PropertyDescriptor("estimationMethod", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, M_ID);
            desc.setDisplayName(M_NAME);
            desc.setShortDescription(M_DESC);
            edesc.setReadOnly(core.allPeriods || (core.noisyPeriods != null && core.noisyPeriods.length > 0));
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor stepDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("step", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, M_ID);
            desc.setDisplayName(S_NAME);
            desc.setShortDescription(S_DESC);
            edesc.setReadOnly(core.allPeriods || (core.noisyPeriods != null && core.noisyPeriods.length > 0)
            || core.method != MaSpecification.EstimationMethod.LikelihoodGradient);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    
    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = npDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = aDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = mDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = stepDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }

    public static final int NP_ID = 0, A_ID = 1, M_ID = 2, S_ID=3;
    public static final String NP_NAME = "Noisy periods",
            A_NAME = "All periods", M_NAME = "Estimation method", S_NAME = "Step";
    public static final String NP_DESC = "Noisy periods",
            A_DESC = "Consider all periods as noisy", M_DESC = "Estimation method: iterative = Bell's solution, "
            + "errorvariance = based on the variance of the prediction error",
            S_DESC = "Step for err. variance in the computation of the likelihood difference"; 
            

    public String getDisplayName() {
        return "Airline + Noise";
    }
}
