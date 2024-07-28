package com.lk.jetl.sql.expressions.arithmetic;

import com.lk.jetl.sql.expressions.BinaryOperator;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.DataType;

public abstract class BinaryArithmetic extends BinaryOperator {
    public BinaryArithmetic(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public DataType getDataType() {
        return left.getDataType();
    }

}
