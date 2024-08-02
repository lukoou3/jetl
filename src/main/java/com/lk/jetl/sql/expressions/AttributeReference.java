package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.types.DataType;

public class AttributeReference extends LeafExpression implements NamedExpression {
    public final String name;
    public final DataType dataType;

    public AttributeReference(String name, DataType dataType) {
        this.name = name;
        this.dataType = dataType;
        this.args = new Object[]{name, dataType};
    }

    @Override
    public boolean isFoldable() {
        return false;
    }

    @Override
    public Object eval(Row input) {
        throw new UnsupportedOperationException("Cannot evaluate expression: " + this);
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }


}
