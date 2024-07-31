package com.lk.jetl.sql.expressions.regexp;

import com.lk.jetl.sql.expressions.BinaryExpression;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.AbstractDataType;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.Types;

import java.util.List;
import java.util.regex.Pattern;

public abstract class StringRegexExpression extends BinaryExpression {
    private Pattern cache;

    public StringRegexExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public DataType getDataType() {
        return Types.BOOLEAN;
    }

    @Override
    public boolean expectsInputTypes() {
        return true;
    }

    @Override
    public List<AbstractDataType> inputTypes() {
        return List.of(Types.STRING, Types.STRING);
    }

    protected abstract String escape(String v);
    protected abstract boolean matches(Pattern regex, String str);

    protected Pattern compile(String str) {
        if (str == null) {
            return null;
        } else {
            // Let it raise exception if couldn't compile the regex string
            return Pattern.compile(escape(str));
        }
    }

    protected Pattern pattern(String str) {
        if (cache == null) {
            if (right.isFoldable()) {
                cache = compile(str);
                return cache;
            }
            return compile(str);
        } else {
            return cache;
        }
    }

    @Override
    protected Object nullSafeEval(Object input1, Object input2) {
        Pattern regex = pattern((String) input2);
        if(regex == null){
            return null;
        }else{
            return matches(regex, (String) input1);
        }
    }
}
