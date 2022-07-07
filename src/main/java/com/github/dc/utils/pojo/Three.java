package com.github.dc.utils.pojo;

/**
 * @author PeiYuan
 */
public class Three<F, S, T> {
    private F f;
    private S s;
    private T t;

    public Three(F f, S s, T t) {
        this.f = f;
        this.s = s;
        this.t = t;
    }

    public F getFirst() {
        return this.f;
    }

    public S getSecond() {
        return this.s;
    }

    public T getThree() {
        return this.t;
    }
}
