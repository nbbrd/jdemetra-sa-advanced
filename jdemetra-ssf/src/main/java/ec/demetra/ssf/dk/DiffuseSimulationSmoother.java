/*
 * Copyright 2016 National Bank of Belgium
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
package ec.demetra.ssf.dk;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.random.IRandomNumberGenerator;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.ResultsRange;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.ISsfData;
import ec.demetra.ssf.univariate.ISsfMeasurement;
import ec.tstoolkit.random.MersenneTwister;

/**
 *
 * @author Jean Palate
 */
public class DiffuseSimulationSmoother {

    private static final Normal N = new Normal();
    private static final IRandomNumberGenerator RNG =new MersenneTwister(0);

    private static void fillRandoms(DataBlock u) {
        synchronized (N) {
            for (int i = 0; i < u.getLength(); ++i) {
                u.set(i, N.random(RNG));
            }
        }
    }

    private static double random() {
        synchronized (N) {
            return N.random(RNG);
        }
    }

    private static final double EPS = 1e-8;

    private Matrix LA;
    private final ISsf ssf;
    private final ISsfData data;
    private final ISsfDynamics dynamics;
    private final ISsfMeasurement measurement;
    private final Smoothing smoothing;
    private final double var;

    public DiffuseSimulationSmoother(ISsf ssf, ISsfData data) {
        this.ssf = ssf;
        dynamics = ssf.getDynamics();
        measurement = ssf.getMeasurement();
        this.data = data;
        initSsf();
        smoothing = new Smoothing();
        var = DkToolkit.var(data.getLength(), smoothing.frslts);
    }

    public Smoothing getReferenceSmoothing() {
        return smoothing;
    }

    public Simulation newSimulation() {
        return new Simulation();
    }

    private double lh(int pos) {
        return Math.sqrt(ssf.getMeasurement().errorVariance(pos));
    }

    private double h(int pos) {
        return ssf.getMeasurement().errorVariance(pos);
    }

    private void initSsf() {
        int dim = dynamics.getStateDim(), resdim = dynamics.getInnovationsDim();
        LA = Matrix.square(dim);
        dynamics.Pf0(LA.all());
        SymmetricMatrix.lcholesky(LA, EPS);

    }

    private void generateTransitionRandoms(int pos, DataBlock u) {
        fillRandoms(u);
    }

    private void generateMeasurementRandoms(DataBlock e) {
        fillRandoms(e);
        e.mul(lh(0));
    }

    private double generateMeasurementRandom(int pos) {
        double e = random();
        return e * lh(pos);
    }

    private void generateInitialState(DataBlock a) {
        fillRandoms(a);
        LowerTriangularMatrix.rmul(LA, a);
    }

    abstract class BaseSimulation {

        protected final IBaseDiffuseFilteringResults frslts;
        protected final DataBlockStorage smoothedInnovations;
        protected final DataBlock esm;
        protected DataBlockStorage smoothedStates;
        protected DataBlock a0;
        protected final int dim, resdim, n, nd;
        protected DataBlock R, Ri;

        protected abstract double getError(int pos);

        protected BaseSimulation(IBaseDiffuseFilteringResults frslts) {
            this.frslts = frslts;
            dim = dynamics.getStateDim();
            resdim = dynamics.getInnovationsDim();
            n = data.getLength();
            nd = frslts.getEndDiffusePosition();
            smoothedInnovations = new DataBlockStorage(resdim, n);
            if (measurement.hasErrors()) {
                esm = new DataBlock(n);
            } else {
                esm = null;
            }
        }

        protected void smooth() {
            R = new DataBlock(dim);
            Ri = new DataBlock(dim);
            // we reproduce here the usual iterations of the smoother
            doNormalSmoothing();
            doDiffuseSmoohing();
//            smoothedInnovations.rescale(Math.sqrt(var));
            computeInitialState();
        }

        private void doNormalSmoothing() {
            double e, v;
            DataBlock M = new DataBlock(dim);
            DataBlock U = new DataBlock(resdim);
            boolean missing;
            int pos = n;
            while (--pos >= nd) {
                // Get info
                e = getError(pos);
                v = frslts.errorVariance(pos);
                M.copy(frslts.M(pos));
                missing = !Double.isFinite(e);
                // Iterate R
                dynamics.XT(pos, R);
                if (!missing && e != 0) {
                    // RT
                    double c = (e - R.dot(M)) / v;
                    measurement.XpZd(pos, R, c);
                }
                // Computes esm, U
                if (esm != null) {
                    if (!missing) {
                        esm.set(pos, h(pos));
                    } else {
                        esm.set(pos, Double.NaN);
                    }
                }
                dynamics.XS(pos, R, U);
                smoothedInnovations.save(pos, U);
            }
        }

        private void doDiffuseSmoohing() {
            double e, f, fi, c;
            DataBlock C = new DataBlock(dim), Ci = new DataBlock(dim);
            DataBlock U = new DataBlock(resdim);
            boolean missing;
            int pos = nd;
            while (--pos >= 0) {
                // Get info
                e = getError(pos);
                f = frslts.errorVariance(pos);
                fi = frslts.diffuseNorm2(pos);
                C.copy(frslts.M(pos));
                if (fi != 0) {
                    Ci.copy(frslts.Mi(pos));
                    Ci.mul(1 / fi);
                    C.addAY(-f, Ci);
                    C.mul(1 / fi);
                } else if (f != 0) {
                    C.mul(1 / f);
                    Ci.set(0);
                }
                missing = !Double.isFinite(e);
                // Iterate R
                dynamics.XT(pos, R);
                dynamics.XT(pos, Ri);
                if (fi == 0) {
                    if (!missing && f != 0) {
                        c = e / f - R.dot(C);
                        measurement.XpZd(pos, R, c);
                    }
                } else if (!missing && f != 0) {
                    c = -Ri.dot(Ci);
                    double ci = e / fi + c - R.dot(C);
                    measurement.XpZd(pos, Ri, ci);
                    double cf = -R.dot(Ci);
                    measurement.XpZd(pos, R, cf);
                }
                // Computes esm, U
                if (esm != null) {
                    if (!missing) {
                        esm.set(pos, h(pos));
                    } else {
                        esm.set(pos, Double.NaN);
                    }
                }
                dynamics.XS(pos, R, U);
                smoothedInnovations.save(pos, U);
            }
        }

        private void computeInitialState() {
            // initial state
            a0 = new DataBlock(dim);
            Matrix Pf0 = Matrix.square(dim);
            dynamics.a0(a0);
            dynamics.Pf0(Pf0.all());
            // stationary initialization
            a0.addProduct(R, Pf0.columns());

            // non stationary initialisation
            Matrix Pi0 = Matrix.square(dim);
            dynamics.Pi0(Pi0.all());
            a0.addProduct(Ri, Pi0.columns());
        }

        public DataBlock getSmoothedInnovations(int pos) {
            return smoothedInnovations.block(pos);
        }

        public DataBlock getSmoothedState(int pos) {
            if (smoothedStates == null) {
                generatesmoothedStates();
            }
            return smoothedStates.block(pos);
        }

        public DataBlockStorage getSmoothedInnovations() {
            return smoothedInnovations;
        }

        public DataBlockStorage getSmoothedStates() {
            if (smoothedStates == null) {
                generatesmoothedStates();
            }
            return smoothedStates;
        }

        private void generatesmoothedStates() {
            smoothedStates = new DataBlockStorage(dim, n);
            smoothedStates.save(0, a0);
            int cur = 1;
            DataBlock a = a0.deepClone();
            do {
                // next: a(t+1) = T a(t) + S*r(t)
                dynamics.TX(cur, a);
                dynamics.addSU(cur, a, smoothedInnovations.block(cur));
                smoothedStates.save(cur++, a);
            } while (cur < n);
        }
    }

    public class Smoothing extends BaseSimulation {

        Smoothing() {
            super(DkToolkit.sqrtFilter(ssf, data, false));
            smooth();
        }

        @Override
        protected double getError(int pos) {
            return frslts.error(pos);
        }

    }

    public class Simulation extends BaseSimulation {

        public Simulation() {
            super(smoothing.frslts);
            boolean err = measurement.hasErrors();
            states = new DataBlockStorage(dim, n);
            transitionInnovations = new DataBlockStorage(resdim, n);
            if (err) {
                measurementErrors = new double[n];
                generateMeasurementRandoms(new DataBlock(measurementErrors));
            } else {
                measurementErrors = null;
            }
            simulatedData = new double[n];
            generateData();
            filter();
            smooth();
        }

        final DataBlockStorage states;
        private DataBlockStorage simulatedStates, simulatedInnovations;
        final DataBlockStorage transitionInnovations;
        final double[] measurementErrors;
        private double[] ferrors;
        private final double[] simulatedData;

        private void generateData() {
            double std = Math.sqrt(var);
            DataBlock a0f = new DataBlock(dim);
            generateInitialState(a0f);
            a0f.mul(std);
            DataBlock a = new DataBlock(dim);
            dynamics.a0(a);
            a.add(a0f);
            states.save(0, a);
            simulatedData[0] = measurement.ZX(0, a);
            if (measurementErrors != null) {
                simulatedData[0] += measurementErrors[0] * std;
            }
            // a0 = a(1|0) -> y[1) = Z*a[1|0) + e(1)
            // a(2|1) = T a(1|0) + S * q(1)...
            DataBlock q = new DataBlock(resdim);
            for (int i = 1; i < simulatedData.length; ++i) {
                generateTransitionRandoms(i - 1, q);
                q.mul(std);
                transitionInnovations.save(i - 1, q);
                dynamics.TX(i, a);
                dynamics.addSU(i, a, q);
                states.save(i, a);
                simulatedData[i] = measurement.ZX(i, a);
                if (measurementErrors != null) {
                    simulatedData[i] += measurementErrors[i] * std;
                }
            }
        }

        private void filter() {
            DkFilter f = new DkFilter(ssf, frslts, new ResultsRange(0, n));
            f.setNormalized(false);
            ferrors = simulatedData.clone();
            f.filter(new DataBlock(ferrors));
        }

        /**
         * @return the simulatedData
         */
        public double[] getSimulatedData() {
            return simulatedData;
        }

        public DataBlockStorage getGeneratedStates() {
            return states;
        }

        @Override
        protected double getError(int pos) {
            return ferrors[pos];
        }

        public ReadDataBlock getErrors() {
            return new ReadDataBlock(ferrors);
        }

        public DataBlockStorage getSimulatedStates() {
            if (simulatedStates == null) {
                computeSimulatedStates();
            }
            return simulatedStates;
        }

        private void computeSimulatedStates() {
            simulatedStates = new DataBlockStorage(dim, n);
            DataBlockStorage sm = smoothing.getSmoothedStates();
            DataBlockStorage ssm = getSmoothedStates();
            DataBlock a = new DataBlock(dim);
            for (int i = 0; i < n; ++i) {
                a.copy(sm.block(i));
                a.sub(ssm.block(i));
                a.add(states.block(i));
                simulatedStates.save(i, a);
            }
        }

        public DataBlockStorage getSimulatedInnovations() {
            if (simulatedInnovations == null) {
                computeSimulatedInnovations();
            }
            return simulatedInnovations;
        }

        private void computeSimulatedInnovations() {
            simulatedInnovations = new DataBlockStorage(resdim, n);
            DataBlockStorage sm = smoothing.getSmoothedInnovations();
            DataBlockStorage ssm = getSmoothedInnovations();
            DataBlock u = new DataBlock(dim);
            for (int i = 0; i < n; ++i) {
                u.copy(sm.block(i));
                u.sub(ssm.block(i));
                u.add(transitionInnovations.block(i));
                simulatedInnovations.save(i, u);
            }
        }
    }
}
