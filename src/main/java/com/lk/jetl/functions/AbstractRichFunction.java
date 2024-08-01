package com.lk.jetl.functions;

import java.io.Serializable;

public abstract class AbstractRichFunction implements RichFunction, Serializable {
    @Override
    public void open() throws Exception {}
    @Override
    public void close() throws Exception {}
}
