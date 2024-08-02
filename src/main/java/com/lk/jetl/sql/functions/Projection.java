package com.lk.jetl.sql.functions;

import com.lk.jetl.functions.RichMapFunction;
import com.lk.jetl.sql.GenericRow;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.expressions.Expression;

public class Projection extends RichMapFunction<Row, Row> {
    final Expression[] projects;

    public Projection(Expression[] projects) {
        this.projects = projects;
    }

    @Override
    public void open() throws Exception {
        for (Expression project : projects) {
            project.open();
        }
    }

    @Override
    public Row map(Row input) {
        Row row = new GenericRow(projects.length);
        for (int i = 0; i < projects.length; i++) {
            row.update(i, projects[i].eval(input));
        }
        return row;
    }

    @Override
    public void close() throws Exception {
        for (Expression project : projects) {
            project.close();
        }
    }
}
