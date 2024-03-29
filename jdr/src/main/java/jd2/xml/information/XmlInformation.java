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

package jd2.xml.information;

import jd2.datatypes.Ts;
import jd2.datatypes.TsCollection;
import jd2.datatypes.TsMoniker;
import jd2.xml.DummyAdapter;
import jd2.xml.XmlAlgorithm;
import jd2.xml.XmlConverterAdapter;
import jd2.xml.XmlDoubles;
import jd2.xml.XmlIntegers;
import jd2.xml.XmlMatrix;
import jd2.xml.XmlParameter;
import jd2.xml.XmlParameters;
import jd2.xml.XmlPeriodSelection;
import jd2.xml.XmlRegItem;
import jd2.xml.XmlStatisticalTest;
import jd2.xml.XmlStrings;
import jd2.xml.XmlTs;
import jd2.xml.XmlTsCollection;
import jd2.xml.XmlTsData;
import jd2.xml.XmlTsMoniker;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.RegressionItem;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.lang.reflect.Type;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlInformation.RNAME)
@XmlType(name = XmlInformation.NAME)
public class XmlInformation {

    static final String NAME = "informationType";
    static final String RNAME = "information";
    static final HashMap<Type, XmlAdapter> fromXmlMap = new HashMap<>();
    static final HashMap<Type, XmlAdapter> toXmlMap = new HashMap<>();

    // default mapping
    static {
        DummyAdapter dummy = new DummyAdapter();
        fromXmlMap.put(Double.class, dummy);
        fromXmlMap.put(Integer.class, dummy);
        fromXmlMap.put(String.class, dummy);
        fromXmlMap.put(Boolean.class, dummy);
        fromXmlMap.put(boolean[].class, dummy);
        //fromXmlMap.put(String[].class, dummy);
        toXmlMap.put(Double.class, dummy);
        toXmlMap.put(Integer.class, dummy);
        toXmlMap.put(String.class, dummy);
        toXmlMap.put(Boolean.class, dummy);
        toXmlMap.put(boolean[].class, dummy);
        //toXmlMap.put(String[].class, dummy);

        XmlConverterAdapter<XmlStrings, String[]> strMapper =
                new XmlConverterAdapter<>(XmlStrings.class);
        fromXmlMap.put(XmlStrings.class, strMapper);
        toXmlMap.put(String[].class, strMapper);

        XmlConverterAdapter<XmlDoubles, double[]> doublesMapper =
                new XmlConverterAdapter<>(XmlDoubles.class);
        fromXmlMap.put(XmlDoubles.class, doublesMapper);
        toXmlMap.put(double[].class, doublesMapper);

        XmlConverterAdapter<XmlIntegers, int[]> intsMapper =
                new XmlConverterAdapter<>(XmlIntegers.class);
        fromXmlMap.put(XmlIntegers.class, intsMapper);
        toXmlMap.put(int[].class, intsMapper);

        XmlConverterAdapter<XmlInformationSet, InformationSet> infoMapper =
                new XmlConverterAdapter<>(XmlInformationSet.class);
        fromXmlMap.put(XmlInformationSet.class, infoMapper);
        toXmlMap.put(InformationSet.class, infoMapper);

        XmlConverterAdapter<XmlAlgorithm, AlgorithmDescriptor> algMapper =
                new XmlConverterAdapter<>(XmlAlgorithm.class);

        fromXmlMap.put(XmlAlgorithm.class, algMapper);
        toXmlMap.put(AlgorithmDescriptor.class, algMapper);

        XmlConverterAdapter<XmlTsData, TsData> tsdataMapper =
                new XmlConverterAdapter<>(XmlTsData.class);

        fromXmlMap.put(XmlTsData.class, tsdataMapper);
        toXmlMap.put(TsData.class, tsdataMapper);

        XmlConverterAdapter<XmlPeriodSelection, TsPeriodSelector> pselMapper =
                new XmlConverterAdapter<>(XmlPeriodSelection.class);

        fromXmlMap.put(XmlPeriodSelection.class, pselMapper);
        toXmlMap.put(TsPeriodSelector.class, pselMapper);

        XmlConverterAdapter<XmlParameters, Parameter[]> paramsMapper =
                new XmlConverterAdapter<>(XmlParameters.class);

        fromXmlMap.put(XmlParameters.class, paramsMapper);
        toXmlMap.put(Parameter[].class, paramsMapper);

        XmlConverterAdapter<XmlParameter, Parameter> paramMapper =
                new XmlConverterAdapter<>(XmlParameter.class);

        fromXmlMap.put(XmlParameter.class, paramMapper);
        toXmlMap.put(Parameter.class, paramMapper);

        //XmlConverterMapper<XmlDiagnostic, SADiagnostic> diagsMapper
        //     = new XmlConverterMapper<XmlDiagnostic, SADiagnostic>();

        //fromXmlMap.put(XmlDiagnostic.class, diagsMapper);
        //toXmlMap.put(SeasonalAdjustmentDiagnostic.class, diagsMapper);

        XmlConverterAdapter<XmlTsMoniker, TsMoniker> monikerMapper =
                new XmlConverterAdapter<>(XmlTsMoniker.class);

        fromXmlMap.put(XmlTsMoniker.class, monikerMapper);
        toXmlMap.put(TsMoniker.class, monikerMapper);

        XmlConverterAdapter<XmlTs, Ts> tsMapper =
                new XmlConverterAdapter<>(XmlTs.class);

        fromXmlMap.put(XmlTs.class, tsMapper);
        toXmlMap.put(Ts.class, tsMapper);
        
        XmlConverterAdapter<XmlTsCollection, TsCollection> tsCollectionMapper =
                new XmlConverterAdapter<>(XmlTsCollection.class);

        fromXmlMap.put(XmlTsCollection.class, tsCollectionMapper);
        toXmlMap.put(TsCollection.class, tsCollectionMapper);

        XmlConverterAdapter<XmlRegItem, RegressionItem> regitemMapper =
                new XmlConverterAdapter<>(XmlRegItem.class);

        fromXmlMap.put(XmlRegItem.class, regitemMapper);
        toXmlMap.put(RegressionItem.class, regitemMapper);

        XmlConverterAdapter<XmlStatisticalTest, StatisticalTest> statsMapper =
                new XmlConverterAdapter<>(XmlStatisticalTest.class);

        fromXmlMap.put(XmlStatisticalTest.class, statsMapper);
        toXmlMap.put(StatisticalTest.class, statsMapper);
//        XmlConverterMapper<InterventionVariable, XmlInterventionVariable> ivarMapper =
//                new XmlConverterMapper<>(XmlInterventionVariable.class);
//
//        fromXmlMap.put(XmlInterventionVariable.class, ivarMapper);
//        toXmlMap.put(InterventionVariable.class, ivarMapper);
//        XmlConverterMapper<Ramp, XmlRamp> rampMapper =
//                new XmlConverterMapper<>(XmlRamp.class);
//
//        fromXmlMap.put(XmlRamp.class, rampMapper);
//        toXmlMap.put(Ramp.class, rampMapper);
        
        XmlConverterAdapter<XmlMatrix, Matrix> matrixMapper =
                new XmlConverterAdapter<>(XmlMatrix.class);

        fromXmlMap.put(XmlMatrix.class, matrixMapper);
        toXmlMap.put(Matrix.class, matrixMapper);
        
    }
    
    @XmlAttribute
    public String name;
    @XmlElements(value = {
        @XmlElement(type = Boolean.class, name = "boolean"),
        @XmlElement(type = Double.class, name = "double"),
        @XmlElement(type = Integer.class, name = "integer"),
        @XmlElement(type = String.class, name = "string"),
        @XmlElement(type = XmlIntegers.class, name = "integers"),
        @XmlElement(type = XmlDoubles.class, name = "doubles"),
        @XmlElement(type = boolean[].class, name = "booleans"),
        @XmlElement(type = XmlStrings.class, name = "strings"),
        @XmlElement(type = XmlInformationSet.class, name = "subset"),
        @XmlElement(type = XmlAlgorithm.class, name = "method"),
        @XmlElement(type = XmlTsData.class, name = "tsdata"),
        @XmlElement(type = XmlTsMoniker.class, name = "moniker"),
        @XmlElement(type = XmlTs.class, name = "ts"),
        @XmlElement(type = XmlTsCollection.class, name = "tscollection"),
        @XmlElement(type = XmlPeriodSelection.class, name = "span"),
        @XmlElement(type = XmlStatisticalTest.class, name = "test"),
//        @XmlElement(type = XmlInterventionVariable.class, name = "ivar"),
//        @XmlElement(type = XmlRamp.class, name = "ramp"),
        @XmlElement(type = XmlRegItem.class, name = "regitem"),
        @XmlElement(type = XmlParameter.class, name = "param"),
        @XmlElement(type = XmlParameters.class, name = "params"),       
        @XmlElement(type = XmlMatrix.class, name = "matrix")})
    Object information;

    public Information<Object> toInformation() throws Exception {
        if (information == null) {
            return null;
        }
        XmlAdapter mapper = fromXmlMap.get(information.getClass());
        if (mapper == null) {
            return null;
        } else {
            return new Information<>(name, mapper.unmarshal(information));
        }
    }

    public XmlInformation() {
    }

    public XmlInformation(String name, Object value) {
        this.name = name;
        this.information = value;
    }

    public static <T> XmlInformation create(Information<T> info) throws Exception {
        XmlAdapter mapper = toXmlMap.get(info.value.getClass());
        if (mapper != null) {
            return new XmlInformation(info.name, mapper.marshal(info.value));
        } else if (info.value.getClass().isEnum()) {
            return new XmlInformation(info.name, info.value.toString());
        } else {
            return null;
        }
    }
}
