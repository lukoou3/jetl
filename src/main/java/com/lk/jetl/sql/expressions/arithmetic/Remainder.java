package com.lk.jetl.sql.expressions.arithmetic;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.*;

import java.util.function.BiFunction;

public class Remainder extends BinaryArithmetic {
    BiFunction<Object, Object, Object> mod;

    public Remainder(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public AbstractDataType getInputType() {
        return Types.NumericType;
    }

    @Override
    public String symbol() {
        return "%";
    }

    @Override
    public void open() {
        super.open();
        DataType dataType = getDataType();
        if (dataType instanceof IntegerType) {
            mod = (x, y) -> (Integer) x % (Integer) y;
        } else if (dataType instanceof LongType) {
            mod = (x, y) -> (Long) x % (Long) y;
        } else if (dataType instanceof DoubleType) {
            mod = (x, y) -> (Double) x % (Double) y;
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public Object eval(Row input) {
        // evaluate right first as we have a chance to skip left if right is 0
        Number value2 = (Number) right.eval(input);
        if (value2 == null || value2.intValue() == 0) {
            return null;
        } else {
            Object value1 = left.eval(input);
            if (value1 == null) {
                return null;
            } else {
                return mod.apply(value1, value2);
            }
        }
    }
}
