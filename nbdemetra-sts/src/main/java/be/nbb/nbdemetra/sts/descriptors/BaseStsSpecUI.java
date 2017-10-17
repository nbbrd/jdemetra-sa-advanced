/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package be.nbb.nbdemetra.sts.descriptors;

import be.nbb.demetra.sts.StsSpecification;
import ec.tstoolkit.descriptors.IPropertyDescriptors;

/**
 *
 * @author Jean Palate
 */
public abstract class BaseStsSpecUI implements IPropertyDescriptors {

    final StsSpecification core;

    public BaseStsSpecUI(StsSpecification spec){
        core = spec;
    }
}
