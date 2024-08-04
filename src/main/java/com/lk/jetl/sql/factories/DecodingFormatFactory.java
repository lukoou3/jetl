package com.lk.jetl.sql.factories;



import com.lk.jetl.configuration.ReadonlyConfig;
import com.lk.jetl.format.DecodingFormat;
import com.lk.jetl.sql.Row;

import java.util.Map;

public interface DecodingFormatFactory extends FormatFactory {
    DecodingFormat<Row> createDecodingFormat(ReadonlyConfig options);
}
