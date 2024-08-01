package com.lk.jetl.rds;

import java.util.List;

public class ParallelCollectionPartition<T> extends Partition{
    final int rdsId;
    final int slice;
    final List<T> values;

    public ParallelCollectionPartition(int rdsId, int slice, List<T> values) {
        this.rdsId = rdsId;
        this.slice = slice;
        this.values = values;
    }

    @Override
    public int index() {
        return slice;
    }

    @Override
    public int hashCode() {
        return 41 * (41 + rdsId) + slice;
    }
}
