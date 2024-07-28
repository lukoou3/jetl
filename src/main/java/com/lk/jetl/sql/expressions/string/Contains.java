package com.lk.jetl.sql.expressions.string;

import com.lk.jetl.sql.expressions.Expression;

public class Contains extends StringPredicate {

    public Contains(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public boolean compare(String l, String r) {
        return l.contains(r);
    }
}
