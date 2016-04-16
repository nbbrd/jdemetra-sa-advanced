/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package be.nbb.nbdemetra.mairline.descriptors;

import be.nbb.demetra.mairline.PreprocessingSpecification;
import ec.tstoolkit.descriptors.IPropertyDescriptors;

/**
 *
 * @author Jean Palate
 */
public abstract class BasePreprocessingSpecUI  implements IPropertyDescriptors {

    final PreprocessingSpecification core;

    public BasePreprocessingSpecUI(PreprocessingSpecification spec){
        core = spec;
    }

}
