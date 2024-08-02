package com.lk.jetl.rds;

import com.lk.jetl.functions.MapFunction;
import com.lk.jetl.functions.RichFunction;
import com.lk.jetl.util.Iterator;

public class MapRDS<U, T> extends RDS<U> {
    final RDS<T> prev;
    final MapFunction<? super T, ? extends U> f;

    public MapRDS(RDS<T> prev, MapFunction<T, U> f) {
        this.prev = prev;
        this.f = f;
    }

    @Override
    public Partition[] getPartitions() {
        return prev.getPartitions();
    }

    @Override
    public Iterator<U> compute(Partition split) {
        return prev.compute(split).map(f::map);
    }

    @Override
    public void open() throws Exception {
        super.open();
        prev.open();
        if (f instanceof RichFunction) {
            ((RichFunction) f).open();
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        prev.close();
        if (f instanceof RichFunction) {
            ((RichFunction) f).close();
        }
    }
}
