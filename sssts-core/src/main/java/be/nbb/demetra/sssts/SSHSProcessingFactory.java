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
package be.nbb.demetra.sssts;

import ec.satoolkit.DefaultPreprocessingFilter;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.GenericSaProcessingFactory;
import static ec.satoolkit.GenericSaProcessingFactory.BENCHMARKING;
import ec.satoolkit.benchmarking.SaBenchmarkingResults;
import ec.satoolkit.benchmarking.SaBenchmarkingSpec;
import ec.tstoolkit.algorithm.*;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.modelling.arima.Method;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SSHSProcessingFactory extends GenericSaProcessingFactory implements IProcessingFactory<SSHSSpecification, TsData, CompositeResults> {
    
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, "SSHS", null);
    
    private static SequentialProcessing<TsData> create(SSHSSpecification xspec, ProcessingContext context) {
        SequentialProcessing processing = new SequentialProcessing();
        DefaultPreprocessingFilter filter = new DefaultPreprocessingFilter();
        if (xspec.getPreprocessingSpec().method != Method.None) {
            addPreprocessingStep(xspec.buildPreprocessor(context), processing);
        }
        addDecompositionStep(new SSHSDecomposer(xspec.getModelSpecification(), xspec.getDecompositionSpec()), filter, processing);
        addFinalStep(filter, processing);
        // TODO For test only
        SaBenchmarkingSpec bspec = xspec.getBenchmarkingSpec();
        if (bspec != null && bspec.isEnabled()) {
            addBenchmarkingStep(bspec, processing);
        }
        return processing;
    }
    
    public static final SSHSProcessingFactory instance = new SSHSProcessingFactory();
    
    protected SSHSProcessingFactory() {
    }
    
    public static CompositeResults process(TsData s, SSHSSpecification xspec) {
        SequentialProcessing<TsData> processing = create(xspec, null);
        return processing.process(s);
    }
    
    @Override
    public SequentialProcessing<TsData> generateProcessing(SSHSSpecification xspec, ProcessingContext context) {
        return create(xspec, context);
    }
    
    public SequentialProcessing<TsData> generateProcessing(SSHSSpecification xspec) {
        return create(xspec, null);
    }
    
    @Override
    public void dispose() {
    }
    
    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }
    
    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof SSHSSpecification;
    }
    
    @Override
    public Map<String, Class> getSpecificationDictionary(Class<SSHSSpecification> specClass) {
        HashMap<String, Class> dic = new HashMap<>();
        SSHSSpecification.fillDictionary(null, dic);
        return dic;
    }
    
    @Override
    public Map<String, Class> getOutputDictionary(boolean compact) {
        HashMap<String, Class> dic = new HashMap<>();
        PreprocessingModel.fillDictionary(null, dic, compact);
        DefaultSeriesDecomposition.fillDictionary(null, dic, compact);
        SaBenchmarkingResults.fillDictionary(BENCHMARKING, dic, compact);
        return dic;
    }
}
