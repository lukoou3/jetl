package com.lk.jetl.sql.expressions.arithmetic;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.*;

import java.util.function.BiFunction;

public class Add extends BinaryArithmetic {
    BiFunction<Object, Object, Object> plus;

    public Add(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public AbstractDataType getInputType() {
        return Types.NumericType;
    }

    @Override
    public String symbol() {
        return "+";
    }

    @Override
    public void open() {
        super.open();
        DataType dataType = getDataType();
        if (dataType instanceof IntegerType) {
            plus = (x, y) -> (Integer) x + (Integer) y;
        } else if (dataType instanceof LongType) {
            plus = (x, y) -> (Long) x + (Long) y;
        } else if (dataType instanceof DoubleType) {
            plus = (x, y) -> (Double) x + (Double) y;
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    protected Object nullSafeEval(Object input1, Object input2) {
        return plus.apply(input1, input2);
    }
}
