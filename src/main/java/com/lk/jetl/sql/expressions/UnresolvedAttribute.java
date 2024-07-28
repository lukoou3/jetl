package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.types.DataType;

import java.util.List;
import java.util.stream.Collectors;

public class UnresolvedAttribute extends LeafExpression{
    public final List<String> nameParts;

    public UnresolvedAttribute(List<String> nameParts) {
        this.nameParts = nameParts;
        this.args = new Object[]{nameParts};
    }

    public String name(){
        return nameParts.stream().map(x -> x.contains(".")? "`" + x + "`":x).collect(Collectors.joining("."));
    }

    @Override
    public String toString() {
        return "'" + name();
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
