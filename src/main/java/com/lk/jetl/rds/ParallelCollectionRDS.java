package com.lk.jetl.rds;

import com.lk.jetl.util.Iterator;

import java.util.List;

public class ParallelCollectionRDS<T> extends RDS<T> {
    final List<T> datas;
    final int parallelism;

    public ParallelCollectionRDS(List<T> datas, int parallelism) {
        this.datas = datas;
        this.parallelism = parallelism;
    }

    @Override
    protected Partition[] getPartitions() {
        Partition[] partitions = new Partition[parallelism];
        for (int i = 0; i < parallelism; i++) {
            partitions[i] = new ParallelCollectionPartition<>(id, i, datas);
        }
        return partitions;
    }

    @Override
    public Iterator<T> compute(Partition split) {
        ParallelCollectionPartition<T> partition = (ParallelCollectionPartition<T>) split;
        return Iterator.fromJava(partition.values.iterator());
    }
}
