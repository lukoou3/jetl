package com.lk.jetl.format;


import com.lk.jetl.serialization.DeserializationSchema;
import com.lk.jetl.sql.types.StructType;

public interface DecodingFormat<T> {
    DeserializationSchema<T> createRuntimeDecoder(StructType dataType);
}
