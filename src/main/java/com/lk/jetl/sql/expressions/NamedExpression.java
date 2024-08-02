package com.lk.jetl.sql.expressions;

public interface NamedExpression {
    String name();

    // 暂时不携带数据库和表的名称
    //List<String> qualifier();

}
