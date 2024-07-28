package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.types.DataType;

public class Alias extends UnaryExpression {
    public final String name;

    public Alias(Expression child, String name) {
        super(child);
        this.name = name;
        this.args = new Object[]{child, name};
    }

    @Override
    public DataType getDataType() {
        return child.getDataType();
    }

    @Override
    public Object eval(Row input) {
        return child.eval(input);
    }

    @Override
    public String toString() {
        return String.format("%s as %s", child, name);
    }
}
