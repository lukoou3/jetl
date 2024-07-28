package com.lk.jetl.sql;

import com.lk.jetl.sql.types.StructType;

import java.io.Serializable;

public interface Row extends Serializable {

    int size();

    default StructType getSchema(){
        return null;
    }

    Object get(int i);

    void update(int i, Object value);

    default boolean isNullAt(int i){
        return get(i) == null;
    }

}
