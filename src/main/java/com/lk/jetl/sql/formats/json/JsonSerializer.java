package com.lk.jetl.sql.formats.json;

import com.alibaba.fastjson2.JSONWriter;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.types.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

public class JsonSerializer implements Serializable{
    private final StructType dataType;
    private final ValueWriter valueWriter;

    public JsonSerializer(StructType dataType) {
        this.dataType = dataType;
        this.valueWriter = makeWriter(dataType);
    }

    public byte[] serialize(Row data){
        try (JSONWriter writer = JSONWriter.ofUTF8()) {
            if (data == null) {
                writer.writeNull();
            } else {
                valueWriter.write(writer, data);
            }
            return writer.getBytes();
        }
    }

    private ValueWriter makeWriter(DataType dataType) {
        if (dataType instanceof StringType) {
            return JsonSerializer::writeString;
        }

        if (dataType instanceof IntegerType) {
            return JsonSerializer::writeInt;
        }

        if (dataType instanceof LongType) {
            return JsonSerializer::writeLong;
        }

        if (dataType instanceof FloatType) {
            return JsonSerializer::writeFloat;
        }

        if (dataType instanceof DoubleType) {
            return JsonSerializer::writeDouble;
        }

        if (dataType instanceof StructType) {
            ValueWriter[] fieldWriters = Arrays.stream(((StructType) dataType).fields).map( f -> this.makeWriter(f.dataType)).toArray(ValueWriter[]::new);
            String[] fields = Arrays.stream(((StructType) dataType).fields).map( f -> f.name).toArray(String[]::new);
            return (writer, obj) -> {
                writeObject(writer, obj, fields, fieldWriters);
            };
        }

        if (dataType instanceof ArrayType) {
            final ValueWriter elementWriter = this.makeWriter(((ArrayType) dataType).elementType);
            return (writer, obj) -> {
                writeArray(writer, obj, elementWriter);
            };
        }

        throw new UnsupportedOperationException("unsupported dataType: " + dataType);
    }

    static void writeString(JSONWriter writer, Object obj) {
        writer.writeString((String) obj);
    }

    static void writeInt(JSONWriter writer, Object obj){
        writer.writeInt32((Integer) obj);
    }

    static void writeLong(JSONWriter writer, Object obj) {
        writer.writeInt64((Long) obj);
    }

    static void writeFloat(JSONWriter writer, Object obj) {
        writer.writeFloat((Float) obj);
    }

    static void writeDouble(JSONWriter writer, Object obj){
        writer.writeDouble((Double) obj);
    }

    static void writeObject(JSONWriter writer, Object obj, String[] fields, ValueWriter[] fieldWriters){
        Row row = (Row) obj;
        writer.startObject();

        for (int i = 0; i < row.size(); i++) {
            if(row.isNullAt(i)){
                continue;
            }

            writer.writeName(fields[i]);
            writer.writeColon();
            fieldWriters[i].write(writer, row.get(i));
        }

        writer.endObject();
    }

    static void writeArray(JSONWriter writer, Object obj, ValueWriter elementWriter){
        Object[] objs = (Object[]) obj;
        writer.startArray();

        Object element;
        for (int i = 0; i < objs.length; i++) {
            if (i != 0) {
                writer.writeComma();
            }

            element = objs[i];
            if (element == null) {
                writer.writeNull();
                continue;
            }

            elementWriter.write(writer, element);
        }

        writer.endArray();
    }

    @FunctionalInterface
    public interface ValueWriter extends Serializable {
        void write(JSONWriter writer, Object obj);
    }
}
