package com.lk.jetl.sql.types;

import java.io.Serializable;

public abstract class AbstractDataType implements Serializable {
    /**
     * Returns true if other is an acceptable input type for a function that expects this,
     * possibly abstract DataType.
     */
    public abstract boolean acceptsType(DataType other);

    /** Readable string representation for the type. */
    public abstract String simpleString();
}
