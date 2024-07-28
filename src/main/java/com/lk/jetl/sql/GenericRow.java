package com.lk.jetl.sql;

public class GenericRow implements Row {
    private final Object[] values;

    public GenericRow(Object[] values) {
        this.values = values;
    }

    public GenericRow(int size) {
        this(new Object[size]);
    }


    @Override
    public int size() {
        return values.length;
    }

    @Override
    public Object get(int i) {
        return values[i];
    }

    @Override
    public void update(int i, Object value) {
        values[i] = value;
    }
}
