/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.sa;

import jd2.algorithm.IProcResults;
import jd2.information.InformationMapping;
import ec.satoolkit.GenericSaProcessingFactory;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.benchmarking.SaBenchmarkingResults;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.jdr.mapping.PreprocessingInfo;
import ec.tstoolkit.jdr.seats.SeatsInfo;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Data
public class TramoSeatsResults implements IProcResults {

    final CompositeResults results;
    SaDiagnostics diagnostics;
    CoherenceDiagnostics coherence;
    ResidualsDiagnostics residuals;
    SaBenchmarkingResults benchmarking;

    SaDiagnostics diagnostics() {
        synchronized (results) {
            if (diagnostics == null && results != null) {
                PreprocessingModel regarima = (PreprocessingModel) results.get(GenericSaProcessingFactory.PREPROCESSING);
                SeatsResults seats = results.get(GenericSaProcessingFactory.DECOMPOSITION, SeatsResults.class);
                ISeriesDecomposition finals = (ISeriesDecomposition) results.get(GenericSaProcessingFactory.FINAL);
                diagnostics = SaDiagnostics.of(regarima, seats, finals);
            }
            return diagnostics;
        }
    }

    CoherenceDiagnostics coherence(){
        synchronized (results) {
            if (coherence == null && results != null){
                coherence=CoherenceDiagnostics.of(results);
            }
            return coherence;
        }
    }

    ResidualsDiagnostics residuals() {
        synchronized (results) {
            if (residuals == null && results != null) {
                residuals = ResidualsDiagnostics.of(results);
            }
            return residuals;
        }
    }

    public ec.tstoolkit.jdr.regarima.Processor.Results regarima() {
        return new ec.tstoolkit.jdr.regarima.Processor.Results(model());
    }

    PreprocessingModel model() {
        return results == null ? null : results.get(TramoSeatsProcessingFactory.PREPROCESSING, PreprocessingModel.class);
    }

    ISeriesDecomposition finals() {
        return results == null ? null : results.get(TramoSeatsProcessingFactory.FINAL, ISeriesDecomposition.class);
    }

    SeatsResults seats() {
        return results == null ? null : results.get(TramoSeatsProcessingFactory.DECOMPOSITION, SeatsResults.class);
    }

    SaBenchmarkingResults benchmarking() {
        return results == null ? null : results.get(TramoSeatsProcessingFactory.BENCHMARKING, SaBenchmarkingResults.class);
    }
    static final InformationMapping<TramoSeatsResults> MAPPING = new InformationMapping<>(TramoSeatsResults.class);

    static {
        MAPPING.delegate(null, SaDecompositionInfo.getMapping(), source -> source.finals());
        MAPPING.delegate("preprocessing", PreprocessingInfo.getMapping(), source -> source.model());
        MAPPING.delegate("decomposition", SeatsInfo.getMapping(), source -> source.seats());
        MAPPING.delegate("diagnostics", SaDiagnostics.getMapping(), source -> source.diagnostics());
        MAPPING.delegate("coherence", CoherenceDiagnostics.getMapping(), source -> source.coherence());
        MAPPING.delegate("residuals", ResidualsDiagnostics.getMapping(), source -> source.residuals());
        MAPPING.delegate("benchmarking", SaBenchmarkingResultsInfo.getMapping(), source -> source.benchmarking());
    }

    public InformationMapping<TramoSeatsResults> getMapping() {
        return MAPPING;
    }

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        Map<String, Class> dic = new LinkedHashMap<>();
        MAPPING.fillDictionary(null, dic, true);
        return dic;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return MAPPING.getData(this, id, tclass);
    }
}
