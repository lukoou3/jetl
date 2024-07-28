package com.lk.jetl.sql.rule;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.StructType;

public abstract class Rule {

    public abstract Expression apply(Expression e, StructType schema);

    public String ruleName(){
        String className = getClass().getName();
        return className;
    }
}
