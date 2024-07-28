package com.lk.jetl.sql.analysis;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.UnresolvedAttribute;
import com.lk.jetl.sql.expressions.conditional.If;
import com.lk.jetl.sql.rule.Rule;
import com.lk.jetl.sql.types.StructType;

import java.util.List;

public class Analyzer {


    public static class ResolveReferences extends Rule{

        @Override
        public Expression apply(Expression e, StructType schema) {
            if (e instanceof UnresolvedAttribute) {
                UnresolvedAttribute u = (UnresolvedAttribute) e;
                List<String> nameParts = u.nameParts;
            }

            return e;
        }
    }

}
