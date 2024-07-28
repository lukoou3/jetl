package com.lk.jetl.sql.analysis;

import com.lk.jetl.sql.expressions.Expression;

public class CheckAnalysis {

    public static void checkAnalysis(Expression e) {
        e.transformUp(x -> {
            TypeCheckResult checkResult = x.checkInputDataTypes();
            if (checkResult.isFailure()){
                throw new IllegalArgumentException(((TypeCheckResult.TypeCheckFailure)checkResult).message);
            };
            return x;
        });
    }
}
