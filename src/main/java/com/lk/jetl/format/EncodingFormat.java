package com.lk.jetl.format;


import com.lk.jetl.serialization.SerializationSchema;
import com.lk.jetl.sql.types.StructType;

public interface EncodingFormat<T> {
    SerializationSchema<T> createRuntimeEncoder(StructType dataType);

}
