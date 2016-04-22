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

import be.nbb.demetra.sts.ModelSpecification;
import ec.satoolkit.IDefaultSeriesDecomposer;
import ec.satoolkit.IPreprocessingFilter;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
public class SSHSDecomposer implements IDefaultSeriesDecomposer<SSHSResults> {

    private final SeasonalSpecification sspec;
    private final ModelSpecification mspec;
    private SSHSResults results;

    public SSHSDecomposer(ModelSpecification mspec, SeasonalSpecification sspec) {
        this.mspec=mspec;
        this.sspec=sspec;
    }

    @Override
    public boolean decompose(PreprocessingModel model, IPreprocessingFilter filter) {
        SSHSMonitor monitor = new SSHSMonitor();
        TsData y = filter.getCorrectedSeries(true);
        if (!monitor.process(y, mspec.clone(), sspec.clone())) {
            return false;
        }
        else {
            results = new SSHSResults(y, monitor, model.description.getTransformation() == DefaultTransformationType.Log);
            return true;
        }
    }

    @Override
    public boolean decompose(TsData y) {
        SSHSMonitor monitor = new SSHSMonitor();
        if (!monitor.process(y, mspec.clone(), sspec.clone())) {
            return false;
        }
        else {
            results = new SSHSResults(y, monitor, false);
            return true;
        }
    }

    @Override
    public SSHSResults getDecomposition() {
        return results;
    }
}
