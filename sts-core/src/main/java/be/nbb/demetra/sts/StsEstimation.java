/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.sts;

import ec.demetra.realfunctions.IFunction;
import ec.demetra.ssf.dk.DkConcentratedLikelihood;
import ec.demetra.ssf.implementations.structural.BasicStructuralModel;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ec.demetra.realfunctions.IFunctionPoint;
import ec.tstoolkit.information.InformationMapping;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
public class StsEstimation implements IProcResults {

    public static final String RESIDUALS = "residuals";

    private final BsmMonitor monitor_;
    private final TsData y_;
    private final TsVariableList x_;

    public StsEstimation(TsData y, TsVariableList x, BsmMonitor monitor) {
        monitor_ = monitor;
        y_ = y;
        x_ = x;
    }

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Class> getDictionary() {
        // TODO
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        MAPPING.fillDictionary(null, map, false);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return (T) MAPPING.getData(this, id, tclass);
    }

    public BasicStructuralModel getModel() {
        return monitor_.getResult();
    }

    public TsData getY() {
        return y_;
    }

    public TsVariableList getX() {
        return x_;
    }

    public TsData getResiduals() {
        IReadDataBlock res = monitor_.getLikelihood().getResiduals();
        TsDomain edom = y_.getDomain();
        return new TsData(edom.getStart().plus(edom.getLength() - res.getLength()), res);
    }

    public DkConcentratedLikelihood getLikelihood() {
        return monitor_.getLikelihood();
    }

    public IFunction likelihoodFunction() {
        return monitor_.likelihoodFunction();
    }

    public IFunctionPoint maxLikelihoodFunction() {
        return monitor_.maxLikelihoodFunction();
    }

    // MAPPING
    public static InformationMapping<StsEstimation> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<StsEstimation, T> extractor) {
        MAPPING.set(name, tclass, extractor);
    }

    public static <T> void setTsData(String name, Function<StsEstimation, TsData> extractor) {
        MAPPING.set(name, extractor);
    }

    private static final InformationMapping<StsEstimation> MAPPING = new InformationMapping<>(StsEstimation.class);

    static {
        MAPPING.set(RESIDUALS, source -> source.getResiduals());
    }
}
