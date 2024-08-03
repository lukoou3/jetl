package com.lk.jetl.sql.connector;

import com.lk.jetl.sql.DataFrame;

import java.io.Serializable;

public interface TransformProvider extends Serializable {
    DataFrame transform(DataFrame[] dependencies);
}
