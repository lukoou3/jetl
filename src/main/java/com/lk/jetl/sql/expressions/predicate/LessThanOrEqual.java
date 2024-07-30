package com.lk.jetl.sql.expressions.predicate;

import com.lk.jetl.sql.expressions.Expression;

public class LessThanOrEqual extends BinaryComparison {

    public LessThanOrEqual(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public String symbol() {
        return "<=";
    }

    @Override
    protected Object nullSafeEval(Object input1, Object input2) {
        return getComparator().compare(input1, input2) <= 0;
    }
}
