package com.lk.jetl.sql.expressions.nvl;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.UnaryExpression;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.Types;

public class IsNull extends UnaryExpression {
    public IsNull(Expression child) {
        super(child);
    }

    @Override
    public DataType getDataType() {
        return Types.BOOLEAN;
    }

    @Override
    public Object eval(Row input) {
        return child.eval(input) == null;
    }
}
