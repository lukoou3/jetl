package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.types.DataType;

public class UnresolvedExtractValue extends BinaryExpression {
    public UnresolvedExtractValue(Expression child, Expression extraction) {
        super(child, extraction);
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", left, right);
    }

    @Override
    public boolean isResolved() {
        return false;
    }

    @Override
    public boolean isFoldable() {
        return false;
    }

    @Override
    public Object eval(Row input) {
        throw new UnsupportedOperationException("Unresolved");
    }

    @Override
    public DataType getDataType() {
        throw new UnsupportedOperationException("Unresolved");
    }
}
