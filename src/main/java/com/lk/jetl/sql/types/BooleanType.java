package com.lk.jetl.sql.types;

public class BooleanType extends AtomicType {
    BooleanType() {
    }

    @Override
    public String simpleString() {
        return "boolean";
    }
}
