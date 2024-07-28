package com.lk.jetl.sql.expressions.conditional;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.analysis.TypeCheckResult;
import com.lk.jetl.sql.analysis.TypeCoercion;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.Types;

import java.util.Arrays;
import java.util.List;

public class If extends Expression {
    public final Expression predicate;
    public final Expression trueValue;
    public final Expression falseValue;

    public If(Expression predicate, Expression trueValue, Expression falseValue) {
        this.predicate = predicate;
        this.trueValue = trueValue;
        this.falseValue = falseValue;
        this.args = new Object[]{predicate, trueValue, falseValue};
    }

    @Override
    public List<Expression> getChildren() {
        return Arrays.asList(predicate, trueValue, falseValue);
    }

    @Override
    public TypeCheckResult checkInputDataTypes() {
        if (!predicate.getDataType().equals(Types.BOOLEAN)) {
            return TypeCheckResult.typeCheckFailure("type of predicate expression in If should be boolean");
        } else if (!TypeCoercion.haveSameType(Arrays.asList(trueValue.getDataType(), falseValue.getDataType()))) {
            return TypeCheckResult.typeCheckFailure(String.format("differing types:%s and %s", trueValue.getDataType(), falseValue.getDataType()));
        } else {
            return TypeCheckResult.typeCheckSuccess();
        }
    }

    @Override
    public Object eval(Row input) {
        if (Boolean.TRUE.equals(predicate.eval(input))) {
            return trueValue.eval(input);
        } else {
            return falseValue.eval(input);
        }
    }

    @Override
    public DataType getDataType() {
        return trueValue.getDataType();
    }
}
