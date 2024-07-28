package com.lk.jetl.sql.types;

public class BinaryType extends AtomicType {

    BinaryType() {
    }

    @Override
    public String simpleString() {
        return "binary";
    }
}
