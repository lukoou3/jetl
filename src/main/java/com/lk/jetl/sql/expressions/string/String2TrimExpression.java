package com.lk.jetl.sql.expressions.string;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.AbstractDataType;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.Types;
import com.lk.jetl.util.Option;

import java.util.ArrayList;
import java.util.List;

public abstract class String2TrimExpression extends Expression {
    protected final Expression srcStr;
    protected final Option<Expression> trimStr;

    public String2TrimExpression(Expression srcStr, Option<Expression> trimStr) {
        this.srcStr = srcStr;
        this.trimStr = trimStr;
        this.args = new Object[]{srcStr, trimStr};
    }

    @Override
    public List<Expression> getChildren() {
        List<Expression> children = new ArrayList<>(trimStr.isEmpty()?1:2);
        children.add(srcStr);
        if(trimStr.isDefined()){
            children.add(trimStr.get());
        }
        return children;
    }

    @Override
    public DataType getDataType() {
        return Types.STRING;
    }

    @Override
    public boolean expectsInputTypes() {
        return true;
    }

    @Override
    public List<AbstractDataType> inputTypes() {
        int size = getChildren().size();
        List<AbstractDataType> tps = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            tps.add(Types.STRING);
        }
        return tps;
    }

}
