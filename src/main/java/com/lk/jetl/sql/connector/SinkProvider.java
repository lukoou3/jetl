package com.lk.jetl.sql.connector;

import com.lk.jetl.functions.SinkFunction;
import com.lk.jetl.sql.Row;

import java.io.Serializable;

public interface SinkProvider extends Serializable {

    SinkFunction<Row> getSinkFunction();

}
