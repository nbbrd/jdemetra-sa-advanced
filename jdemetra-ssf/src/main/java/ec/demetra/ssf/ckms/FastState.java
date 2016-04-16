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
package ec.demetra.ssf.ckms;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FastState {

    /**
     *
     */
    final DataBlock a, l, k;
    double f;

    /**
     * @return the a
     */
    public final DataBlock a() {
        return a;
    }

    public final DataBlock k() {
        return k;
    }

    public final DataBlock l() {
        return l;
    }
    /**
     *
     * @param n
    */
    public FastState(final int n) {
        a = new DataBlock(n);
        l = new DataBlock(n);
        k = new DataBlock(n);
    }

    public final int getDim(){
        return a.getLength();
    }

    /**
     * @return the f
     */
    public double getF() {
        return f;
    }

    /**
     * @param f the f to set
     */
    public void setF(double f) {
        this.f = f;
    }
    
   
}
