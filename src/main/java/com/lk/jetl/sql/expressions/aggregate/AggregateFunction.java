package com.lk.jetl.sql.expressions.aggregate;

import com.lk.jetl.sql.expressions.AttributeReference;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.StructType;

import java.util.List;

public abstract class AggregateFunction extends Expression {

    /** An aggregate function is not foldable. */
    @Override
    public final boolean isFoldable() {
        return false;
    }

    public abstract StructType aggBufferSchema();

    public abstract List<AttributeReference> aggBufferAttributes();


}
