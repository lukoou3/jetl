package com.lk.jetl.sql.expressions.regexp;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.Literal;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.Types;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;

public class RegExpExtract extends RegExpExtractBase {
    public RegExpExtract(Expression subject, Expression regexp, Expression idx) {
        super(subject, regexp, idx);
    }

    public RegExpExtract(Expression subject, Expression regexp) {
        this(subject, regexp, new Literal(1, Types.INT));
    }

    @Override
    public DataType getDataType() {
        return Types.STRING;
    }

    @Override
    public String prettyName() {
        return "regexp_extract";
    }

    @Override
    protected Object nullSafeEval(Object s, Object p, Object r) {
        Matcher m = getLastMatcher((String) s, (String) p);
        if (m.find()) {
            MatchResult mr = m.toMatchResult();
            int index = (int) r;
            RegExpExtractBase.checkGroupIndex(mr.groupCount(), index);
            String group = mr.group(index);
            if (group == null) { // Pattern matched, but it's an optional group
                return "";
            } else {
                return group;
            }
        } else {
            return "";
        }
    }
}
