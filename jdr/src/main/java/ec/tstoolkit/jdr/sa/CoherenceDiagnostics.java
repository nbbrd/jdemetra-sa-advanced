/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.sa;

import jd2.algorithm.IProcResults;
import jd2.information.InformationMapping;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.GenericSaResults;
import ec.satoolkit.ISaResults;
import ec.satoolkit.ISeriesDecomposition;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcQuality;
import ec.tstoolkit.data.DescriptiveStatistics;
import static ec.tstoolkit.jdr.sa.SaDiagnostics.MAPPING;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.YearIterator;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public final class CoherenceDiagnostics implements IProcResults{
    private static final double EB = .5;
    private static final double UB = .01, BB = 0.05, SB = .1;
    private static final double LDEF = 1e-6;
    private static final int YSHORT = 7;
    private static final String SHORTSERIES = "Short series";
    public static final String DEF = "definition", BIAS = "annual totals";
    public static final String NAME = "Basic checks";
    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(DEF, BIAS));

    private double maxDA_, maxDDef_;
    private boolean mul_;
    private boolean short_;
    private double scale;

    static final InformationMapping<CoherenceDiagnostics> MAPPING = new InformationMapping<>(CoherenceDiagnostics.class);

    public static CoherenceDiagnostics of(CompositeResults rslts) {
        try {
            if (rslts == null || GenericSaResults.getDecomposition(rslts, ISaResults.class) == null) {
                return null;
            } else {
                return new CoherenceDiagnostics(rslts);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public static InformationMapping<CoherenceDiagnostics> getMapping() {
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
    
    private CoherenceDiagnostics(CompositeResults rslts) {
        test(rslts);
    }

    private void test(CompositeResults rslts) {
        TsData yl = rslts.getData(ModellingDictionary.Y_LIN, TsData.class);
        if (yl != null && yl.getLength() < YSHORT * yl.getFrequency().intValue()) {
            short_ = true;
        }
        ISeriesDecomposition decomposition = GenericSaResults.getFinalDecomposition(rslts);
        mul_ = decomposition.getMode() != DecompositionMode.Additive;
        TsData y = rslts.getData(ModellingDictionary.YC, TsData.class);
        DescriptiveStatistics ds=new DescriptiveStatistics(y);
        scale=ds.getRmse();
        TsData yc = y;
        TsData sa = rslts.getData(ModellingDictionary.SA, TsData.class);
        TsData s = rslts.getData(ModellingDictionary.S, TsData.class);
        TsData t = rslts.getData(ModellingDictionary.T, TsData.class);
        TsData i = rslts.getData(ModellingDictionary.I, TsData.class);
        if (decomposition.getMode() == DecompositionMode.PseudoAdditive) {
            // finals
            TsData df0 = sub(y, TsData.multiply(t, TsData.add(s, i).minus(1)));
            TsData df1 = sub(sa, TsData.multiply(t, i));
            check(df0);
            check(df1);
        } else {

            TsData regy = rslts.getData(ModellingDictionary.REG_Y, TsData.class);
            TsData regsa = rslts.getData(ModellingDictionary.REG_SA, TsData.class);
            TsData cy = rslts.getData(ModellingDictionary.Y_CMP, TsData.class);
            TsData ct = rslts.getData(ModellingDictionary.T_CMP, TsData.class);
            TsData cs = rslts.getData(ModellingDictionary.S_CMP, TsData.class);
            TsData ci = rslts.getData(ModellingDictionary.I_CMP, TsData.class);
            TsData csa = rslts.getData(ModellingDictionary.SA_CMP, TsData.class);
            TsData ly = rslts.getData(ModellingDictionary.Y_LIN, TsData.class);
            TsData lt = rslts.getData(ModellingDictionary.T_LIN, TsData.class);
            TsData ls = rslts.getData(ModellingDictionary.S_LIN, TsData.class);
            TsData li = rslts.getData(ModellingDictionary.I_LIN, TsData.class);
            TsData lsa = rslts.getData(ModellingDictionary.SA_LIN, TsData.class);
            TsData tde = rslts.getData(ModellingDictionary.TDE, TsData.class);
            TsData ee = rslts.getData(ModellingDictionary.EE, TsData.class);
            TsData omhe = rslts.getData(ModellingDictionary.OMHE, TsData.class);
            TsData cal = rslts.getData(ModellingDictionary.CAL, TsData.class);
            TsData outs = rslts.getData(ModellingDictionary.OUT_S, TsData.class);
            TsData regs = rslts.getData(ModellingDictionary.REG_S, TsData.class);
            TsData outt = rslts.getData(ModellingDictionary.OUT_T, TsData.class);
            TsData regt = rslts.getData(ModellingDictionary.REG_T, TsData.class);
            TsData outi = rslts.getData(ModellingDictionary.OUT_I, TsData.class);
            TsData regi = rslts.getData(ModellingDictionary.REG_I, TsData.class);

            // main constraints
            yc = inv_op(y, regy);
            // finals
            TsData df0 = sub(yc, op(t, s, i, regsa));
            TsData df1 = sub(sa, op(t, i, regsa));
            TsData df2 = sub(inv_op(y, regy), op(sa, s));
            TsData df3 = sub(s, op(cs, cal, regs, outs));
            TsData df4 = sub(t, op(ct, regt, outt));
            TsData df5 = sub(i, op(ci, regi, outi));
            TsData dcal = sub(cal, op(tde, ee, omhe));
            // components
            TsData dc0 = sub(cy, op(ct, cs, ci));
            TsData dc1 = sub(csa, op(ct, ci));
            TsData dc2 = sub(cy, op(csa, cs));

            maxDDef_ = Double.NaN;
            check(df0);
            check(df1);
            check(df2);
            check(df3);
            check(df4);
            check(df5);
            check(dcal);
            check(dc0);
            check(dc1);
            check(dc2);
            // lin
            if (lsa != null) {
                TsData dl0 = sub(ly, add(lt, ls, li));
                TsData dl1 = sub(lsa, add(lt, li));
                TsData dl2 = sub(ly, add(lsa, ls));
                check(dl0);
                check(dl1);
                check(dl2);
            }
        }
        // annual totals
        YearIterator yiter = YearIterator.fullYears(yc);
        YearIterator saiter = YearIterator.fullYears(sa);

        maxDA_ = 0;
        while (yiter.hasMoreElements() && saiter.hasMoreElements()) {
            TsDataBlock ydb = yiter.nextElement();
            TsDataBlock sadb = saiter.nextElement();
            double dcur = Math.abs(ydb.data.sum() - sadb.data.sum());
            if (dcur > maxDA_) {
                maxDA_ = dcur;
            }
        }
        maxDA_ /= y.getFrequency().intValue() * scale;
    }

    private TsData op(TsData l, TsData r) {
        if (mul_) {
            return TsData.multiply(l, r);
        } else {
            return TsData.add(l, r);
        }
    }

    private TsData op(TsData a, TsData b, TsData c) {
        if (mul_) {
            return TsData.multiply(a, TsData.multiply(b, c));
        } else {
            return TsData.add(a, TsData.add(b, c));
        }
    }

    private TsData add(TsData l, TsData r) {
        return TsData.add(l, r);
    }

    private TsData add(TsData a, TsData b, TsData c) {
        return TsData.add(a, TsData.add(b, c));
    }

    private TsData op(TsData a, TsData b, TsData c, TsData d) {
        return op(op(a, b), op(c, d));
    }

    private TsData inv_op(TsData l, TsData r) {
        if (mul_) {
            return TsData.divide(l, r);
        } else {
            return TsData.subtract(l, r);
        }
    }

    private TsData sub(TsData l, TsData r) {
        return TsData.subtract(l, r);
    }

    public ProcQuality getDiagnostic(String test) {
        if (test.equals(ALL.get(0))) {
            if (Double.isNaN(maxDDef_)) {
                return ProcQuality.Error;
            }
            return maxDDef_ < LDEF ? ProcQuality.Good : ProcQuality.Error;
        } else {
            if (Double.isNaN(maxDA_) || maxDA_ > EB) {
                return ProcQuality.Error;
            } else if (maxDA_ > SB) {
                return ProcQuality.Severe;
            } else if (maxDA_ > BB) {
                return ProcQuality.Bad;
            } else if (maxDA_ > UB) {
                return ProcQuality.Uncertain;
            } else {
                return ProcQuality.Good;
            }
        }
    }

    public double getValue(String test) {
        double val;
        if (test.equals(ALL.get(0))) {
            val = maxDDef_;
        } else {
            val = maxDA_;
        }
        return val;
    }

    public List<String> getWarnings() {
        if (short_) {
            return Collections.singletonList(SHORTSERIES);
        } else {
            return null;
        }
    }


    private void check(TsData d) {
        if (d == null || scale == 0) {
            return;
        }
        DescriptiveStatistics stats = new DescriptiveStatistics(d);
        double dmax = Math.max(Math.abs(stats.getMax()), Math.abs(stats.getMin()))/scale;
        if (Double.isNaN(maxDDef_) || dmax > maxDDef_) {
            maxDDef_ = dmax;
        }
    }
    
    static{
        MAPPING.set("annualtotals.value", Double.class, source->source.getValue(BIAS));
        MAPPING.set("annualtotals", String.class, source->source.getDiagnostic(BIAS).name());
        MAPPING.set("definition.value", Double.class, source->source.getValue(DEF));
        MAPPING.set("definition", String.class, source->source.getDiagnostic(DEF).name());
    }
}
