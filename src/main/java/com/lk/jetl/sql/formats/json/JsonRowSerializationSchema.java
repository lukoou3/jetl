package com.lk.jetl.sql.formats.json;

import com.lk.jetl.serialization.SerializationSchema;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.types.StructType;

public class JsonRowSerializationSchema implements SerializationSchema<Row> {
    private final StructType dataType;
    private final JsonSerializer serializer;

    public JsonRowSerializationSchema(StructType dataType) {
        this.dataType = dataType;
        this.serializer = new JsonSerializer(dataType);
    }

    @Override
    public byte[] serialize(Row value) {
        return serializer.serialize(value);
    }
}
