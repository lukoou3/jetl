package com.lk.jetl.sql.formats.json;


import com.lk.jetl.format.DecodingFormat;
import com.lk.jetl.format.EncodingFormat;
import com.lk.jetl.serialization.DeserializationSchema;
import com.lk.jetl.serialization.SerializationSchema;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.factories.DecodingFormatFactory;
import com.lk.jetl.sql.factories.EncodingFormatFactory;
import com.lk.jetl.sql.types.StructType;

import java.util.Map;

public class JsonFormatFactory implements DecodingFormatFactory, EncodingFormatFactory {
    public static final String IDENTIFIER = "json";

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }


    @Override
    public DecodingFormat<Row> createDecodingFormat(Map<String, Object> options) {
        return new DecodingFormat<>() {
            @Override
            public DeserializationSchema<Row> createRuntimeDecoder(StructType dataType) {
                return new JsonRowDeserializationSchema(dataType, false);
            }
        };
    }

    @Override
    public EncodingFormat<Row> createEncodingFormat(Map<String, Object> options) {
        return new EncodingFormat<>() {
            @Override
            public SerializationSchema<Row> createRuntimeEncoder(StructType dataType) {
                return new JsonRowSerializationSchema(dataType);
            }
        };
    }
}
