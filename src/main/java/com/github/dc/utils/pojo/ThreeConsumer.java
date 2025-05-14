package com.github.dc.utils.pojo;

import java.util.Objects;

@FunctionalInterface
public interface ThreeConsumer<F, S, T> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param f the first input argument
     * @param s the second input argument
     * @param t the three input argument
     */
    void accept(F f, S s, T t);

    /**
     * Returns a composed {@code ThreeConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code ThreeConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default ThreeConsumer<F, S, T> andThen(ThreeConsumer<? super F, ? super S, ? super T> after) {
        Objects.requireNonNull(after);

        return (f, s, t) -> {
            accept(f, s, t);
            after.accept(f, s, t);
        };
    }
}
