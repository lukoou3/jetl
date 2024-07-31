package com.lk.jetl.sql.analysis;

import com.lk.jetl.sql.expressions.BoundReference;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.UnresolvedAttribute;
import com.lk.jetl.sql.expressions.UnresolvedFunction;
import com.lk.jetl.sql.rule.Rule;
import com.lk.jetl.sql.types.StructType;

import java.util.*;

public class Analyzer {
    private static final Rule[] rules;

    static {
        rules = new Rule[]{
                new ResolveFunctions(),
                new TypeCoercion.ImplicitTypeCasts(),
                new TypeCoercion.IfCoercion(),
        };
    }

    public static Expression analyse(Expression e, StructType schema){
        Expression curExpr = e;
        Expression lastExpr = curExpr;
        ResolveReferences resolveRule = new ResolveReferences(schema);
        curExpr = resolveRule.apply(curExpr);
        for (Rule rule : rules) {
            curExpr = rule.apply(curExpr);
        }
        System.out.println("curExpr eq lastExpr:" + curExpr.equals(lastExpr));
        lastExpr = curExpr;

        CheckAnalysis.checkAnalysis(lastExpr);

        return lastExpr;
    }

    public static class ResolveReferences extends Rule {
        final StructType schema;
        final Map<String, Integer> nameIdxes;

        public ResolveReferences(StructType schema) {
            this.schema = schema;
            nameIdxes = new HashMap<>();
            for (int i = 0; i < schema.fields.length; i++) {
                nameIdxes.put(schema.fields[i].name, i);
            }
        }

        @Override
        public Expression applyChild(Expression e) {
            if (e instanceof UnresolvedAttribute) {
                UnresolvedAttribute u = (UnresolvedAttribute) e;
                List<String> nameParts = u.nameParts;
                String name = nameParts.get(0);
                Integer idx = nameIdxes.get(name);
                if (idx == null) {
                    throw new IllegalArgumentException("no col:" + name);
                }
                return new BoundReference(idx, schema.fields[idx].dataType, name);
            }

            return e;
        }
    }

    public static class ResolveFunctions extends Rule {

        @Override
        protected Expression applyChild(Expression e) {
            if (e instanceof UnresolvedFunction) {
                UnresolvedFunction u = (UnresolvedFunction) e;
                Expression func = FunctionRegistryUtils.lookupFunction(u.name, u.arguments);
                return func;
            }
            return e;
        }
    }
}
