package com.lk.jetl.sql.expressions.string;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.util.Option;
import com.lk.jetl.sql.util.StringUtils;

public class StringTrim extends String2TrimExpression {
    public StringTrim(Expression srcStr, Option<Expression> trimStr) {
        super(srcStr, trimStr);
    }

    public StringTrim(Expression srcStr, Expression trimStr) {
        this(srcStr, Option.option(trimStr));
    }

    public StringTrim(Expression srcStr) {
        this(srcStr, Option.none());
    }

    @Override
    public String prettyName() {
        return "trim";
    }

    @Override
    public Object eval(Row input) {
        String srcString = (String) srcStr.eval(input);
        if (srcString == null) {
            return null;
        }else {
            if (trimStr.isEmpty()) {
                return srcString.trim();
            } else {
                String trimString = (String) trimStr.get().eval(input);
                if(trimString == null){
                    return null;
                }
                return StringUtils.trim(srcString, true, true, trimString);
            }
        }
    }
}
