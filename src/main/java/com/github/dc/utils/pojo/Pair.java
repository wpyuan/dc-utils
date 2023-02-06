package com.github.dc.utils.pojo;

import lombok.EqualsAndHashCode;

/**
 * @author PeiYuan
 */
@EqualsAndHashCode
public class Pair<K, V> {
    private K k;
    private V v;

    public Pair(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public K getFirst() {
        return this.k;
    }

    public V getSecond() {
        return this.v;
    }

    public static <K, V> Pair<K, V> of(K k, V v) {
        return new Pair<K, V>(k, v);
    }
}
