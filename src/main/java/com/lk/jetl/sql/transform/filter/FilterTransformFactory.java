package com.lk.jetl.sql.transform.filter;

import com.lk.jetl.configuration.Option;
import com.lk.jetl.configuration.Options;
import com.lk.jetl.configuration.ReadonlyConfig;
import com.lk.jetl.configuration.util.OptionRule;
import com.lk.jetl.sql.DataFrame;
import com.lk.jetl.sql.connector.TransformProvider;
import com.lk.jetl.sql.factories.TransformFactory;
import com.lk.jetl.sql.types.StructType;

import java.util.Map;

public class FilterTransformFactory implements TransformFactory {
    public static final String IDENTIFIER = "filter";

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public TransformProvider getTransformProvider(StructType[] dependencies, ReadonlyConfig options) {
        assert dependencies.length == 1;
        final String condition = options.get(Options.key("condition").stringType().noDefaultValue());
        return new TransformProvider() {
            @Override
            public DataFrame transform(DataFrame[] dependencies) {
                DataFrame df = dependencies[0];
                return df.filter(condition);
            }
        };
    }

    @Override
    public OptionRule optionRule() {
        return OptionRule.builder().required(CONDITION).build();
    }

    public static final Option<String> CONDITION = Options.key("condition")
            .stringType()
            .noDefaultValue()
            .withDescription("condition sql");
}
