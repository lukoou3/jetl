package com.lk.jetl;

import com.lk.jetl.functions.SinkFunction;
import com.lk.jetl.rds.Partition;
import com.lk.jetl.rds.RDS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JEtlContext {

    public static <T> void runJob(RDS<T> rds) throws Exception {
        Partition[] partitions = rds.getPartitions();
        //RDS<T>[] instances = new RDS[partitions.length];
        Thread[] threads = new Thread[partitions.length];
        for (int i = 0; i < partitions.length; i++) {
            Partition partition = partitions[i];
            RDS<T> instance = copyBySerialize(rds);
            threads[i] = new Thread(()->{
                Exception error = null;
                try {
                    instance.open();
                    instance.compute(partition);
                } catch (Exception e) {
                    error = e;
                }finally {
                    try {
                        instance.close();
                    } catch (Exception e) {
                        error = e;
                    }
                }
                if(error != null){
                    error.printStackTrace();
                }
            }, instance.toString() + ":" + (i + 1) + "/" + partitions.length);
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
    }

    private static <T> T copyBySerialize(T obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                 ObjectInputStream in = new ObjectInputStream(bis)) {
                return (T) in.readObject();
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
