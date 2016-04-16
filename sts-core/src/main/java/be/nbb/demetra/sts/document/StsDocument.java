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

package be.nbb.demetra.sts.document;

import be.nbb.demetra.sts.StsDecomposition;
import be.nbb.demetra.sts.StsEstimation;
import be.nbb.demetra.sts.StsProcessingFactory;
import be.nbb.demetra.sts.StsSpecification;
import ec.satoolkit.GenericSaResults;
import ec.tss.sa.SaManager;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.modelling.DeterministicComponent;

/**
 *
 * @author Jean Palate
 */
public class StsDocument extends SaDocument<StsSpecification> implements Cloneable {

    public StsDocument() {
        super(SaManager.instance.getProcessor(StsProcessor.DESCRIPTOR));
        setSpecification(new StsSpecification());
    }

    public StsDocument(ProcessingContext context) {
        super(SaManager.instance.getProcessor(StsProcessor.DESCRIPTOR), context);
        setSpecification(new StsSpecification());
    }
    
    public StsEstimation getEstimationPart(){
        CompositeResults rslts = getResults();
        return rslts.get(StsProcessingFactory.ESTIMATION, StsEstimation.class);
    }
    
    public DeterministicComponent getDeterministicPart(){
        CompositeResults rslts = getResults();
        return rslts.get(StsProcessingFactory.DETERMINISTIC, DeterministicComponent.class);
    }
    
    @Override
    public StsDecomposition getDecompositionPart() {
        CompositeResults rslts = getResults();
        return GenericSaResults.getDecomposition(rslts, StsDecomposition.class);
    }

    @Override
    public StsDocument clone() {
        return (StsDocument) super.clone();
    }
}
