package com.lk.jetl.sql.api;

import com.lk.jetl.sql.types.AbstractDataType;
import com.lk.jetl.sql.types.DataType;

import java.io.Serializable;
import java.util.List;

public abstract class UDF implements Serializable {
    public abstract Object call(Object[] args);

    public abstract List<AbstractDataType> inputTypes();

    public abstract DataType dataType();

}
