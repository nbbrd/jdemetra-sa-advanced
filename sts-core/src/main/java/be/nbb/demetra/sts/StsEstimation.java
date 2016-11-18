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
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ec.demetra.realfunctions.IFunctionPoint;

/**
 *
 * @author Jean Palate
 */
public class StsEstimation implements IProcResults{
    
    private final BsmMonitor monitor_;
    private final TsData y_;
    private final TsVariableList x_;

    public StsEstimation(TsData y, TsVariableList x, BsmMonitor monitor) {
        monitor_ = monitor;
        y_=y;
        x_=x;
    }

    @Override
    public boolean contains(String id) {
        synchronized (mapper) {
            return mapper.contains(id);
        }
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Map<String, Class> getDictionary() {
        // TODO
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        mapper.fillDictionary(null, map);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        synchronized (mapper) {
            
                return (T) mapper.getData(this, id, tclass);
        }
    }


    public BasicStructuralModel getModel() {
        return monitor_.getResult();
    }
    
    public TsData getY(){
        return y_;
    }

    public TsVariableList getX(){
        return x_;
    }

    public TsData getResiduals() {
        IReadDataBlock res = monitor_.getLikelihood().getResiduals();
        TsDomain edom=y_.getDomain();
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


    // MAPPERS

    public static <T> void addMapping(String name, InformationMapper.Mapper<StsEstimation, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }

    private static final InformationMapper<StsEstimation> mapper = new InformationMapper<>();

    static {
        mapper.add("residuals", new InformationMapper.Mapper<StsEstimation, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StsEstimation source) {
                return source.getResiduals();
            }
        });
    }
}

