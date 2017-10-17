/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import ec.demetra.ssf.implementations.structural.BasicStructuralModel;
import ec.demetra.ssf.implementations.structural.ModelSpecification;
import ec.demetra.ssf.implementations.structural.SsfBsm;
import ec.demetra.ssf.implementations.structural.SeasonalModel;
import ec.demetra.ssf.univariate.SsfData;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Random;

/**
 *
 * @author Admin
 */
public class Models {

    public final static SsfBsm ssfBsm;
    public final static SsfData ssfProd, ssfX, ssfXRandom;
    public final static TsData xts;

    static {
        int M = 50;
        xts = data.Data.X.clone();
        int[] missing = new int[M];
        Random rng = new Random();
        for (int i = 0; i < M; ++i) {
            missing[i] = rng.nextInt(xts.getLength());
        }
 
        for (int i = 0; i < M; ++i) {
            xts.setMissing(missing[i]);
        }
        ModelSpecification mspec = new ModelSpecification();
        mspec.setSeasonalModel(SeasonalModel.Crude);
        BasicStructuralModel model = new BasicStructuralModel(mspec, 12);
        ssfBsm = SsfBsm.create(model);

        ssfXRandom = new SsfData(xts);

        ssfProd = new SsfData(Data.P);
        ssfX = new SsfData(Data.X);

    }
}
