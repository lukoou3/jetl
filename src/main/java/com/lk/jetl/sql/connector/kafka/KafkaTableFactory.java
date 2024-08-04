package com.lk.jetl.sql.connector.kafka;

import com.lk.jetl.configuration.Option;
import com.lk.jetl.configuration.Options;
import com.lk.jetl.configuration.ReadonlyConfig;
import com.lk.jetl.configuration.util.OptionRule;
import com.lk.jetl.connector.kafka.KafkaConsumerRDS;
import com.lk.jetl.connector.kafka.KafkaProducerFunction;
import com.lk.jetl.format.DecodingFormat;
import com.lk.jetl.format.EncodingFormat;
import com.lk.jetl.functions.SinkFunction;
import com.lk.jetl.serialization.DeserializationSchema;
import com.lk.jetl.serialization.SerializationSchema;
import com.lk.jetl.sql.DataFrame;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.connector.SinkProvider;
import com.lk.jetl.sql.connector.SourceProvider;
import com.lk.jetl.sql.factories.*;
import com.lk.jetl.sql.types.StructType;

import java.util.Map;
import java.util.Properties;

public class KafkaTableFactory implements SourceTableFactory, SinkTableFactory {
    public static final String IDENTIFIER = "kafka";

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public SourceProvider getSourceProvider(Context context) {
        StructType dataType = context.getPhysicalDataType();
        ReadonlyConfig options = context.getOptions();
        int parallelism = options.get(FactoryUtil.PARALLELISM);
        String topic = options.get(TOPIC);
        DecodingFormat<Row> decodingFormat = FactoryUtil.discoverDecodingFormat(DecodingFormatFactory.class, ReadonlyConfig.fromMap(options.get(FactoryUtil.FORMAT)), options.get(FactoryUtil.FORMAT_TYPE));
        DeserializationSchema<Row> deserialization = decodingFormat.createRuntimeDecoder(dataType);
        Properties properties = getProperties(options);
        DataFrame df = new DataFrame(new KafkaConsumerRDS<>(parallelism, topic, deserialization, properties), dataType);
        return () -> df;
    }

    @Override
    public SinkProvider getSinkProvider(Context context) {
        StructType dataType = context.getSchema();
        ReadonlyConfig options = context.getOptions();
        EncodingFormat<Row> encodingFormat = FactoryUtil.discoverEncodingFormat(EncodingFormatFactory.class, ReadonlyConfig.fromMap(options.get(FactoryUtil.FORMAT)),options.get(FactoryUtil.FORMAT_TYPE));
        SerializationSchema<Row> serialization = encodingFormat.createRuntimeEncoder(dataType);
        String topic = options.get(TOPIC);
        Properties properties = getProperties(options);
        SinkFunction<Row> sinkFunction = new KafkaProducerFunction<>(topic, serialization, properties);
        return () -> sinkFunction;
    }

    private Properties getProperties(ReadonlyConfig options){
        Map<String, String> props = options.get(PROPERTIES);
        Properties properties = new Properties();
        props.forEach(properties::put);
        return properties;
    }

    @Override
    public OptionRule optionRule() {
        return OptionRule.builder()
                .required(TOPIC, PROPERTIES)
                .optional(FactoryUtil.PARALLELISM, FactoryUtil.FORMAT)
                .build();
    }

    public static final Option<String> TOPIC =
            Options.key("topic")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Topic name from which the table is read.");

    public static final Option<Map<String, String>> PROPERTIES = Options.key("properties")
            .mapType()
            .noDefaultValue()
            .withDescription("Kafka properties.");
}
