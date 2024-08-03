package com.lk.jetl.sql.connector.print;

import com.lk.jetl.functions.RichSinkFunction;
import com.lk.jetl.serialization.SerializationSchema;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static com.lk.jetl.sql.connector.print.PrintMode.*;

public class PrintRowSinkFunction extends RichSinkFunction<Row> {
    private static final Logger LOG = LoggerFactory.getLogger(PrintRowSinkFunction.class.getSimpleName());
    private final StructType dataType;
    private final SerializationSchema<Row> serialization;
    private final PrintMode printMode;

    public PrintRowSinkFunction(StructType dataType, SerializationSchema<Row> serialization, PrintMode printMode) {
        this.dataType = dataType;
        this.serialization = serialization;
        this.printMode = printMode;
    }

    @Override
    public void open() throws Exception {
        serialization.open();
    }

    @Override
    public void invoke(Row value) {
        byte[] bytes = serialization.serialize(value);
        String msg = new String(bytes, StandardCharsets.UTF_8);
        if(printMode == STDOUT){
            System.out.println(msg);
        } else if (printMode == LOG_INFO) {
            LOG.info(msg);
        } else if (printMode == LOG_WARN) {
            LOG.warn(msg);
        }
    }

    @Override
    public void close() throws Exception {
        serialization.close();
    }
}
