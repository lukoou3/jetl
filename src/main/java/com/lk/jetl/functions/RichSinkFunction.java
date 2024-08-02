package com.lk.jetl.functions;

public abstract class RichSinkFunction<T> extends AbstractRichFunction
        implements SinkFunction<T> {
    @Override
    public abstract void invoke(T value);
}
