package com.lk.jetl.sql.expressions.string;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.util.StringUtils;

import java.util.regex.Pattern;

public class Like extends StringRegexExpression{
    private final char escapeChar = '\\';

    public Like(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    protected String escape(String v) {
        return StringUtils.escapeLikeRegex(v, escapeChar);
    }

    @Override
    protected boolean matches(Pattern regex, String str) {
        return regex.matcher(str).matches(); // 完全匹配
    }
}
