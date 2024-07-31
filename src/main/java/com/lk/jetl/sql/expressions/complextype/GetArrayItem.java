package com.lk.jetl.sql.expressions.complextype;

import com.lk.jetl.sql.expressions.BinaryExpression;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.AbstractDataType;
import com.lk.jetl.sql.types.ArrayType;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.Types;

import java.util.List;

public class GetArrayItem extends BinaryExpression {
    public GetArrayItem(Expression child, Expression ordinal) {
        super(child, ordinal);
    }

    @Override
    public DataType getDataType() {
        return ((ArrayType)left.getDataType()).elementType;
    }

    @Override
    public boolean expectsInputTypes() {
        return true;
    }

    @Override
    public List<AbstractDataType> inputTypes() {
        // // We have done type checking for child in `UnresolvedExtractValue`, so only need to check the `ordinal`.
        return List.of(Types.AnyDataType, Types.INT);
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", left, right);
    }

    @Override
    protected Object nullSafeEval(Object value, Object ordinal) {
        Object[] baseValue = (Object[]) value;
        int index = (Integer) ordinal;
        if (index >= baseValue.length || index < 0) {
            return null;
        } else {
            return baseValue[index];
        }
    }
}
