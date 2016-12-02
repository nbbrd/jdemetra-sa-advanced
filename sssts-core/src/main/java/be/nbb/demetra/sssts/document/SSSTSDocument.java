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
import be.nbb.demetra.sssts.SSSTSResults;
import be.nbb.demetra.sssts.SSSTSSpecification;
import ec.satoolkit.GenericSaResults;
import ec.tss.sa.SaManager;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;

/**
 *
 * @author Jean Palate
 */
public class SSSTSDocument extends SaDocument<SSSTSSpecification> implements Cloneable{

    public SSSTSDocument(){
        super(SaManager.instance.getProcessor(SSSTSProcessor.DESCRIPTOR));
        setSpecification(new SSSTSSpecification());
    }
   
    public SSSTSDocument(ProcessingContext context){
        super(SaManager.instance.getProcessor(SSSTSProcessor.DESCRIPTOR), context);
        setSpecification(new SSSTSSpecification());
    }
    
    @Override
    public SSSTSDocument clone(){
        return (SSSTSDocument) super.clone();
    }
    
    @Override
    public SSSTSResults getDecompositionPart(){
        CompositeResults rslts=getResults();
        if (rslts == null)
            return null;
        else
            return GenericSaResults.getDecomposition(rslts, SSSTSResults.class);
    }
}
