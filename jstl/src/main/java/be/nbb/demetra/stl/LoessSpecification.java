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
    private final RealFunction loessfn = x -> {
        double t = 1 - x * x * x;
        return t * t * t;
    };

    public static LoessSpecification of(int window) {
        if (window < 2 || window % 2 != 1) {
            throw new IllegalArgumentException("STL");
        }
        return new LoessSpecification(window, 1, (int) Math.ceil( .1 * window));
    }

    public static LoessSpecification of(int window, int degree) {
        if (window < 2 || window % 2 != 1) {
            throw new IllegalArgumentException("STL");
        }
        if (degree < 0 || degree > 1) {
            throw new IllegalArgumentException("STL");
        }
        return new LoessSpecification(window, degree, (int) Math.ceil( .1 * window));
    }

    public static LoessSpecification of(int window, int degree, int jump) {
        if (window < 2 || window % 2 != 1) {
            throw new IllegalArgumentException("STL");
        }
        if (degree < 0 || degree > 1) {
            throw new IllegalArgumentException("STL");
        }
        if (jump < 1) {
            throw new IllegalArgumentException("STL");
        }
        return new LoessSpecification(window, degree, jump);
    }

    private LoessSpecification(int window, int degree, int jump) {
        this.window = window;
        this.degree = degree;
        this.jump = jump;
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
