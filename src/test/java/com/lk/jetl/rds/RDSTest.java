package com.lk.jetl.rds;

import com.lk.jetl.connector.kafka.KafkaConsumerRDS;
import com.lk.jetl.exec.JEtlContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

public class RDSTest{
    static final Logger LOG = LoggerFactory.getLogger(RDSTest.class);
    private <T> T copyBySerialize(T obj) throws Exception{
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                 ObjectInputStream in = new ObjectInputStream(bis)) {
                return (T) in.readObject();
            }
        }
    }

    @Test
    public void test() throws Exception {
        List<Integer> datas = List.of(1, 2, 3, 4, 5);
        RDS<Integer> rds1 = new ParallelCollectionRDS<>(datas, 2);
        RDS<Integer> rds2 = rds1.filter(x -> x % 2 == 0).map(x -> x * 100);
        RDS<Void> rst = rds2.sink(x -> LOG.warn(x.toString()));
        JEtlContext.runJob(rst);
    }

    @Test
    public void testKafkaConsumerRDS() throws Exception {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.44.12:9092");
        props.put("enable.auto.commit", "false");
        props.put("auto.offset.reset", "latest");
        props.put("kafka.session.timeout.ms", "60000");
        props.put("max.poll.records", "1000");
        props.put("group.id", "test");
        RDS<String> rds = new KafkaConsumerRDS<byte[]>(1, "OBJECT-STATISTICS-METRIC", props)
                .map(x -> new String(x, StandardCharsets.UTF_8))
                .filter(x -> x.contains("102"))
                ;
        RDS<Void> rst = rds.sink(x -> LOG.warn(x));
        JEtlContext.runJob(rst);
    }
}