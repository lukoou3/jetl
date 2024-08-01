package com.lk.jetl.rds;

import com.lk.jetl.functions.FilterFunction;
import com.lk.jetl.functions.RichFunction;
import com.lk.jetl.util.Iterator;

public class FilterRDS<T> extends RDS<T> {
    final RDS<T> prev;
    final FilterFunction<T> f;

    public FilterRDS(RDS<T> prev, FilterFunction<T> f) {
        this.prev = prev;
        this.f = f;
    }

    @Override
    protected Partition[] getPartitions() {
        return prev.getPartitions();
    }

    @Override
    public Iterator<T> compute(Partition split) {
        return prev.compute(split).filter(f::filter);
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
