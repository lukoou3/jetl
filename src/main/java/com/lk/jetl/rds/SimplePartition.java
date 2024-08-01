package com.lk.jetl.rds;

public class SimplePartition extends Partition {
    private final int rdsId;
    private final int index;

    public SimplePartition(int rdsId, int index) {
        this.rdsId = rdsId;
        this.index = index;
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public int hashCode() {
        return 41 * (41 + rdsId) + index;
    }

    public static Partition[] simplePartitions(RDS rds, int parallelism) {
        Partition[] partitions = new Partition[parallelism];
        for (int i = 0; i < parallelism; i++) {
            partitions[i] = new SimplePartition(rds.id, i);
        }
        return partitions;
    }
}
