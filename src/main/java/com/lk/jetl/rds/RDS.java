package com.lk.jetl.rds;

import com.lk.jetl.functions.FilterFunction;
import com.lk.jetl.functions.MapFunction;
import com.lk.jetl.util.Iterator;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public abstract class RDS<T> implements Serializable {
    static final AtomicInteger nextRddId = new AtomicInteger(0);

    protected final int id = nextRddId.getAndIncrement();

    protected abstract Partition[] getPartitions();

    public abstract Iterator<T> compute(Partition split);

    public <U> RDS<U> map(MapFunction<? super T, ? extends U> f){
        return new MapRDS(this, f);
    }

    public RDS<T> filter(FilterFunction<T> f){
        return new FilterRDS(this, f);
    }

    public void open() throws Exception{}

    public void close() throws Exception{}
}
