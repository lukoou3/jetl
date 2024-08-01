package com.lk.jetl.sql.expressions.string;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.TernaryExpression;
import com.lk.jetl.sql.types.AbstractDataType;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.Types;

import java.util.List;

public class StringReplace extends TernaryExpression {
    private final Expression srcStr;
    private final Expression searchExpr;
    private final Expression replaceExpr;

    public StringReplace(Expression srcStr, Expression searchExpr, Expression replaceExpr) {
        this.srcStr = srcStr;
        this.searchExpr = searchExpr;
        this.replaceExpr = replaceExpr;
        this.args = new Object[]{srcStr, searchExpr, replaceExpr};
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(srcStr, searchExpr, replaceExpr);
    }

    @Override
    public boolean expectsInputTypes() {
        return true;
    }

    @Override
    public List<AbstractDataType> inputTypes() {
        return List.of(Types.STRING, Types.STRING, Types.STRING);
    }

    @Override
    public DataType getDataType() {
        return Types.STRING;
    }

    @Override
    public String prettyName() {
        return "replace";
    }

    @Override
    protected Object nullSafeEval(Object srcEval, Object searchEval, Object replaceEval) {
        return ((String) srcEval).replace((String) searchEval, (String) replaceEval);
    }
}
