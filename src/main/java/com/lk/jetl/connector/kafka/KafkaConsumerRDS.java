package com.lk.jetl.connector.kafka;

import com.lk.jetl.rds.Partition;
import com.lk.jetl.rds.RDS;
import com.lk.jetl.rds.SimplePartition;
import com.lk.jetl.util.Iterator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerRDS<T> extends RDS<T> {
    final int parallelism;
    final String topic;
    final Properties props;
    transient KafkaConsumer<byte[], byte[]> consumer;

    public KafkaConsumerRDS(int parallelism, String topic, Properties props) {
        this.parallelism = parallelism;
        this.topic = topic;
        this.props = props;
    }

    @Override
    public Partition[] getPartitions() {
        return SimplePartition.simplePartitions(this, parallelism);
    }

    @Override
    public void open() throws Exception {
        consumer = new KafkaConsumer<>(props, new ByteArrayDeserializer(), new ByteArrayDeserializer());
        consumer.subscribe(Collections.singletonList(topic));
    }

    @Override
    public Iterator<T> compute(Partition split) {
        return new Iterator<T>() {
            Iterator<ConsumerRecord<byte[], byte[]>> recordIter = Iterator.empty();

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                if (recordIter.hasNext()) {
                    return (T) recordIter.next().value();
                }

                do {
                    recordIter = Iterator.fromJava(consumer.poll(Duration.ofMillis(250)).iterator());
                } while (!recordIter.hasNext());

                return (T) recordIter.next().value();
            }
        };
    }

    @Override
    public void close() throws Exception {
        if (consumer != null) {
            consumer.close();
        }
    }
}
