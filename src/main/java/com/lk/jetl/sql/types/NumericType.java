package com.lk.jetl.sql.types;

public abstract class NumericType extends AtomicType {
    @Override
    public boolean acceptsType(DataType other) {
        return other instanceof NumericType;
    }
}
