package com.lk.jetl.execution;

import com.lk.jetl.sql.DataFrame;
import com.lk.jetl.sql.connector.TransformProvider;
import com.lk.jetl.sql.factories.FactoryUtil;
import com.lk.jetl.sql.factories.TransformFactory;
import com.lk.jetl.sql.types.StructType;
import com.typesafe.config.Config;

import java.util.Arrays;
import java.util.Map;

public class TransformNode implements Node {
    public final String name;
    public final String type;
    public final Map<String, Object> options;
    public final Node[] dependencies;
    public TransformNode(String name, String type, Config config, Node[] dependencies) {
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
        TransformFactory transformFactory = FactoryUtil.discoverTransformFactory(TransformFactory.class, type);
        StructType[] depSchemas = new StructType[dependencies.length];
        DataFrame[] depDfs = new DataFrame[dependencies.length];
        for (int i = 0; i < dependencies.length; i++) {
            depDfs[i] = dependencies[i].execute();
            depSchemas[i] = depDfs[i].getSchema();
        }
        TransformProvider transformProvider = transformFactory.getTransformProvider(depSchemas, options);
        return transformProvider.transform(depDfs);
    }

    @Override
    public String toString() {
        return "TransformNode{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", options=" + options +
                ", dependencies=" + Arrays.toString(dependencies) +
                '}';
    }
}
