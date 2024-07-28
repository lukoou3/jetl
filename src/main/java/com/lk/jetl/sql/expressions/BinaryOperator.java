package com.lk.jetl.sql.expressions;


import com.lk.jetl.sql.analysis.TypeCheckResult;
import com.lk.jetl.sql.types.AbstractDataType;

public abstract class BinaryOperator extends BinaryExpression{
    public BinaryOperator(Expression left, Expression right) {
        super(left, right);
    }

    public abstract AbstractDataType getInputType();

    public abstract String symbol();

    public String sqlOperator(){
        return symbol();
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)", left, sqlOperator(), right);
    }

    @Override
    public TypeCheckResult checkInputDataTypes() {
        // First check whether left and right have the same type, then check if the type is acceptable.
        if(!left.getDataType().sameType(right.getDataType())){
            return TypeCheckResult.typeCheckFailure("differing types");
        } else if (!getInputType().acceptsType(left.getDataType())) {
            return TypeCheckResult.typeCheckFailure("requires " + getInputType().simpleString() + " type");
        } else{
            return TypeCheckResult.typeCheckSuccess();
        }
    }
}

