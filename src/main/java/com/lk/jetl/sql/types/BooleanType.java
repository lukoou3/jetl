package com.lk.jetl.sql.types;

public class BooleanType extends DataType{
    BooleanType() {
    }

    @Override
    public String simpleString() {
        return "boolean";
    }
}
