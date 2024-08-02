package com.lk.jetl.sql.functions;

import com.lk.jetl.functions.RichFilterFunction;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.expressions.Expression;

public class Predicate extends RichFilterFunction<Row> {
    final Expression predicate;

    public Predicate(Expression predicate) {
        this.predicate = predicate;
    }

    @Override
    public void open() throws Exception {
        predicate.open();
    }

    @Override
    public boolean filter(Row value) {
        return Boolean.TRUE.equals(predicate.eval(value));
    }

    @Override
    public void close() throws Exception {
        predicate.close();
    }

}
