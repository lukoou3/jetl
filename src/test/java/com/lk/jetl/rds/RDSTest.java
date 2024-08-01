package com.lk.jetl.rds;

import com.lk.jetl.connector.kafka.KafkaConsumerRDS;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

public class RDSTest{
    @Test
    public void test() throws Exception {
        List<Integer> datas = List.of(1, 2, 3, 4, 5);
        RDS<Integer> rds1 = new ParallelCollectionRDS<>(datas, 2);
        RDS<Integer> rds2 = rds1.filter(x -> x % 2 == 0).map(x -> x * 100);
        Partition[] partitions = rds2.getPartitions();
        for (int i = 0; i < partitions.length; i++) {
            System.out.println(i + ":");
            rds2.compute(partitions[i]).forEach(x->{
                System.out.println(x);
            });
        }
    }

    @Test
    public void testKafkaConsumerRDS() throws Exception {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.144.112:9092");
        props.put("enable.auto.commit", "false");
        props.put("auto.offset.reset", "latest");
        props.put("kafka.session.timeout.ms", "60000");
        props.put("max.poll.records", "1000");
        props.put("group.id", "test");
        RDS<String> rds = new KafkaConsumerRDS<byte[]>(1, "OBJECT-STATISTICS-METRIC", props)
                .map(x -> new String(x, StandardCharsets.UTF_8))
                .filter(x -> x.contains("102"))
                ;
        Partition partition = rds.getPartitions()[0];
        rds.open();
        rds.compute(partition).forEach(x->{
            System.out.println(x);
        });
        rds.close();
    }
}