package com.lk.jetl.execution;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.lk.jetl.configuration.ReadonlyConfig;
import com.lk.jetl.configuration.util.ConfigValidator;
import com.lk.jetl.sql.DataFrame;
import com.lk.jetl.sql.connector.SourceProvider;
import com.lk.jetl.sql.factories.FactoryUtil;
import com.lk.jetl.sql.factories.SourceTableFactory;
import com.lk.jetl.sql.factories.TableFactory;
import com.lk.jetl.sql.types.StructType;
import com.lk.jetl.sql.types.Types;
import com.typesafe.config.Config;

import java.util.Map;

public class SourceNode implements Node {
    public final String name;
    public final String type;
    @JsonSerialize(using = ToStringSerializer.class)
    public final StructType schema;
    public final Map<String, Object> options;

    public SourceNode(String name, String type, Config config) {
        this.name = name;
        this.type = type;
        this.schema = Types.parseStructType(config.getString("schema"));
        this.options = config.getConfig("options").root().unwrapped();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public DataFrame execute() {
        SourceTableFactory sourceTableFactory = FactoryUtil.discoverTableFactory(SourceTableFactory.class, type);
        TableFactory.Context context = new TableFactory.Context(schema, schema, ReadonlyConfig.fromMap(options));
        ConfigValidator.of(context.getOptions()).validate(sourceTableFactory.optionRule());
        SourceProvider sourceProvider = sourceTableFactory.getSourceProvider(context);
        return sourceProvider.getDataFrame();
    }

    @Override
    public String toString() {
        return "SourceNode{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", schema=" + schema +
                ", options=" + options +
                '}';
    }
}
