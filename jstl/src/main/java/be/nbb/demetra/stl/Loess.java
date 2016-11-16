/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.stl;

import ec.demetra.realfunctions.RealFunction;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class Loess {

    protected final int degree;
    protected final int span;
    protected double wthreshold = 0.001;
    protected RealFunction wfn = x -> {
        double t = 1 - x * x * x;
        return t * t * t;
    };

    protected double[] weights;
    protected double wsum;

    public Loess(int span) {
        this.degree=1;
        this.span = span;
    }

    public Loess(int degree, int span) {
        this.degree=degree;        
        this.span = span;
    }

    private void computeWeights(int n, double xs, IntToDoubleFunction userWeights) {
        weights = new double[span];
        double h = Math.max(xs - 1, span - xs);
        if (span > n) {
            h += (span - n) * .5;
        }
        double h9 = (1 - wthreshold) * h;
        double h1 = wthreshold * h;
        wsum = 0;
        for (int i = 0; i < span; ++i) {
            double r = Math.abs(i - xs);
            if (r <= h9) {
                if (r < h1) {
                    weights[i] = 1;
                } else {
                    weights[i] = wfn.apply(r / h);
                }
                if (userWeights != null) {
                    weights[i] *= userWeights.applyAsDouble(i);
                }
                wsum += weights[i];
            }
        }

    }

    public boolean smooth(IntToDoubleFunction y, double xs, IntToDoubleFunction userWeights) {
        computeWeights(span, xs, userWeights);
        if (wsum == 0) {
            return false;
        }
        return true;
    }

    public boolean smooth(IntToDoubleFunction x, IntToDoubleFunction y, double xs, IntToDoubleFunction userWeights) {
        computeWeights(span, xs, userWeights);
        if (wsum == 0) {
            return false;
        }
        return true;
    }

    /**
     * @return the degree
     */
    public int getDegree() {
        return degree;
    }

    /**
     * @return the span
     */
    public int getSpan() {
        return span;
    }

    /**
     * @return the weights
     */
    public double[] getWeights() {
        return weights;
    }


    /**
     * @return the wthreshold
     */
    public double getWthreshold() {
        return wthreshold;
    }

    /**
     * @param wthreshold the wthreshold to set
     */
    public void setWthreshold(double wthreshold) {
        this.wthreshold = wthreshold;
    }

    /**
     * @return the wsum
     */
    public double getWsum() {
        return wsum;
    }

    /**
     * @return the wfn
     */
    public RealFunction getWfn() {
        return wfn;
    }

    /**
     * @param wfn the wfn to set
     */
    public void setWfn(RealFunction wfn) {
        this.wfn = wfn;
    }

}