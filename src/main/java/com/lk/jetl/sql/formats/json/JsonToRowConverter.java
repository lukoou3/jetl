package com.lk.jetl.sql.formats.json;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.lk.jetl.sql.GenericRow;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class JsonToRowConverter implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(JsonToRowConverter.class);
    private final StructType dataType;
    private final boolean ignoreParseErrors;
    private final ValueConverter[] fieldConverters;
    private Map<String, Integer> nameIdxes;

    public JsonToRowConverter(StructType dataType, boolean ignoreParseErrors) {
        this.dataType = dataType;
        this.ignoreParseErrors = ignoreParseErrors;
        this.fieldConverters = Arrays.stream(dataType.fields).map(f -> this.makeConverter(f.dataType)).toArray(ValueConverter[]::new);
        this.nameIdxes = geneNameIdxes(dataType);
    }


    public Row convert(String message) {
        try (JSONReader reader = JSONReader.of(message)) {
            if (!reader.nextIfMatch('{')) {
                throw new JSONException("object not start with {");
            }

            Row row = new GenericRow(fieldConverters.length);

            while (!reader.nextIfMatch('}')) {
                String fieldName = reader.readFieldName();
                Integer index = nameIdxes.get(fieldName);
                if (index != null) {
                    row.update(index, fieldConverters[index].convert(reader));
                } else {
                    reader.skipValue();
                }
            }

            return row;
        } catch (Exception e) {
            LOG.error(String.format("JSON Parse Errors:%s", message), e);
            if (ignoreParseErrors) {
                return null;
            }

            throw new UnsupportedOperationException("Unsupported type or invalid data format:" + message);
        }
    }

    private ValueConverter makeConverter(DataType dataType) {
        if (dataType instanceof StringType) {
            return this::convertToString;
        }

        if (dataType instanceof IntegerType) {
            return this::convertToInteger;
        }

        if (dataType instanceof LongType) {
            return this::convertToLong;
        }

        if (dataType instanceof FloatType) {
            return this::convertToFloat;
        }

        if (dataType instanceof DoubleType) {
            return this::convertToDouble;
        }

        if (dataType instanceof StructType) {
            final ValueConverter[] fieldConverterArray = Arrays.stream(((StructType) dataType).fields).map(f -> this.makeConverter(f.dataType)).toArray(ValueConverter[]::new);
            Map<String, Integer> nameIdxMap = geneNameIdxes((StructType) dataType);
            return reader -> {
                if (reader.nextIfNull()) {
                    return null;
                }

                Row row = new GenericRow(fieldConverters.length);
                // 字符串，当成json对象解析
                if (reader.isString()) {
                    String str = reader.readString();

                    try (JSONReader r = JSONReader.of(str)) {
                        if (!r.nextIfMatch('{')) {
                            return row;
                        }

                        while (!r.nextIfMatch('}')) {
                            String fieldName = r.readFieldName();
                            Integer index = nameIdxMap.get(fieldName);
                            if (index != null) {
                                row.update(index, fieldConverterArray[index].convert(reader));
                            } else {
                                reader.skipValue();
                            }
                        }

                        return row;
                    }
                }

                if (!reader.nextIfMatch('{')) {
                    return row;
                }

                while (!reader.nextIfMatch('}')) {
                    String fieldName = reader.readFieldName();
                    Integer index = nameIdxMap.get(fieldName);
                    if (index != null) {
                        row.update(index, fieldConverterArray[index].convert(reader));
                    } else {
                        reader.skipValue();
                    }
                }

                reader.nextIfMatch(',');

                return row;
            };
        }

        if (dataType instanceof ArrayType) {
            ValueConverter converter = this.makeConverter(((ArrayType) dataType).elementType);
            return reader -> {
                if (reader.nextIfNull()) {
                    return null;
                }

                // 字符串，当成json数组解析
                if (reader.isString()) {
                    String str = reader.readString();

                    try (JSONReader r = JSONReader.of(str)) {
                        if (!r.nextIfMatch('[')) {
                            return null;
                        }

                        List<Object> objs = new ArrayList<>();
                        while (!r.nextIfMatch(']')) {
                            objs.add(converter.convert(r));
                            r.nextIfMatch(',');
                        }
                        return objs.toArray();
                    }
                }

                // 数组格式
                if (reader.nextIfMatch('[')) {
                    List<Object> objs = new ArrayList<>();
                    while (!reader.nextIfMatch(']')) {
                        objs.add(converter.convert(reader));
                        reader.nextIfMatch(',');
                    }
                    reader.nextIfMatch(',');

                    return objs.toArray();
                }

                throw new UnsupportedOperationException("can not convert to array");
            };
        }

        throw new UnsupportedOperationException("unsupported dataType: " + dataType);
    }


    private Map<String, Integer> geneNameIdxes(StructType dataType) {
        Map<String, Integer> nameIdxes = new HashMap<>(dataType.fields.length * 2);
        for (int i = 0; i < dataType.fields.length; i++) {
            nameIdxes.put(dataType.fields[i].name, i);
        }
        return nameIdxes;
    }

    private Object convertToString(JSONReader reader) throws Exception {
        return reader.readString();
    }

    private Object convertToInteger(JSONReader reader) throws Exception {
        return reader.readInt32();
    }

    private Object convertToLong(JSONReader reader) throws Exception {
        return reader.readInt64();
    }

    private Object convertToFloat(JSONReader reader) throws Exception {
        return reader.readFloat();
    }

    private Object convertToDouble(JSONReader reader) throws Exception {
        return reader.readDouble();
    }

    @FunctionalInterface
    public interface ValueConverter extends Serializable {
        Object convert(JSONReader reader) throws Exception;
    }
}
