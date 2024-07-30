package com.lk.jetl.sql.expressions.predicate;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.UnaryExpression;
import com.lk.jetl.sql.types.AbstractDataType;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.Types;

import java.util.List;

public class Not extends UnaryExpression {
    public Not(Expression child) {
        super(child);
    }

    @Override
    public DataType getDataType() {
        return Types.BOOLEAN;
    }

    @Override
    public boolean expectsInputTypes() {
        return true;
    }

    @Override
    public List<AbstractDataType> inputTypes() {
        return List.of(Types.BOOLEAN);
    }

    @Override
    public String toString() {
        return "NOT " + child.toString();
    }

    @Override
    protected Object nullSafeEval(Object input) {
        return !(Boolean)input;
    }
}
