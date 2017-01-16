/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.maths.matrices;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public final class Matrix {
    
    public static Matrix square(int n){
        return new Matrix(n,n);
    }
    
    public static Matrix of(double[] data, int nrows){
        if (data.length%nrows != 0)
            throw new IllegalArgumentException("Illegal matrix dimensions");
        int ncols=data.length/nrows;
        return new Matrix(data, nrows, ncols);
    }
    
    public SubMatrix all(){
        return new SubMatrix(data, 0, nrows, ncols, 1, nrows);
    }

    /**
     * Creates a sub-matrix from this matrix
     *
     * @param r0 First row (included). 0-based.
     * @param r1 Last row (excluded). 0-based.
     * @param c0 First column (included). 0-based.
     * @param c1 Last column (excluded). 0-based.
     * @return
     */
    public SubMatrix subMatrix(final int r0, final int r1, final int c0,
            final int c1) {
        int nr, nc;
        if (r1 < 0) {
            nr = nrows - r0;
        } else {
            nr = r1 - r0;
        }
        if (c1 < 0) {
            nc = ncols - c0;
        } else {
            nc = c1 - c0;
        }
        return new SubMatrix(data, r0 + c0 * nrows, nr, nc, 1,
                nrows);
    }
    
    
    
    private Matrix(int nr, int nc){
        nrows=nr;
        ncols=nc;
        data=new double[nrows*ncols];
    }
    
    private Matrix(double[] data, int nr, int nc){
        nrows=nr;
        ncols=nc;
        this.data=data;
    }

    private final double[] data;
    private final int nrows, ncols;
    
}
