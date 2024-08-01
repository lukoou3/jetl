package com.lk.jetl.functions;

import java.io.Serializable;

@FunctionalInterface
public interface MapFunction<A, B> extends Serializable {
    B map(A value);
}
