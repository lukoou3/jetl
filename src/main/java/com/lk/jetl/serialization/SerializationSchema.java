package com.lk.jetl.serialization;

import java.io.Serializable;

public interface SerializationSchema<T> extends Serializable {

    default void open() throws Exception{}

    byte[] serialize(T value);

    default void close() throws Exception{}
}
