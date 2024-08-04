package com.lk.jetl.sql.connector.inline;

import com.lk.jetl.configuration.Option;
import com.lk.jetl.configuration.Options;

public class InlineConnectorOptions {
    public static final Option<String> DATA = Options.key("data")
            .stringType()
            .noDefaultValue()
            .withDescription("inline source的输入数据");
    public static final Option<InlineDataType> TYPE = Options.key("data_type")
            .objectType(InlineDataType.class)
            .defaultValue(InlineDataType.STRING)
            .withDescription("数据类型:string(UTF8字符串),hex(十六进制编码),base64(base64编码)");

    public static final Option<Integer> ROWS_PER_SECOND =
            Options.key("rows_per_second")
                    .intType()
                    .defaultValue(1000)
                    .withDescription("Rows per second to control the emit rate.");

    public static final Option<Long> NUMBER_OF_ROWS =
            Options.key("number_of_rows")
                    .longType()
                    .defaultValue(-1L)
                    .withDescription("Total number of rows to emit. By default, the source is unbounded.");

    public static final Option<Long> MILLIS_PER_ROW =
            Options.key("millis_per_row")
                    .longType()
                    .defaultValue(0L)
                    .withDescription("millis per row to control the emit rate.");
}
