package com.lk.jetl.sql.types;

public class StringType extends AtomicType {
    StringType() {
    }
    @Override
    public String simpleString() {
        return "string";
    }
}
