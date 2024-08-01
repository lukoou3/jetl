package com.lk.jetl.sql.expressions.string;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.util.Option;
import com.lk.jetl.util.StringUtils;

public class StringTrimRight extends String2TrimExpression {
    public StringTrimRight(Expression srcStr, Option<Expression> trimStr) {
        super(srcStr, trimStr);
    }

    public StringTrimRight(Expression srcStr, Expression trimStr) {
        this(srcStr, Option.option(trimStr));
    }

    public StringTrimRight(Expression srcStr) {
        this(srcStr, Option.none());
    }

    @Override
    public String prettyName() {
        return "rtrim";
    }

    @Override
    public Object eval(Row input) {
        String srcString = (String) srcStr.eval(input);
        if (srcString == null) {
            return null;
        }else {
            if (trimStr.isEmpty()) {
                return StringUtils.trim(srcString, false, true, null);
            } else {
                String trimString = (String) trimStr.get().eval(input);
                if(trimString == null){
                    return null;
                }
                return StringUtils.trim(srcString, false, true, trimString);
            }
        }
    }

}
