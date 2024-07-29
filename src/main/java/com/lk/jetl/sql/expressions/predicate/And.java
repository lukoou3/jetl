package com.lk.jetl.sql.expressions.predicate;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.expressions.BinaryOperator;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.AbstractDataType;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.Types;

public class And extends BinaryOperator {

    public And(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public AbstractDataType getInputType() {
        return Types.BOOLEAN;
    }

    @Override
    public DataType getDataType() {
        return Types.BOOLEAN;
    }

    @Override
    public String symbol() {
        return "&&";
    }

    @Override
    public Object eval(Row input) {
        Object input1 = left.eval(input);
        if (Boolean.FALSE.equals(input1)) {
            return false;
        } else {
            Object input2 = right.eval(input);
            if (Boolean.FALSE.equals(input2)) {
                return false;
            } else {
                if (input1 != null && input2 != null) {
                    return true;
                } else {
                    return null;
                }
            }
        }
    }
}
