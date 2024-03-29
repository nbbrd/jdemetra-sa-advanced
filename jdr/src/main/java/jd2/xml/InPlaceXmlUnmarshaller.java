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
package jd2.xml;

/**
 *
 * @author Jean Palate
 * @param <X> Xml class
 * @param <J> pure Java class
 */
public interface InPlaceXmlUnmarshaller <X, J>{
    /**
     * Reads an xml object an put its information in a Java object
     * @param xml The xml being read
     * @param v The object being initialised
     * @return
     */
    boolean unmarshal(X xml, J v);
}
