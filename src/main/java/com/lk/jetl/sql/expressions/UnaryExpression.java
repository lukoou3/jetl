package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.Row;

import java.util.Arrays;
import java.util.List;

public abstract class UnaryExpression extends Expression{

    public final Expression child;

    public UnaryExpression(Expression child) {
        this.child = child;
        this.args = new Object[]{child};
    }

    @Override
    public boolean isFoldable() {
        return child.isFoldable();
    }

    @Override
    public List<Expression> getChildren() {
        return Arrays.asList(child);
    }

    @Override
    public Object eval(Row input) {
        Object value = child.eval(input);
        if (value == null) {
            return null;
        } else {
            return nullSafeEval(value);
        }
    }

    protected Object nullSafeEval(Object input) {
        throw new UnsupportedOperationException("UnaryExpressions must override either eval or nullSafeEval");
    }
}
