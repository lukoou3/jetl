package com.lk.jetl.sql.expressions.string;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.Literal;
import com.lk.jetl.sql.expressions.TernaryExpression;
import com.lk.jetl.sql.types.AbstractDataType;
import com.lk.jetl.sql.types.DataType;

import java.util.List;

import static com.lk.jetl.sql.types.Types.INT;
import static com.lk.jetl.sql.types.Types.STRING;

public class Substring extends TernaryExpression {
    final Expression str;
    final Expression pos;
    final Expression len;

    public Substring(Expression str, Expression pos, Expression len) {
        this.str = str;
        this.pos = pos;
        this.len = len;
        this.args = new Object[]{str, pos, len};
    }

    public Substring(Expression str, Expression pos) {
        this(str, pos, new Literal(Integer.MAX_VALUE,INT));
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(str, pos, len);
    }

    @Override
    public DataType getDataType() {
        return str.getDataType();
    }

    @Override
    public boolean expectsInputTypes() {
        return true;
    }

    @Override
    public List<AbstractDataType> inputTypes() {
        return List.of(STRING, INT, INT);
    }

    @Override
    protected Object nullSafeEval(Object input1, Object input2, Object input3) {
        String string = (String)input1;
        int pos = (Integer) input2;
        int len = (Integer) input3;
        int length = string.length();
        if (pos > length) {
            return "";
        }

        int start = 0;
        int end;
        if (pos > 0) {
            start = pos - 1;
        } else if (pos < 0) {
            start = length + pos;
        }
        if ((length - start) < len) {
            end = length;
        } else {
            end = start + len;
        }
        start = Math.max(start, 0); // underflow
        if (start >= end) {
            return "";
        }

        return  string.substring(start, end);
    }
}
