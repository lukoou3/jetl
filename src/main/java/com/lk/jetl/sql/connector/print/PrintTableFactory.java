package com.lk.jetl.sql.connector.print;

import com.lk.jetl.functions.SinkFunction;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.connector.SinkProvider;
import com.lk.jetl.sql.factories.SinkTableFactory;
import com.lk.jetl.sql.formats.json.JsonRowSerializationSchema;
import com.lk.jetl.sql.types.StructType;

import java.util.Map;

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
        Map<String, Object> options = context.getOptions();
        PrintRowSinkFunction sinkFunction = new PrintRowSinkFunction(dataType,
                new JsonRowSerializationSchema(dataType), STDOUT);
        return new SinkProvider() {
            @Override
            public SinkFunction<Row> getSinkFunction() {
                return sinkFunction;
            }
        };
    }
}
