package com.lk.jetl.functions;

public abstract class RichFilterFunction<T> extends AbstractRichFunction
        implements FilterFunction<T> {
    @Override
    public abstract boolean filter(T value);
}
