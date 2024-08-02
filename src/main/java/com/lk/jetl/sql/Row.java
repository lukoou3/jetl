package com.lk.jetl.sql;

import com.lk.jetl.sql.types.StructType;

import java.io.Serializable;
import java.util.Arrays;

public abstract class Row implements Serializable {

    public abstract int size();

    public StructType getSchema(){
        return null;
    }

    public abstract Object get(int i);

    public abstract void update(int i, Object value);

    public boolean isNullAt(int i){
        return get(i) == null;
    }

    public String mkString(String start, String sep, String end){
        StringBuilder builder = new StringBuilder();
        builder.append(start);

        int len = size();
        Object item;
        for (int i = 0; i < len; i++) {
            item = get(i);
            if(i == 0){
                builder.append(item instanceof Object[]? Arrays.toString((Object[]) item): item);
            }else{
                builder.append(sep);
                builder.append(item instanceof Object[]? Arrays.toString((Object[]) item): item);
            }
        }

        builder.append(end);
        return builder.toString();
    }

    @Override
    public String toString() {
        return mkString("[", ", ", "]");
    }
}
