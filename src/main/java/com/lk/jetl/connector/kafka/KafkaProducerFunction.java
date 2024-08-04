package com.lk.jetl.connector.kafka;

import com.lk.jetl.functions.RichSinkFunction;
import com.lk.jetl.serialization.SerializationSchema;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaProducerFunction<T> extends RichSinkFunction<T> {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaProducerFunction.class.getSimpleName());
    private final String topic;
    private final SerializationSchema<T> serialization;
    private final Properties props;
    private transient KafkaProducer<byte[], byte[]> producer;

    public KafkaProducerFunction(String topic, SerializationSchema<T> serialization, Properties props) {
        this.topic = topic;
        this.serialization = serialization;
        this.props = props;
    }

    @Override
    public void open() throws Exception {
        serialization.open();
        producer = new KafkaProducer<>(props, new ByteArraySerializer(), new ByteArraySerializer());
    }

    @Override
    public void invoke(T value) {
        byte[] bytes = serialization.serialize(value);
        producer.send(new ProducerRecord<>(topic, bytes));
    }

    @Override
    public void close() throws Exception {
        serialization.close();
        if (producer != null) {
            producer.close();
        }
    }
}
