package com.lk.jetl.sql.formats.json;


import com.lk.jetl.configuration.Option;
import com.lk.jetl.configuration.Options;
import com.lk.jetl.configuration.ReadonlyConfig;
import com.lk.jetl.configuration.util.OptionRule;
import com.lk.jetl.format.DecodingFormat;
import com.lk.jetl.format.EncodingFormat;
import com.lk.jetl.serialization.DeserializationSchema;
import com.lk.jetl.serialization.SerializationSchema;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.factories.DecodingFormatFactory;
import com.lk.jetl.sql.factories.EncodingFormatFactory;
import com.lk.jetl.sql.types.StructType;

import java.util.Map;

public class JsonFormatFactory implements DecodingFormatFactory, EncodingFormatFactory {
    public static final String IDENTIFIER = "json";

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }


    @Override
    public DecodingFormat<Row> createDecodingFormat(ReadonlyConfig options) {
        final boolean ignoreParseErrors = options.get(IGNORE_PARSE_ERRORS);
        return new DecodingFormat<>() {
            @Override
            public DeserializationSchema<Row> createRuntimeDecoder(StructType dataType) {
                return new JsonRowDeserializationSchema(dataType, ignoreParseErrors);
            }
        };
    }

    @Override
    public EncodingFormat<Row> createEncodingFormat(ReadonlyConfig options) {
        return new EncodingFormat<>() {
            @Override
            public SerializationSchema<Row> createRuntimeEncoder(StructType dataType) {
                return new JsonRowSerializationSchema(dataType);
            }
        };
    }

    @Override
    public OptionRule optionRule() {
        return OptionRule.builder().optional(IGNORE_PARSE_ERRORS).build();
    }

    public static final Option<Boolean> IGNORE_PARSE_ERRORS = Options.key("ignore_parse_errors")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("Optional flag to skip fields and rows with parse errors instead of failing;\n"
                            + "fields are set to null in case of errors, false by default.");
}
