package com.lk.jetl.sql.expressions;

import com.google.common.base.Preconditions;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.api.UDF;
import com.lk.jetl.sql.types.AbstractDataType;
import com.lk.jetl.sql.types.AtomicType;
import com.lk.jetl.sql.types.DataType;

import java.util.List;
import java.util.function.Function;

public class UDFExpression extends Expression {
    public final UDF function;
    public final List<Expression> children;
    public final int argCount;
    transient Function<Object, Object>[] argConverters;
    transient Function<Object, Object> resultConverter;

    public UDFExpression(UDF function, List<Expression> children) {
        this.function = function;
        this.children = children;
        this.argCount = function.inputTypes().size();
        this.args = new Object[]{function, children};
        Preconditions.checkArgument(argCount == children.size());
    }

    @Override
    public List<Expression> getChildren() {
        return children;
    }

    @Override
    public DataType getDataType() {
        return function.dataType();
    }

    @Override
    public boolean expectsInputTypes() {
        return true;
    }

    @Override
    public List<AbstractDataType> inputTypes() {
        return function.inputTypes();
    }

    @Override
    public Object eval(Row input) {
        if (argConverters == null) {
            argConverters = new Function[argCount];
            for (int i = 0; i < argCount; i++) {
                argConverters[i] = createToJavaConverter(children.get(i).getDataType());
            }
            resultConverter = createToCatalystConverter(getDataType());
        }

        Object[] args = new Object[argCount];
        for (int i = 0; i < argCount; i++) {
            args[i] = argConverters[i].apply(children.get(i).eval(input));
        }

        return resultConverter.apply(function.call(args));
    }

    private Function<Object, Object> createToJavaConverter(DataType dataType) {
        if (dataType instanceof AtomicType) {
            return x -> x;
        }

        throw new UnsupportedOperationException("can not convert type:" + dataType);
    }

    private Function<Object, Object> createToCatalystConverter(DataType dataType) {
        if (dataType instanceof AtomicType) {
            return x -> x;
        }

        throw new UnsupportedOperationException("can not convert type:" + dataType);
    }
}
