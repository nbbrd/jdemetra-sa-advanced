/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.jdr.sa;

import jd2.information.InformationMapping;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.satoolkit.benchmarking.SaBenchmarkingResults;

/**
 *
 * @author Alain Quartier la Tente
 */
@lombok.experimental.UtilityClass
public class SaBenchmarkingResultsInfo {

    final InformationMapping<SaBenchmarkingResults> MAPPING = new InformationMapping<>(SaBenchmarkingResults.class);
    
    public InformationMapping<SaBenchmarkingResults> getMapping() {
        return MAPPING;
    }

    static {
        MAPPING.set("original", TsData.class, source -> source.getOriginalSeries());
        MAPPING.set("target", TsData.class, source -> source.getTarget());
        MAPPING.set("result", TsData.class, source -> source.getBenchmarkedSeries());
    }

}
