package com.lk.jetl.functions;

public abstract class RichMapFunction<A, B> extends AbstractRichFunction
        implements MapFunction<A, B> {
    @Override
    public abstract B map(A value);
}
