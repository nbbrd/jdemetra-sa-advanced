/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package be.nbb.nbdemetra.mixedfreq.descriptors;

import be.nbb.demetra.mixedfreq.MixedFrequenciesSpecification;
import ec.tstoolkit.descriptors.IPropertyDescriptors;

/**
 *
 * @author Jean Palate
 */
public abstract class BaseSpecUI  implements IPropertyDescriptors {

    final MixedFrequenciesSpecification core;

    public BaseSpecUI(MixedFrequenciesSpecification spec){
        core = spec;
    }

}
