package com.lk.jetl.connector.inline;


import com.lk.jetl.rds.ParallelCollectionPartition;
import com.lk.jetl.rds.Partition;
import com.lk.jetl.rds.RDS;
import com.lk.jetl.util.Iterator;

import java.util.List;

public class InlineRDS<T> extends RDS<T> {
    private final List<T> datas;
    private final int parallelism;
    private final int rowsPerSecond;
    private final long numberOfRows;
    private final long millisPerRow;

    public InlineRDS(List<T> datas, int parallelism, int rowsPerSecond, long numberOfRows, long millisPerRow) {
        this.datas = datas;
        this.parallelism = parallelism;
        this.rowsPerSecond = rowsPerSecond;
        this.numberOfRows = numberOfRows;
        this.millisPerRow = millisPerRow;
    }

    @Override
    public Partition[] getPartitions() {
        Partition[] partitions = new Partition[parallelism];
        for (int i = 0; i < parallelism; i++) {
            partitions[i] = new ParallelCollectionPartition<>(id, i, datas);
        }
        return partitions;
    }

    @Override
    public Iterator<T> compute(Partition split) {
        final long rowsForSubtask = getRowsForSubTask(parallelism, split.index());
        final long rowsPerSecondForSubtask = getRowsPerSecondForSubTask(parallelism, split.index());
        ParallelCollectionPartition<T> partition = (ParallelCollectionPartition<T>) split;
        return new Iterator<T>() {
            long rows = 0;
            int batchRows = 0;
            long nextReadTime = System.currentTimeMillis();
            long waitMs;
            Iterator<T> cur = Iterator.empty();

            private void nextCur() {
                cur = null;
                cur = Iterator.fromJava(partition.values.iterator());
            }

            @Override
            public boolean hasNext() {
                if(rows >= rowsForSubtask){
                    return false;
                }
                while (!cur.hasNext()) {
                    nextCur();
                }
                return true;
            }

            @Override
            public T next() {
                if (hasNext()) {
                    controlSpeed();
                    rows += 1;
                    return cur.next();
                } else {
                    return (T) empty.next();
                }
            }

            private void controlSpeed(){
                if(millisPerRow > 0){
                    if(rows != 0){
                        try {
                            Thread.sleep(millisPerRow);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }else{
                    batchRows += 1;
                    if(batchRows > rowsPerSecondForSubtask){
                        //System.out.println(rows + ","+ batchRows);
                        batchRows = 1;
                        nextReadTime += 1000;
                        waitMs = Math.max(0, nextReadTime - System.currentTimeMillis());
                        if(waitMs > 0) {
                            try {
                                Thread.sleep(waitMs);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        };
    }

    private long getRowsPerSecondForSubTask(int numSubtasks, int indexOfThisSubtask) {
        long baseRowsPerSecondPerSubtask = rowsPerSecond / numSubtasks;
        return (rowsPerSecond % numSubtasks > indexOfThisSubtask) ? baseRowsPerSecondPerSubtask + 1 : baseRowsPerSecondPerSubtask;
    }

    private long getRowsForSubTask(int numSubtasks, int indexOfThisSubtask) {
        if (numberOfRows < 0) {
            return Long.MAX_VALUE;
        } else {
            final long baseNumOfRowsPerSubtask = numberOfRows / numSubtasks;
            return (numberOfRows % numSubtasks > indexOfThisSubtask) ? baseNumOfRowsPerSubtask + 1 : baseNumOfRowsPerSubtask;
        }
    }

}
