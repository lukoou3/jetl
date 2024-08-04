package com.lk.jetl.sql.connector.print;

import com.lk.jetl.configuration.Option;
import com.lk.jetl.configuration.Options;
import com.lk.jetl.configuration.ReadonlyConfig;
import com.lk.jetl.configuration.util.OptionRule;
import com.lk.jetl.format.EncodingFormat;
import com.lk.jetl.functions.SinkFunction;
import com.lk.jetl.serialization.SerializationSchema;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.connector.SinkProvider;
import com.lk.jetl.sql.factories.EncodingFormatFactory;
import com.lk.jetl.sql.factories.FactoryUtil;
import com.lk.jetl.sql.factories.SinkTableFactory;
import com.lk.jetl.sql.types.StructType;

import static com.lk.jetl.sql.connector.print.PrintMode.STDOUT;

public class PrintTableFactory implements SinkTableFactory {
    public static final String IDENTIFIER = "print";

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public SinkProvider getSinkProvider(Context context) {
        StructType dataType = context.getSchema();
        ReadonlyConfig options = context.getOptions();
        EncodingFormat<Row> encodingFormat = FactoryUtil.discoverEncodingFormat(EncodingFormatFactory.class, ReadonlyConfig.fromMap(options.get(FactoryUtil.FORMAT)),options.get(FactoryUtil.FORMAT_TYPE));
        SerializationSchema<Row> serialization = encodingFormat.createRuntimeEncoder(dataType);
        PrintRowSinkFunction sinkFunction = new PrintRowSinkFunction(dataType, serialization, options.get(MODE));
        return new SinkProvider() {
            @Override
            public SinkFunction<Row> getSinkFunction() {
                return sinkFunction;
            }
        };
    }

    @Override
    public OptionRule optionRule() {
        return OptionRule.builder().optional(MODE).build();
    }

    public static final Option<PrintMode> MODE =
            Options.key("mode")
                    .objectType(PrintMode.class)
                    .defaultValue(STDOUT)
                    .withDescription("输出模式:stdout,log_info,log_warn,null");
}
