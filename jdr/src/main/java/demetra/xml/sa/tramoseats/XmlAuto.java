/*
* Copyright 2013 National Bank of Belgium
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


package demetra.xml.sa.tramoseats;

import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import demetra.xml.sa.IXmlAuto;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.tramo.TransformSpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlAuto.NAME)
public class XmlAuto extends AbstractXmlTransform implements IXmlAuto {
    static final String NAME = "autoTransformSpecType";

    @XmlElement
    public Double fct = TransformSpec.DEF_FCT;
    public boolean isFctSpecified() {
        return fct != null;
    }

    public XmlAuto() { }

    @Override
    public void copyTo(TramoSeatsSpecification spec) {
        spec.getTramoSpecification().setTransform(new TransformSpec());
        spec.getTramoSpecification().getTransform().setFunction(DefaultTransformationType.Auto);
        if (isFctSpecified())
            spec.getTramoSpecification().getTransform().setFct(fct);
    }
}
