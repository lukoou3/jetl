package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.types.*;

public class Literal extends LeafExpression {
    public final Object value;
    public final DataType dataType;

    public Literal(Object value, DataType dataType) {
        this.value = value;
        this.dataType = dataType;
        validateLiteralValue(value, dataType);
        this.args = new Object[]{value, dataType};
    }

    @Override
    public boolean isFoldable() {
        return true;
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public String toString() {
        if (value == null){
            return "null";
        } else if (dataType instanceof LongType) {
            return value + "L";
        } else if (dataType instanceof StringType) {
            return "'" + value + "'";
        } else {
            return value.toString();
        }
    }

    @Override
    public Object eval(Row input) {
        return value;
    }

    private void validateLiteralValue(Object value, DataType dataType) {
        if (!doValidate(value, dataType)) {
            throw new IllegalArgumentException(String.format("Literal must have a corresponding value to %s but class %s found.", dataType, value.getClass()));
        }
    }

    private boolean doValidate(Object v, DataType dataType) {
        if (v == null) {
            return true;
        } else if (dataType instanceof BooleanType) {
            return v instanceof Boolean;
        } else if (dataType instanceof IntegerType) {
            return v instanceof Integer;
        } else if (dataType instanceof LongType) {
            return v instanceof Long;
        } else if (dataType instanceof DoubleType) {
            return v instanceof Double;
        } else if (dataType instanceof StringType) {
            return v instanceof String;
        }

        return false;
    }

}
