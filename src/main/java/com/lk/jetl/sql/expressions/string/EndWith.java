package com.lk.jetl.sql.expressions.string;

import com.lk.jetl.sql.expressions.Expression;

public class EndWith extends StringPredicate {

    public EndWith(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public boolean compare(String l, String r) {
        return l.endsWith(r);
    }
}
