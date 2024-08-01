package com.lk.jetl.functions;

import java.io.Serializable;

public interface FilterFunction<T> extends Serializable {
    boolean filter(T value);
}
