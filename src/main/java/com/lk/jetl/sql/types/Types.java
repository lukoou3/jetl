package com.lk.jetl.sql.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.lk.jetl.sql.types.StructType.StructField ;

public class Types {
    public static final IntegerType INT = new IntegerType();
    public static final LongType BIGINT = new LongType();
    public static final StringType STRING = new StringType();
    public static final FloatType FLOAT = new FloatType();
    public static final DoubleType DOUBLE = new DoubleType();
    public static final BooleanType BOOLEAN = new BooleanType();
    public static final BinaryType BINARY = new BinaryType();
    public static final NullType NULL = new NullType();

    public static final AbstractDataType NumericType = new AbstractDataType() {
        @Override
        public boolean acceptsType(DataType other) {
            return other instanceof NumericType;
        }

        @Override
        public String simpleString() {
            return "numeric";
        }
    };


    public static final AbstractDataType AnyDataType = new AbstractDataType() {
        @Override
        public boolean acceptsType(DataType other) {
            return true;
        }

        @Override
        public String simpleString() {
            return "any";
        }
    };

    public static final Pattern ARRAY_RE = Pattern.compile("array\\s*<(.+)>", Pattern.CASE_INSENSITIVE);
    public static final Pattern STRUCT_RE = Pattern.compile("struct\\s*<(.+)>", Pattern.CASE_INSENSITIVE);

    public static StructType parseSchemaFromJson(String jsonFields) {
        JSONArray fieldArray = JSON.parseArray(jsonFields);
        StructField[] fields = new StructField[fieldArray.size()];

        for (int i = 0; i < fieldArray.size(); i++) {
            JSONObject fieldObject = fieldArray.getJSONObject(i);
            String name = fieldObject.getString("name").trim();
            String type = fieldObject.getString("type").trim();
            DataType dataType = parseDataType(type);
            fields[i] = new StructField(name, dataType);
        }

        return new StructType(fields);
    }

    // 解析struct<>中的字段
    public static StructType parseStructType(String str){
        // 外面是否包含struct<>都能解析
        Matcher matcher = STRUCT_RE.matcher(str);
        if(matcher.matches()){
            str = matcher.group(1);
        }

        List<StructField> fields = new ArrayList<>();
        int startPos = 0, endPos = -1;
        int i = startPos + 1;
        int level = 0;
        while (i < str.length()){
            while (i < str.length()){
                if(str.charAt(i) == ':'){
                    endPos = i;
                    break;
                }
                i++;
            }

            if(endPos <= startPos){
                throw new UnsupportedOperationException("can not parse " + str);
            }

            String name = str.substring(startPos, endPos).trim();
            startPos = i + 1;
            endPos = -1;
            i = startPos + 1;
            while (i < str.length()){
                if(str.charAt(i) == ',' && level == 0){
                    endPos = i;
                    break;
                }
                if(str.charAt(i) == '<'){
                    level++;
                }
                if(str.charAt(i) == '>'){
                    level--;
                }
                i++;
            }

            if(i == str.length()){
                endPos = i;
            }
            if(endPos <= startPos){
                throw new UnsupportedOperationException("can not parse " + str);
            }

            String tp = str.substring(startPos, endPos).trim();
            fields.add(new StructField(name, parseDataType(tp)));

            i++;
            startPos = i;
            endPos = -1;
        }

        return new StructType(fields.toArray(new StructField[fields.size()]));
    }

    public static DataType parseDataType(String type){
        type = type.trim();
        if("int".equalsIgnoreCase(type)){
            return INT;
        } else if ("bigint".equalsIgnoreCase(type)){
            return BIGINT;
        } else if ("string".equalsIgnoreCase(type)){
            return STRING;
        } else if ("float".equalsIgnoreCase(type)){
            return FLOAT;
        } else if ("double".equalsIgnoreCase(type)){
            return DOUBLE;
        } else if ("boolean".equalsIgnoreCase(type)){
            return BOOLEAN;
        } else if ("binary".equalsIgnoreCase(type)){
            return BINARY;
        }

        // array类型
        Matcher matcher = ARRAY_RE.matcher(type);
        if(matcher.matches()){
            String eleType = matcher.group(1);
            DataType elementType = parseDataType(eleType);
            return new ArrayType(elementType);
        }

        // struct类型
        matcher = STRUCT_RE.matcher(type);
        if(matcher.matches()){
            String str = matcher.group(1);
            return parseStructType(str);
        }

        throw new UnsupportedOperationException("not support type:" + type);
    }

    public static Comparator getComparator(DataType t){
        if(t instanceof IntegerType){
            return (x, y) -> Integer.compare((Integer) x, (Integer)y);
        } else if (t instanceof LongType) {
            return (x, y) -> Long.compare((Long) x, (Long)y);
        } else if (t instanceof FloatType) {
            return (x, y) -> Float.compare((Float) x, (Float)y);
        } else if (t instanceof DoubleType) {
            return (x, y) -> Double.compare((Double) x, (Double)y);
        } else if (t instanceof BooleanType) {
            return (x, y) -> Boolean.compare((Boolean) x, (Boolean)y);
        } else if (t instanceof BinaryType) {
            return (x, y) -> Arrays.compare((byte[]) x, (byte[])y);
        } else if (t instanceof StringType) {
            return (x, y) -> ((String) x).compareTo((String)y);
        }

        throw new UnsupportedOperationException();
    }

    static void buildFormattedString(DataType dataType, String prefix, StringBuilder sb, int maxDepth){
        if(dataType instanceof ArrayType){
            ((ArrayType)dataType).buildFormattedString(prefix, sb, maxDepth - 1);
        } else if (dataType instanceof StructType) {
            ((StructType)dataType).buildFormattedString(prefix, sb, maxDepth - 1);
        }
    }
}
