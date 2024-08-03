package com.lk.jetl.serialization;

import java.io.Serializable;

public interface DeserializationSchema<T> extends Serializable {

    default void open() throws Exception{}

    T deserialize(byte[] message);

    default void close() throws Exception{}
}
