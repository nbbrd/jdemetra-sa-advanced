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

import ec.demetra.eco.IConcentratedLikelihood;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
public class DkConcentratedLikelihood extends DkLikelihood implements IConcentratedLikelihood {

    private DataBlock coeff;
    private Matrix var;

    @Override
    public IReadDataBlock getCoefficients() {
        return coeff == null ? DataBlock.EMPTY : coeff;
    }

    @Override
    public Matrix getCoefficientsCovariance() {
        return var;
    }

    public void set(IReadDataBlock coeff, Matrix var) {
        this.var = var;
        this.coeff = DataBlock.of(coeff);
    }

}
