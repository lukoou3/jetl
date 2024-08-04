package com.lk.jetl.serialization;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.google.common.base.Preconditions.checkNotNull;

public class SimpleStringSchema implements DeserializationSchema<String>, SerializationSchema<String>{
    private transient Charset charset;

    public SimpleStringSchema() {
        this(StandardCharsets.UTF_8);
    }

    public SimpleStringSchema(Charset charset) {
        this.charset = checkNotNull(charset);
    }

    @Override
    public String deserialize(byte[] message) {
        return new String(message, charset);
    }

    @Override
    public byte[] serialize(String value) {
        return value.getBytes(charset);
    }

    @Override
    public void open() throws Exception {}

    @Override
    public void close() throws Exception {}
}
