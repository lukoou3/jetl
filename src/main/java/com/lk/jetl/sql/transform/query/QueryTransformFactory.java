package com.lk.jetl.sql.transform.query;

import com.lk.jetl.configuration.Option;
import com.lk.jetl.configuration.Options;
import com.lk.jetl.configuration.ReadonlyConfig;
import com.lk.jetl.configuration.util.OptionRule;
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
    public TransformProvider getTransformProvider(StructType[] dependencies, ReadonlyConfig options) {
        assert dependencies.length == 1;
        final String sql = options.get(SQL);
        return new TransformProvider() {
            @Override
            public DataFrame transform(DataFrame[] dependencies) {
                DataFrame df = dependencies[0];
                return df.query(sql);
            }
        };
    }

    @Override
    public OptionRule optionRule() {
        return OptionRule.builder().required(SQL).build();
    }

    public static final Option<String> SQL = Options.key("sql")
        .stringType()
        .noDefaultValue()
        .withDescription("simple sql");
}
