package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.types.DataType;

public class Alias extends UnaryExpression implements NamedExpression{
    public final String name;

    public Alias(Expression child, String name) {
        super(child);
        this.name = name;
        this.args = new Object[]{child, name};
    }

    /** We should never fold named expressions in order to not remove the alias. */
    @Override
    public boolean isFoldable() {
        return false;
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

    @Override
    public String name() {
        return name;
    }
}
