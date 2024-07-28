package com.lk.jetl.sql.expressions.arithmetic;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.*;

import java.util.function.BiFunction;

public class Subtract extends BinaryArithmetic {
    BiFunction<Object, Object, Object> minus;

    public Subtract(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public AbstractDataType getInputType() {
        return Types.NumericType;
    }

    @Override
    public String symbol() {
        return "-";
    }

    @Override
    public void open() {
        super.open();
        DataType dataType = getDataType();
        if (dataType instanceof IntegerType) {
            minus = (x, y) -> (Integer) x - (Integer) y;
        } else if (dataType instanceof LongType) {
            minus = (x, y) -> (Long) x - (Long) y;
        } else if (dataType instanceof DoubleType) {
            minus = (x, y) -> (Double) x - (Double) y;
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    protected Object nullSafeEval(Object input1, Object input2) {
        return minus.apply(input1, input2);
    }
}
