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
package be.nbb.demetra.sssts.document;

import be.nbb.demetra.sssts.SSSTSProcessingFactory;
import be.nbb.demetra.sssts.SSSTSSpecification;
import ec.satoolkit.ISaSpecification;
import ec.tss.sa.EstimationPolicyType;
import ec.tss.sa.ISaProcessingFactory;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class SSSTSProcessor implements ISaProcessingFactory<SSSTSSpecification> {

    public static final AlgorithmDescriptor DESCRIPTOR=SSSTSProcessingFactory.DESCRIPTOR;

    public SSSTSProcessor() {
    }

    @Override
    public ISaSpecification createSpecification(InformationSet info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ISaSpecification createSpecification(SaItem doc, TsDomain frozenPeriod, EstimationPolicyType policy, boolean nospan) {
        return doc.getEstimationSpecification();
    }

    @Override
    public boolean updatePointSpecification(SaItem item) {
        return true;
    }

    @Override
    public SaDocument<?> createDocument() {
        return new SSSTSDocument();
    }

    @Override
    public List<ISaSpecification> defaultSpecifications() {
        ArrayList<ISaSpecification> specs = new ArrayList<>();
        return specs;
    }

    @Override
    public void dispose() {
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return SSSTSProcessingFactory.instance.getInformation();
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return SSSTSProcessingFactory.instance.canHandle(spec);
    }

    @Override
    public IProcessing<TsData, CompositeResults> generateProcessing(SSSTSSpecification specification, ProcessingContext context) {
        return SSSTSProcessingFactory.instance.generateProcessing(specification, context);
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<SSSTSSpecification> specClass) {
        return SSSTSProcessingFactory.instance.getSpecificationDictionary(specClass);
    }
    @Override
    public Map<String, Class> getOutputDictionary(boolean compact) {
        return SSSTSProcessingFactory.instance.getOutputDictionary(compact);
    }
}
