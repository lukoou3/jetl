package com.lk.jetl.sql.factories;


import com.lk.jetl.configuration.ReadonlyConfig;
import com.lk.jetl.format.EncodingFormat;
import com.lk.jetl.sql.Row;

import java.util.Map;

public interface EncodingFormatFactory extends FormatFactory {
    EncodingFormat<Row> createEncodingFormat(ReadonlyConfig options);
}
