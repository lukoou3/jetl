package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.Row;

public abstract class TernaryExpression extends Expression {
    private Expression[] exprs;

    @Override
    public boolean isFoldable() {
        return getChildren().stream().allMatch(x -> x.isFoldable());
    }

    @Override
    public Object eval(Row input) {
        if(exprs == null){
            exprs = getChildren().toArray(Expression[]::new);
            assert exprs.length == 3;
        }
        Object value1 = exprs[0].eval(input);
        if (value1 != null) {
            Object value2 = exprs[1].eval(input);
            if (value2 != null) {
                Object value3 = exprs[2].eval(input);
                if (value3 != null) {
                    return nullSafeEval(value1, value2, value3);
                }
            }
        }
        return null;
    }

    protected Object nullSafeEval(Object input1, Object input2, Object input3) {
        throw new UnsupportedOperationException("TernaryExpressions must override either eval or nullSafeEval");
    }
}
