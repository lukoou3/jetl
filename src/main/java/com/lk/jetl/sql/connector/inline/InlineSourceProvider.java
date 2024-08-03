package com.lk.jetl.sql.connector.inline;

import com.lk.jetl.connector.inline.InlineRDS;
import com.lk.jetl.sql.DataFrame;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.connector.SourceProvider;
import com.lk.jetl.sql.types.StructType;

import java.util.List;

public class InlineSourceProvider implements SourceProvider {
    private final StructType dataType;
    private final List<Row> datas;
    private final int parallelism;
    private final int rowsPerSecond;
    private final long numberOfRows;
    private final long millisPerRow;

    public InlineSourceProvider(StructType dataType, List<Row> datas, int parallelism, int rowsPerSecond, long numberOfRows, long millisPerRow) {
        this.dataType = dataType;
        this.datas = datas;
        this.parallelism = parallelism;
        this.rowsPerSecond = rowsPerSecond;
        this.numberOfRows = numberOfRows;
        this.millisPerRow = millisPerRow;
    }

    @Override
    public DataFrame getDataFrame() {
        return new DataFrame(new InlineRDS<>(datas, parallelism, rowsPerSecond, numberOfRows, millisPerRow), dataType);
    }
}
