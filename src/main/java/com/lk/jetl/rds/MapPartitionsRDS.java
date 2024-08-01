package com.lk.jetl.rds;

import java.util.Iterator;

public class MapPartitionsRDS<U, T> extends RDS<U> {
    final RDS<T> prev;
    final Object f;

    public MapPartitionsRDS(RDS<T> prev, Object f) {
        this.prev = prev;
        this.f = f;
    }


    @Override
    protected Partition[] getPartitions() {
        return prev.getPartitions();
    }

    @Override
    public Iterator<U> compute(Partition split) {
        Iterator<T> compute = prev.compute(split);
        return new Iterator<U>() {
            @Override
            public boolean hasNext() {
                return compute.hasNext();
            }

            @Override
            public U next() {
                return (U)compute.next();
            }
        };
    }
}
