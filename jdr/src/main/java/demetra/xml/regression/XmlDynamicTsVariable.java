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
package demetra.xml.regression;

import demetra.datatypes.DynamicTsVariable;
import demetra.datatypes.TsMoniker;
import demetra.xml.IXmlConverter;
import demetra.xml.XmlNamedObject;
import demetra.xml.XmlTsData;
import demetra.xml.XmlTsMoniker;
import ec.tstoolkit.timeseries.simplets.TsData;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * Default dynamic regression variable. Identifier must be present. The name of
 * the tsdata sub-item should be considered as a description, while the "name"
 * attribute is the actual name of the variable
 *
 *
 * <p>
 * Java class for DynamicTsVariableType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="DynamicTsVariableType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/core}TsVariableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Moniker" type="{ec/eurostat/jdemetra/core}XmlTsMoniker"/&gt;
 *         &lt;element name="Tsdata" type="{ec/eurostat/jdemetra/core}TsDataType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlDynamicTsVariable extends XmlNamedObject implements IXmlConverter<DynamicTsVariable> {

    static final String NAME = "dynamicTsVariableType";
    @XmlElement
    public XmlTsData tsdata;
    @XmlElement
    public XmlTsMoniker moniker;

    @Override
    public DynamicTsVariable create() {
        TsMoniker m = moniker.create();
        if (tsdata != null) {
            return new DynamicTsVariable(tsdata.name, m, tsdata.create());
        } else {
            return new DynamicTsVariable(null, m, null);
        }
    }

    @Override
    public void copy(DynamicTsVariable t) {
        moniker = new XmlTsMoniker();
        moniker.copy(t.getMoniker());
        TsData d = t.getTsData();
        if (d != null) {
            tsdata = new XmlTsData();
            tsdata.copy(d);
            tsdata.name=t.getName();
        }
    }
}