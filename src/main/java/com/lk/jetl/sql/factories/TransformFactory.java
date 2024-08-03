package com.lk.jetl.sql.factories;

import com.lk.jetl.sql.connector.TransformProvider;
import com.lk.jetl.sql.types.StructType;

import java.util.Map;

public interface TransformFactory extends Factory {
    TransformProvider getTransformProvider(StructType[] dependencies, Map<String, Object> options);
}
