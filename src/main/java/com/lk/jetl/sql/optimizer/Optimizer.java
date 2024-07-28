package com.lk.jetl.sql.optimizer;

import com.lk.jetl.sql.analysis.TypeCoercion;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.Literal;
import com.lk.jetl.sql.rule.Rule;
import com.lk.jetl.sql.types.StructType;

import java.util.Arrays;
import java.util.List;

public class Optimizer {

    private static List<Rule> optimizers = Arrays.asList(new ConstantFolding());

    public static Expression applyOptimizers(Expression e){
        return e.transformUp(x -> {
            for (Rule rule : optimizers) {
                x = rule.apply(x, null);
            }
            return x;
        });
    }

    public static class ConstantFolding extends Rule {

        @Override
        public Expression apply(Expression e, StructType schema) {
            if(e instanceof Literal){
                // Skip redundant folding of literals. This rule is technically not necessary. Placing this
                // here avoids running the next rule for Literal values, which would create a new Literal
                // object and running eval unnecessarily.
                return e;
            }

            if(e.isFoldable()){
                e.open();
                Object v = e.eval(null);
                e.close();
                return new Literal(v, e.getDataType());
            }

            return e;
        }
    }
}
