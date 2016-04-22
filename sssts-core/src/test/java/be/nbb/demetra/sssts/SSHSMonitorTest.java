/*
 * Copyright 2016-2017 National Bank of Belgium
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

import data.Data;
import be.nbb.demetra.sssts.SSHSMonitor.MixedEstimation;
import be.nbb.demetra.sssts.SeasonalSpecification.EstimationMethod;
import be.nbb.demetra.sts.ModelSpecification;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class SSHSMonitorTest {
    
    public SSHSMonitorTest() {
    }

    @Test
    @Ignore
    public void test1() {
        SSHSMonitor monitor=new SSHSMonitor();
        ModelSpecification mspec=new ModelSpecification();
        SeasonalSpecification sspec=new SeasonalSpecification();
        //sspec.method= EstimationMethod.Iterative;
        boolean process = monitor.process(Data.X, mspec, sspec);
        
        for (MixedEstimation me :monitor.getAllResults()){
            System.out.print(me.model);
            System.out.print('\t');
            System.out.println(me.ll.getLogLikelihood());
       }
    }
    
}
