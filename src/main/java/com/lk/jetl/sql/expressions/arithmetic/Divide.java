package com.lk.jetl.sql.expressions.arithmetic;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.*;

import java.util.function.BiFunction;

public class Divide extends BinaryArithmetic {
    BiFunction<Object, Object, Object> div;

    public Divide(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public AbstractDataType getInputType() {
        return Types.DOUBLE;
    }

    @Override
    public String symbol() {
        return "/";
    }

    @Override
    public void open() {
        super.open();
        DataType dataType = getDataType();
        if (dataType instanceof DoubleType) {
            div = (x, y) -> (Double) x / (Double) y;
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public Object eval(Row input) {
        // evaluate right first as we have a chance to skip left if right is 0
        Number value2 = (Number) right.eval(input);
        if (value2 == null || value2.doubleValue() == 0) {
            return null;
        } else {
            Object value1 = left.eval(input);
            if (value1 == null) {
                return null;
            } else {
                return div.apply(value1, value2);
            }
        }
    }
}
