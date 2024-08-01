package com.lk.jetl.rds;

import java.io.Serializable;
import java.util.Iterator;

public abstract class RDS<T> implements Serializable {

    protected abstract Partition[] getPartitions();

    public abstract Iterator<T> compute(Partition split);

    public <U> RDS<U> map(){
        return null;
    }

    public void open() throws Exception{}

    public void close() throws Exception{}
}
