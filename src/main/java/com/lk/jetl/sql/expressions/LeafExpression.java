package com.lk.jetl.sql.expressions;

import java.util.Collections;
import java.util.List;

public abstract class LeafExpression extends Expression {
    @Override
    public List<Expression> getChildren() {
        return Collections.emptyList();
    }
}
