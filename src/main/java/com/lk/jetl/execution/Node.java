package com.lk.jetl.execution;

import com.lk.jetl.sql.DataFrame;

public interface Node {

    String name();

    DataFrame execute();


}
