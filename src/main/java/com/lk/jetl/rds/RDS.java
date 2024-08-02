package com.lk.jetl.rds;

import com.lk.jetl.functions.FilterFunction;
import com.lk.jetl.functions.MapFunction;
import com.lk.jetl.functions.SinkFunction;
import com.lk.jetl.util.Iterator;
import com.lk.jetl.util.Option;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class RDS<T> implements Serializable {
    static final AtomicInteger nextRddId = new AtomicInteger(0);

    protected final int id = nextRddId.getAndIncrement();
    protected String name;

    public abstract Partition[] getPartitions();

    public abstract Iterator<T> compute(Partition split);

    public <U> RDS<U> map(MapFunction<? super T, ? extends U> f){
        return new MapRDS(this, f);
    }

    public RDS<Void> sink(SinkFunction<? super T> f){
        return new SinkRDS(this, f);
    }

    public RDS<T> filter(FilterFunction<T> f){
        return new FilterRDS(this, f);
    }

    public RDS<T> name(String name){
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return String.format("%s%s[%d]", Option.option(name).map(x->x + " ").getOrElse(""), getClass().getSimpleName(), id);
    }

    public void open() throws Exception{}

    public void close() throws Exception{}
}
