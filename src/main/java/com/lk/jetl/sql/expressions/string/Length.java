package com.lk.jetl.sql.expressions.string;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.UnaryExpression;
import com.lk.jetl.sql.types.AbstractDataType;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.Types;

import java.util.List;

public class Length extends UnaryExpression {
    public Length(Expression child) {
        super(child);
    }

    @Override
    public DataType getDataType() {
        return Types.INT;
    }

    @Override
    public boolean expectsInputTypes() {
        return true;
    }

    @Override
    public List<AbstractDataType> inputTypes() {
        return List.of(Types.STRING);
    }

    @Override
    protected Object nullSafeEval(Object input) {
        return ((String)input).length();
    }
}
