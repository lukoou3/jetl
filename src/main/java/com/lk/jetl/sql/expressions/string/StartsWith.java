package com.lk.jetl.sql.expressions.string;

import com.lk.jetl.sql.expressions.Expression;

public class StartsWith extends StringPredicate {

    public StartsWith(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public boolean compare(String l, String r) {
        return l.startsWith(r);
    }
}
