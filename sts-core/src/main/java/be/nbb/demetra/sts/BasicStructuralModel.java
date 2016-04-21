/*
 * Copyright 2015 National Bank of Belgium
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
/*
 */
package be.nbb.demetra.sts;

import ec.demetra.ssf.implementations.structural.Component;
import ec.demetra.ssf.implementations.structural.ComponentUse;
import ec.demetra.ssf.implementations.structural.SeasonalModel;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.maths.linearfilters.SymmetricFrequencyResponseDecomposer;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.ucarima.UcarimaModel;

/**
 *
 * @author Jean Palate
 */
public class BasicStructuralModel {

    private static ComponentUse getUse(double var) {
        if (var < 0) {
            return ComponentUse.Unused;
        } else if (var == 0) {
            return ComponentUse.Fixed;
        } else {
            return ComponentUse.Free;
        }
    }

    private static double getVar(ComponentUse use) {
        if (use == ComponentUse.Unused) {
            return -1;
        } else if (use == ComponentUse.Fixed) {
            return 0;
        } else {
            return 1;
        }
    }

    private static double getVar(SeasonalModel use) {
        if (use == SeasonalModel.Unused) {
            return -1;
        } else if (use == SeasonalModel.Fixed) {
            return 0;
        } else {
            return 1;
        }
    }

    private SeasonalModel getSeas() {
        if (seasVar < 0) {
            return SeasonalModel.Unused;
        } else if (seasVar == 0) {
            return SeasonalModel.Fixed;
        } else {
            return seasModel;
        }
    }

    /**
     *
     * @return
     */
    public ModelSpecification getSpecification() {
        ModelSpecification spec = new ModelSpecification();
        spec.setSeasonalModel(getSeas());
        spec.useLevel(getUse(lVar));
        spec.useSlope(getUse(sVar));
        spec.useCycle(getUse(cVar));
        spec.useNoise(nVar <= 0 ? ComponentUse.Unused : ComponentUse.Free);
        return spec;
    }

    private static void svar(int freq, SubMatrix O) {
        int n = freq - 1;
        Matrix H = new Matrix(freq, n);
        // should be improved
        for (int i = 0; i < freq; ++i) {
            double z = 2 * Math.PI * (i + 1) / freq;
            for (int j = 0; j < n / 2; ++j) {
                H.set(i, 2 * j, Math.cos((j + 1) * z));
                H.set(i, 2 * j + 1, Math.sin((j + 1) * z));
            }
            if (n % 2 == 1) {
                H.set(i, n - 1, Math.cos((freq / 2) * z));
            }
        }

        SymmetricMatrix.XXt(H.all(), O);
    }

    /**
     *
     */
    final int freq;
    private int[] m_cmps;
    private Matrix m_tsvar;
    double lVar, sVar, seasVar, cVar, nVar;
    double cDump, cPeriod;
    double ccos, csin;
    SeasonalModel seasModel;

    /**
     *
     * @param spec
     * @param freq
     */
    public BasicStructuralModel(ModelSpecification spec, int freq) {
        this.freq = freq;
        seasModel = spec.getSeasonalModel();
        seasVar = getVar(seasModel);
        lVar = getVar(spec.lUse);
        sVar = getVar(spec.sUse);
        cVar = getVar(spec.cUse);
        nVar = getVar(spec.nUse);
        if (spec.cUse != ComponentUse.Unused) {
            cycle(.5, freq * 2);
        }
    }

    public UcarimaModel computeReducedModel(boolean normalized) {
        UcarimaModel ucm = new UcarimaModel();
        // trend.
        BackFilter D = BackFilter.D1;
        ArimaModel cycle = null, trend = null;
        if (cVar >= 0) {
            double[] ar = new double[]{1, -2 * ccos, cDump * cDump};
            double t = 1 + cDump * cDump;
            double[] ma = new double[]{t, -ccos};
            SymmetricFrequencyResponseDecomposer decomposer = new SymmetricFrequencyResponseDecomposer();
            decomposer.decompose(SymmetricFilter.of(ma));
            cycle = new ArimaModel(BackFilter.of(ar), null, decomposer.getBFilter(), cVar * decomposer.getFactor());
        }
        if (lVar >= 0 && sVar >= 0) {
            if (lVar == 0 && sVar == 0) {
                trend = new ArimaModel(null, D.times(D), null, 0);
            } else if (lVar == 0) {
                trend = new ArimaModel(null, D.times(D), null, sVar);
            } else if (sVar == 0) {
                trend = new ArimaModel(null, D.times(D), D, lVar);

            } else {
                ArimaModel ml = new ArimaModel(null, null, D, lVar);
                ml = ml.plus(sVar);
                trend = new ArimaModel(null, D.times(D), ml.sma());
            }
        } else if (lVar >= 0) {// sVar < 0
            trend = new ArimaModel(null, D, null, lVar);
        }
        if (trend != null) {
            if (cycle != null) {
                ucm.addComponent(trend.plus(cycle, false));
            } else {
                ucm.addComponent(trend);
            }
        } else {
            if (cycle != null) {
                ucm.addComponent(cycle);
            } else {
                ucm.addComponent(new ArimaModel(null, null, null, 0));
            }
        }

        //seasonal
        if (seasVar >= 0) {
            BackFilter S = new BackFilter(UnitRoots.S(freq, 1));
            if (seasVar > 0) {
                SymmetricFilter sma;
                if (seasModel != SeasonalModel.Dummy) {
                    // ma is the first row of the v/c innovations
                    Matrix O = new Matrix(freq, freq);
                    switch (seasModel) {
                        case Crude:
                            int f = freq - 1;
                            O.set(1);
                            O.row(0).mul(-f);
                            O.column(0).mul(-f);
                            break;

                        case HarrisonStevens:
                            O.set(-1.0 / freq);
                            O.diagonal().add(1);
                            break;
                        case Trigonometric:
                            svar(freq, O.all());
                            break;
                        default:
                            break;
                    }
                    double[] w = new double[freq - 1];
                    for (int i = 0; i < freq - 1; ++i) {
                        for (int j = i; j < freq - 1; ++j) {
                            SubMatrix s = O.subMatrix(0, 1 + j - i, 0, 1 + j);
                            w[i] += s.sum();
                        }
                    }
                    sma = SymmetricFilter.of(w);
                    sma = sma.times(seasVar);
                } else {
                    sma = SymmetricFilter.of(new double[]{seasVar});
                }
                ucm.addComponent(new ArimaModel(null, S, sma));
            } else {
                ucm.addComponent(new ArimaModel(null, S, null, 0));

            }
        } else {
            ucm.addComponent(new ArimaModel(null, null, null, 0));
        }

        if (nVar > 0) {
            ucm.addComponent(new ArimaModel(null, null, null, nVar));
        } else {
            ucm.addComponent(new ArimaModel(null, null, null, 0));
        }
        if (normalized) {
            ucm.normalize();
        }
        return ucm;
    }

    /**
     *
     * @param factor
     */
    public void scaleVariances(double factor) {
        if (lVar > 0) {
            lVar *= factor;
        }
        if (cVar > 0) {
            cVar *= factor;
        }
        if (sVar > 0) {
            sVar *= factor;
        }
        if (seasVar > 0) {
            seasVar *= factor;
        }
        if (nVar > 0) {
            nVar *= factor;
        }
        if (factor == 0) {
            m_cmps = null;
        }
    }

    /**
     *
     * @param cmp
     * @param var
     */
    public void setVariance(Component cmp, double var) {
        if (var == 0) {
            m_cmps = null;
        }
        switch (cmp) {
            case Noise:
                if (nVar > 0) {
                    nVar = var;
                }
                return;
            case Cycle:
                if (cVar >= 0) {
                    cVar = var;
                }
                return;
            case Level:
                if (lVar >= 0) {
                    lVar = var;
                }
                return;
            case Slope:
                if (sVar >= 0) {
                    sVar = var;
                }
                return;
            case Seasonal:
                if (seasVar >= 0) {
                    seasVar = var;
                    if (var == 0) {
                        seasModel = SeasonalModel.Fixed;
                    }
                }
        }
    }

    /**
     *
     * @param val
     * @return
     */
    public Component fixMaxVariance(double val) {
        Component max = getMaxVariance();
        if (max != Component.Undefined) {
            double vmax = getVariance(max);
            if (vmax != val) {
                scaleVariances(val / vmax);
            }
        }
        return max;
    }

    /**
     *
     * @param eps
     * @return
     */
    public boolean fixSmallVariance(double eps) {
        Component min = getMinVariance();
        if (min != Component.Undefined && getVariance(min) < eps) {
            setVariance(min, 0);
            return true;
        } else {
            return false;
        }
    }
    
    /**
     *
     * @return
     */
    public Component getMaxVariance() {
        Component cmp = Component.Undefined;
        double vmax = 0;
        if (lVar > vmax) {
            vmax = lVar;
            cmp = Component.Level;
        }
        if (sVar > vmax) {
            vmax = sVar;
            cmp = Component.Slope;
        }
        if (seasVar > vmax) {
            vmax = seasVar;
            cmp = Component.Seasonal;
        }
        if (cVar > vmax) {
            vmax = cVar;
            cmp = Component.Cycle;
        }
        if (nVar > vmax) {
            cmp = Component.Noise;
        }
        return cmp;
    }

    /**
     *
     * @return
     */
    public Component getMinVariance() {
        Component cmp = Component.Undefined;
        double vmin = Double.MAX_VALUE;
        if (lVar > 0 && lVar < vmin) {
            vmin = lVar;
            cmp = Component.Level;
        }
        if (sVar > 0 && sVar < vmin) {
            vmin = sVar;
            cmp = Component.Slope;
        }
        if (seasVar > 0 && seasVar < vmin) {
            vmin = seasVar;
            cmp = Component.Seasonal;
        }
        if (cVar > 0 && cVar < vmin) {
            vmin = cVar;
            cmp = Component.Cycle;
        }
        if (nVar > 0 && nVar < vmin) {
            cmp = Component.Noise;
        }
        return cmp;
    }

    public int getComponentsCount() {
        int n = 0;
        if (nVar > 0) {
            ++n;
        }
        if (cVar >= 0) {
            ++n;
        }
        if (lVar >= 0) {
            ++n;
            if (sVar >= 0) {
                ++n;
            }
        }
        if (seasVar >= 0) {
            ++n;
        }
        return n;
    }

    /**
     *
     * @return
     */
    public Component[] getComponents() {
        Component[] cmp = new Component[getComponentsCount()];
        int idx = 0;
        if (nVar > 0) {
            cmp[idx++] = Component.Noise;
        }
        if (cVar >= 0) {
            cmp[idx++] = Component.Cycle;
        }
        if (lVar >= 0) {
            cmp[idx++] = Component.Level;
            if (sVar >= 0) {
                cmp[idx++] = Component.Slope;
            }
        }
        if (seasVar >= 0) {
            cmp[idx] = Component.Seasonal;
        }

        return cmp;
    }

    /**
     *
     * @param cmp
     * @param var
     */
    public double getVariance(Component cmp) {
        switch (cmp) {
            case Noise:
                return nVar;
            case Cycle:
                return cVar;
            case Level:
                return lVar;
            case Slope:
                return sVar;
            case Seasonal:
                return seasVar;
            default:
                return -1;
        }
    }

    public void setCycle(double cro, double cperiod) {
        cycle(cro, cperiod);
    }

    private void cycle(double cro, double cperiod) {
        cDump = cro;
        cPeriod = cperiod;
        double q = Math.PI * 2 / cperiod;
        ccos = cDump * Math.cos(q);
        csin = cDump * Math.sin(q);
    }

    public double getCyclicalDumpingFactor() {
        return cDump;
    }

    public double getCyclicalPeriod() {
        return cPeriod;
    }

    public int getFrequency() {
        return freq;
    }

}
