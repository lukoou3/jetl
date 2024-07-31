package com.lk.jetl.sql.rule;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.conditional.If;
import com.lk.jetl.sql.types.StructType;

import java.util.Arrays;

public abstract class Rule {

    public Expression apply(Expression e){
        return e.transformUp(x -> applyChild(x));
    }

    protected abstract Expression applyChild(Expression e);

    public String ruleName(){
        String className = getClass().getName();
        return className;
    }
}
