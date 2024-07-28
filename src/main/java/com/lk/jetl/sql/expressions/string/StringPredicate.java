package com.lk.jetl.sql.expressions.string;

import com.lk.jetl.sql.expressions.BinaryExpression;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.DataType;

import static com.lk.jetl.sql.types.Types.BOOLEAN;

public abstract class StringPredicate extends BinaryExpression{

    public StringPredicate(Expression left, Expression right) {
        super(left, right);
    }

    public abstract boolean compare(String l, String r);

    @Override
    protected Object nullSafeEval(Object input1, Object input2) {
        return compare((String) input1, (String) input2);
    }

    @Override
    public DataType getDataType() {
        return BOOLEAN;
    }
}
