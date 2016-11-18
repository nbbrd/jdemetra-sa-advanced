/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.stl;

import ec.demetra.realfunctions.RealFunction;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class LoessSpecification {

    private final int window;
    private final int degree;
    private final int jump;
    private final RealFunction loessfn;
    
    private static final RealFunction DEF_FN= x -> {
        double t = 1 - x * x * x;
        return t * t * t;
    };
    
    public static LoessSpecification defaultTrend(int period, int swindow){
        int win=(int)Math.ceil((1.5 * period) / (1 - 1.5 / swindow));
        if (win%2 == 0)
            ++win;
        return of(win);
    }

    public static LoessSpecification defaultLowPass(int period){
        int win=period+1;
        if (win%2 == 0)
            ++win;
        return of(win);
    }

    public static LoessSpecification defaultSeasonal(int swin){
        if (swin%2 == 0)
            ++swin;
        return of(swin, 0);
    }

    public static LoessSpecification of(int window) {
        if (window < 2 || window % 2 != 1) {
            throw new IllegalArgumentException("STL");
        }
        return new LoessSpecification(window, 1, (int) Math.ceil( .1 * window), null);
    }

    public static LoessSpecification of(int window, int degree) {
        if (window < 2 || window % 2 != 1) {
            throw new IllegalArgumentException("STL");
        }
        if (degree < 0 || degree > 1) {
            throw new IllegalArgumentException("STL");
        }
        return new LoessSpecification(window, degree, (int) Math.ceil( .1 * window), null);
    }

    public static LoessSpecification of(int window, int degree, int jump, RealFunction fn) {
        if (window < 2 || window % 2 != 1) {
            throw new IllegalArgumentException("STL");
        }
        if (degree < 0 || degree > 1) {
            throw new IllegalArgumentException("STL");
        }
        if (jump < 1) {
            throw new IllegalArgumentException("STL");
        }
        return new LoessSpecification(window, degree, jump, fn);
    }

    private LoessSpecification(int window, int degree, int jump, RealFunction fn) {
        this.window = window;
        this.degree = degree;
        this.jump = jump;
        this.loessfn=fn == null ? DEF_FN : fn;
    }

    /**
     * @return the window
     */
    public int getWindow() {
        return window;
    }

    /**
     * @return the degree
     */
    public int getDegree() {
        return degree;
    }

    /**
     * @return the jump
     */
    public int getJump() {
        return jump;
    }

    /**
     * @return the loessfn
     */
    public RealFunction getWeights() {
        return loessfn;
    }

}
