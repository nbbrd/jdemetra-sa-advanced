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
import be.nbb.demetra.sssts.SSHSResults;
import be.nbb.demetra.sssts.SSHSSpecification;
import ec.satoolkit.GenericSaResults;
import ec.tss.sa.SaManager;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;

/**
 *
 * @author Jean Palate
 */
public class SSHSDocument extends SaDocument<SSHSSpecification> implements Cloneable{

    public SSHSDocument(){
        super(SaManager.instance.getProcessor(SSHSProcessor.DESCRIPTOR));
        setSpecification(new SSHSSpecification());
    }
   
    public SSHSDocument(ProcessingContext context){
        super(SaManager.instance.getProcessor(SSHSProcessor.DESCRIPTOR), context);
        setSpecification(new SSHSSpecification());
    }
    
    @Override
    public SSHSDocument clone(){
        return (SSHSDocument) super.clone();
    }
    
    @Override
    public SSHSResults getDecompositionPart(){
        CompositeResults rslts=getResults();
        if (rslts == null)
            return null;
        else
            return GenericSaResults.getDecomposition(rslts, SSHSResults.class);
    }
}
