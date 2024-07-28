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
        return Types.DOUBLE;
    }

    @Override
    public AbstractDataType getInputType() {
        return Types.AnyDataType;
    }

    @Override
    public TypeCheckResult checkInputDataTypes() {
        TypeCheckResult checkResult = super.checkInputDataTypes();
        if(checkResult.isSuccess()){
            // checkForOrderingExpr
        }
        return checkResult;
    }

    protected Comparator<Object> getComparator(){
        if(comparator == null){
            DataType dataType = left.getDataType();
            if(dataType instanceof IntegerType){
                comparator = (x, y) -> Integer.compare((Integer) x, (Integer)y);
            } else if (dataType instanceof LongType) {
                comparator = (x, y) -> Long.compare((Long) x, (Long)y);
            } else if (dataType instanceof DoubleType) {
                comparator = (x, y) -> Double.compare((Double) x, (Double)y);
            }else{
                throw new UnsupportedOperationException();
            }
        }

        return comparator;
    }
}
