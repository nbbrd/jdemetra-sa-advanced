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
package ec.demetra.data;

import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
public class DoubleArrayReader implements IDoubleArrayReader{
    private final int n;
    private final IntToDoubleFunction fn;
    
    public DoubleArrayReader(final int n, final IntToDoubleFunction fn){
        this.n=n;
        this.fn=fn;
    }

    @Override
    public double get(int idx) {
        return fn.applyAsDouble(idx);
    }

    @Override
    public int getLength() {
        return n;
    }

    @Override
    public IDoubleArrayReader rextract(int start, int length) {
        return new DoubleArrayReader(length, i->fn.applyAsDouble(start+i));
    }
}
