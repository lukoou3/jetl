package com.lk.jetl.sql.transform.query;

import com.lk.jetl.sql.DataFrame;
import com.lk.jetl.sql.connector.TransformProvider;
import com.lk.jetl.sql.factories.TransformFactory;
import com.lk.jetl.sql.types.StructType;

import java.util.Map;

public class QueryTransformFactory implements TransformFactory {
    public static final String IDENTIFIER = "query";

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public TransformProvider getTransformProvider(StructType[] dependencies, Map<String, Object> options) {
        assert dependencies.length == 1;
        final String sql = options.get("sql").toString();
        return new TransformProvider() {
            @Override
            public DataFrame transform(DataFrame[] dependencies) {
                DataFrame df = dependencies[0];
                return df.query(sql);
            }
        };
    }
}
