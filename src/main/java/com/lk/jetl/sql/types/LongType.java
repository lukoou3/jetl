package com.lk.jetl.sql.types;

public class LongType extends IntegralType {
    LongType() {
    }
    @Override
    public String simpleString() {
        return "bigint";
    }
}
