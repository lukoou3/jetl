package com.lk.jetl.sql.expressions.predicate;

import com.lk.jetl.sql.analysis.TypeCheckResult;
import com.lk.jetl.sql.expressions.BinaryOperator;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.*;

import java.util.Comparator;

public abstract class BinaryComparison extends BinaryOperator {
    Comparator<Object> comparator;

    public BinaryComparison(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public DataType getDataType() {
        return Types.BOOLEAN;
    }

    @Override
    public AbstractDataType getInputType() {
        return Types.AnyDataType;
    }

    @Override
    public TypeCheckResult checkInputDataTypes() {
        TypeCheckResult checkResult = super.checkInputDataTypes();
        if(checkResult.isSuccess()){
            if(!(left.getDataType() instanceof AtomicType)){
                return TypeCheckResult.typeCheckFailure("not support ordering on type " + left.getDataType());
            }
        }
        return checkResult;
    }

    protected Comparator<Object> getComparator(){
        if(comparator == null){
            comparator = Types.getComparator(left.getDataType());
        }

        return comparator;
    }
}
