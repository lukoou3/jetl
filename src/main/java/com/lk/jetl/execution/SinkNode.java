package com.lk.jetl.execution;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.lk.jetl.configuration.ReadonlyConfig;
import com.lk.jetl.sql.DataFrame;
import com.lk.jetl.sql.connector.SinkProvider;
import com.lk.jetl.sql.factories.FactoryUtil;
import com.lk.jetl.sql.factories.SinkTableFactory;
import com.lk.jetl.sql.factories.TableFactory;
import com.lk.jetl.sql.types.StructType;
import com.typesafe.config.Config;

import java.util.Arrays;
import java.util.Map;

public class SinkNode implements Node {
    public final String name;
    public final String type;
    public final Map<String, Object> options;
    public final Node[] dependencies;
    public SinkNode(String name, String type, Config config, Node[] dependencies) {
        this.name = name;
        this.type = type;
        this.options = config.getConfig("options").root().unwrapped();
        this.dependencies = dependencies;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public DataFrame execute() {
        DataFrame df = dependencies[0].execute();
        StructType schema = df.getSchema();
        SinkTableFactory sinkTableFactory = FactoryUtil.discoverTableFactory(SinkTableFactory.class, type);
        TableFactory.Context context = new TableFactory.Context(schema, schema, ReadonlyConfig.fromMap(options));
        SinkProvider sinkProvider = sinkTableFactory.getSinkProvider(context);
        return new DataFrame(df.sink(sinkProvider.getSinkFunction()), new StructType(new StructType.StructField[0]));
    }

    @Override
    public String toString() {
        return "SinkNode{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", options=" + options +
                ", dependencies=" + Arrays.toString(dependencies) +
                '}';
    }
}
