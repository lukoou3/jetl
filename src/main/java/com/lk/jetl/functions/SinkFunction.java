package com.lk.jetl.functions;

import java.io.Serializable;

public interface SinkFunction<T> extends Serializable {
    void invoke(T value) throws Exception;
}
