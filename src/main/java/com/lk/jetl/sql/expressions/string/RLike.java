package com.lk.jetl.sql.expressions.string;

import com.lk.jetl.sql.expressions.Expression;

import java.util.regex.Pattern;

public class RLike extends StringRegexExpression {
    public RLike(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    protected String escape(String v) {
        return v;
    }

    @Override
    protected boolean matches(Pattern regex, String str) {
        return regex.matcher(str).find(0);
    }
}
