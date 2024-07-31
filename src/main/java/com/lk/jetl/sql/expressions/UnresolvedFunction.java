package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.types.DataType;

import java.util.List;
import java.util.stream.Collectors;

public class UnresolvedFunction extends Expression {
    public final String name;
    public final List<Expression> arguments;

    public UnresolvedFunction(String name, List<Expression> arguments) {
        this.name = name;
        this.arguments = arguments;
        this.args = new Object[]{name, arguments};
    }

    @Override
    public List<Expression> getChildren() {
        return arguments;
    }

    @Override
    public String toString() {
        return String.format("'%s(%s)", name, arguments.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    }

    @Override
    public boolean isResolved() {
        return false;
    }

    @Override
    public boolean isFoldable() {
        return false;
    }

    @Override
    public Object eval(Row input) {
        throw new UnsupportedOperationException("Unresolved");
    }

    @Override
    public DataType getDataType() {
        throw new UnsupportedOperationException("Unresolved");
    }
}
