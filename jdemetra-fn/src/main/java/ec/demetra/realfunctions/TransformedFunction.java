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
package ec.demetra.realfunctions;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TransformedFunction implements IFunction {

    class Instance implements IFunctionInstance {

        private final IFunctionInstance fx_;

        Instance(IFunctionInstance fx) {
            fx_ = fx;
        }

        @Override
        public IReadDataBlock getParameters() {
            return fx_.getParameters();
        }

        @Override
        public double getValue() {
            return t_.f(fx_.getValue());
        }

        @Override
        public IFunction getFunction() {
            return TransformedFunction.this;
        }

        @Override
        public IFunctionDerivatives getDerivatives() {
            return new Derivatives(fx_.getDerivatives(), getValue());
        }

    }

    class Derivatives implements IFunctionDerivatives {

        private final IFunctionDerivatives dfx_;
        private final double fx_;

        Derivatives(IFunctionDerivatives dfx, double fx) {
            dfx_ = dfx;
            fx_ = fx;
        }

        /**
         * F(y) = t(f(y)) dF/dyi = t'(f(y))*df/dyi
         *
         * @return
         */
        @Override
        public IReadDataBlock getGradient() {
            DataBlock g = new DataBlock(dfx_.getGradient());
            double dt = t_.df(fx_);
            g.mul(dt);
            return g;
        }

        /**
         * F(y) = t(f(y)) dF/dyi = t'(f(y)*df/dyi d2F/dyi dyj =
         * t''(f(y))*df/dyi*df/dyj + t'(f(y))*d2f/dyi dyj
         *
         * @return
         */
        @Override
        public void getHessian(SubMatrix H) {
            int n = getDomain().getDim();
            Matrix h = Matrix.square(n);
            dfx_.getHessian(h.all());
            IReadDataBlock grad = dfx_.getGradient();
            double dt = t_.df(fx_), d2t = t_.d2f(fx_);
            h.mul(dt);
            h.addXaXt(d2t, new DataBlock(grad));
            H.copy(h.all());
        }
    }

    /**
     * f(x) = a + b*x
     *
     * @param a
     * @param b
     * @return
     */
    public static ITransformation linearTransformation(final double a, final double b) {
        return new ITransformation() {

            @Override
            public double f(double x) {
                return a + b * x;
            }

            @Override
            public double df(double x) {
                return b;
            }

            @Override
            public double d2f(double x) {
                return 0;
            }
        };
    }

    public interface ITransformation {

        double f(double x);

        double df(double x);

        double d2f(double x);
    }

    private final ITransformation t_;
    private final IFunction f_;

    public TransformedFunction(IFunction fn, ITransformation t) {
        f_ = fn;
        t_ = t;
    }

    @Override
    public IFunctionInstance evaluate(IReadDataBlock parameters) {
        return new Instance(f_.evaluate(parameters));
    }

    @Override
    public IParametersDomain getDomain() {
        return f_.getDomain();
    }

}
