package com.lk.jetl.serialization;

public class SimpleBinarySchema implements DeserializationSchema<byte[]>, SerializationSchema<byte[]> {
    @Override
    public byte[] deserialize(byte[] message) {
        return message;
    }

    @Override
    public byte[] serialize(byte[] value) {
        return value;
    }

    @Override
    public void open() throws Exception {}

    @Override
    public void close() throws Exception {}
}
