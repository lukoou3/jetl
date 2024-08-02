package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.StructType;

import java.util.HashMap;
import java.util.Map;

public class BoundReference extends LeafExpression {
    public final int ordinal;
    public final DataType dataType;
    public final String name;

    public BoundReference(int ordinal, DataType dataType) {
        this(ordinal, dataType, "");
    }

    public BoundReference(int ordinal, DataType dataType, String name) {
        this.ordinal = ordinal;
        this.dataType = dataType;
        this.name = name;
        this.args = new Object[]{ordinal, dataType, name};
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public String toString() {
        return String.format("input[%s#%d, %s]", name, ordinal, dataType.simpleString());
    }

    @Override
    public Object eval(Row input) {
        if (input.isNullAt(ordinal)) {
            return null;
        }
        return input.get(ordinal);
    }

    public static Map<String, Integer> nameIdxes(StructType schema){
        Map<String, Integer> nameIdxes = new HashMap<>();
        for (int i = 0; i < schema.fields.length; i++) {
            nameIdxes.put(schema.fields[i].name, i);
        }
        return nameIdxes;
    }

    public static Expression bindReference(Expression expression, StructType schema, Map<String, Integer> nameIdxes) {
        return expression.transformUp(e -> {
            if(e instanceof AttributeReference){
                AttributeReference a = (AttributeReference) e;
                String name = a.name;
                Integer idx = nameIdxes.get(name);
                if (idx == null) {
                    throw new IllegalArgumentException("no col:" + name);
                }
                assert a.dataType.equals(schema.fields[idx].dataType);
                return new BoundReference(idx, a.dataType, name);
            }
            return e;
        });
    }
}
