package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.Row;

import java.util.Arrays;
import java.util.List;

public abstract class BinaryExpression extends Expression {
    public final Expression left;
    public final Expression right;

    public BinaryExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
        this.args = new Object[]{left, right};
    }

    @Override
    public List<Expression> getChildren() {
        return Arrays.asList(left, right);
    }

    @Override
    public boolean isFoldable() {
        return left.isFoldable() && right.isFoldable();
    }

    @Override
    public Object eval(Row input) {
        Object value1 = left.eval(input);
        if (value1 == null) {
            return null;
        } else {
            Object value2 = right.eval(input);
            if (value2 == null) {
                return null;
            } else {
                return nullSafeEval(value1, value2);
            }
        }
    }

    protected Object nullSafeEval(Object input1, Object input2) {
        throw new UnsupportedOperationException("BinaryExpressions must override either eval or nullSafeEval");
    }
}
