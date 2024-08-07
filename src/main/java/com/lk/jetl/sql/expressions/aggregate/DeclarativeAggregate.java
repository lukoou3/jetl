package com.lk.jetl.sql.expressions.aggregate;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.expressions.AttributeReference;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.StructType;
import com.lk.jetl.sql.types.StructType.StructField;

import java.util.List;

public abstract class DeclarativeAggregate extends AggregateFunction {

    public abstract List<Expression> initialValues();
    public abstract List<Expression> updateExpressions();
    public abstract Expression evaluateExpression();

    @Override
    public final StructType aggBufferSchema() {
        List<AttributeReference> attributes = aggBufferAttributes();
        return new StructType(attributes.stream().map(x -> new StructField(x.name, x.dataType)).toArray(StructField[]::new));
    }

    @Override
    public final Object eval(Row input) {
        throw new UnsupportedOperationException("Cannot evaluate expression: " + this);
    }
}
