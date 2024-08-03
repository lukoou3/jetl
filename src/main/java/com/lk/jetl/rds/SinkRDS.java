package com.lk.jetl.rds;

import com.lk.jetl.functions.SinkFunction;
import com.lk.jetl.functions.RichFunction;
import com.lk.jetl.util.Iterator;

public class SinkRDS<T> extends RDS<T> {
    final RDS<T> prev;
    final SinkFunction<? super T> f;

    public SinkRDS(RDS<T> prev, SinkFunction<? super T> f) {
        this.prev = prev;
        this.f = f;
    }

    @Override
    public Partition[] getPartitions() {
        return prev.getPartitions();
    }

    @Override
    public Iterator<T> compute(Partition split) {
        Iterator<T> iter = prev.compute(split);
        while (iter.hasNext()){
            f.invoke(iter.next());
        }
        return Iterator.empty();
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
