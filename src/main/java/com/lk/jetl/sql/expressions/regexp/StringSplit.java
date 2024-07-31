package com.lk.jetl.sql.expressions.regexp;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.Literal;
import com.lk.jetl.sql.expressions.TernaryExpression;
import com.lk.jetl.sql.types.AbstractDataType;
import com.lk.jetl.sql.types.ArrayType;
import com.lk.jetl.sql.types.DataType;

import java.util.List;

import static com.lk.jetl.sql.types.Types.INT;
import static com.lk.jetl.sql.types.Types.STRING;

public class StringSplit extends TernaryExpression {
    final Expression str;
    final Expression regex;
    final Expression limit;

    public StringSplit(Expression str, Expression regex, Expression limit) {
        this.str = str;
        this.regex = regex;
        this.limit = limit;
        this.args = new Object[]{str, regex, limit};
    }

    public StringSplit(Expression str, Expression regex) {
        this(str, regex, new Literal(-1, INT));
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(str, regex, limit);
    }

    @Override
    public DataType getDataType() {
        return new ArrayType(STRING);
    }

    @Override
    public boolean expectsInputTypes() {
        return true;
    }

    @Override
    public List<AbstractDataType> inputTypes() {
        return List.of(STRING, STRING, INT);
    }

    @Override
    protected Object nullSafeEval(Object input1, Object input2, Object input3) {
        String string = (String)input1;
        String regex = (String) input2;
        int limit = (Integer) input3;

        // Java String's split method supports "ignore empty string" behavior when the limit is 0
        // whereas other languages do not. To avoid this java specific behavior, we fall back to
        // -1 when the limit is 0.
        if (limit == 0) {
            limit = -1;
        }

        return  string.split(regex, limit);
    }

    @Override
    public String prettyName() {
        return "split";
    }
}
