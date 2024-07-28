package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.types.DataType;

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

}
