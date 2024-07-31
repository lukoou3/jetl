package com.lk.jetl.sql.optimizer;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.Literal;
import com.lk.jetl.sql.expressions.predicate.And;
import com.lk.jetl.sql.expressions.predicate.EqualTo;
import com.lk.jetl.sql.expressions.predicate.GreaterThanOrEqual;
import com.lk.jetl.sql.expressions.string.*;
import com.lk.jetl.sql.rule.Rule;
import com.lk.jetl.sql.types.StringType;
import com.lk.jetl.sql.types.Types;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Optimizer {

    private static List<Rule> optimizers = Arrays.asList(new ConstantFolding());

    public static Expression optimize(Expression e){
        return e.transformUp(x -> {
            for (Rule rule : optimizers) {
                x = rule.apply(x);
            }
            return x;
        });
    }

    public static class ConstantFolding extends Rule {

        @Override
        public Expression applyChild(Expression e) {
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

    /**
     * Simplifies LIKE expressions that do not need full regular expressions to evaluate the condition.
     * For example, when the expression is just checking to see if a string starts with a given
     * pattern.
     */
    public static class LikeSimplification extends Rule {
        private static final Pattern startsWith = Pattern.compile("([^_%]+)%");
        private static final Pattern endsWith = Pattern.compile("%([^_%]+)");
        private static final Pattern startsAndEndsWith = Pattern.compile("([^_%]+)%([^_%]+)");
        private static final Pattern contains = Pattern.compile("%([^_%]+)%");
        private static final Pattern equalTo = Pattern.compile("([^_%]*)");

        @Override
        protected Expression applyChild(Expression e) {
            if(e instanceof Like){
                Like l = (Like) e;
                Expression input = l.left;
                Expression regex = l.right;
                if(regex instanceof Literal && regex.getDataType() instanceof StringType){
                    String pattern = (String) ((Literal)regex).value;
                    if (pattern == null) {
                        // If pattern is null, return null value directly, since "col like null" == null.
                        return new Literal(null, Types.BOOLEAN);
                    } else {
                        return simplifyLike(input, pattern, '\\').orElse(l);
                    }
                }
            }

            return e;
        }

        private Optional<Expression> simplifyLike(Expression input, String pattern, char escapeChar){
            if (pattern.indexOf(escapeChar) >= 0) {
                // There are three different situations when pattern containing escapeChar:
                // 1. pattern contains invalid escape sequence, e.g. 'm\aca'
                // 2. pattern contains escaped wildcard character, e.g. 'ma\%ca'
                // 3. pattern contains escaped escape character, e.g. 'ma\\ca'
                // Although there are patterns can be optimized if we handle the escape first, we just
                // skip this rule if pattern contains any escapeChar for simplicity.
                return Optional.empty();
            }
            Matcher matcher = startsWith.matcher(pattern);
            if(matcher.matches()){
                String prefix = matcher.group(1);
                return Optional.of(new StartsWith(input, new Literal(prefix, Types.STRING)));
            }
            matcher = endsWith.matcher(pattern);
            if(matcher.matches()){
                String postfix = matcher.group(1);
                return Optional.of(new EndsWith(input, new Literal(postfix, Types.STRING)));
            }
            matcher = startsAndEndsWith.matcher(pattern);
            if(matcher.matches()){
                // 'a%a' pattern is basically same with 'a%' && '%a'.
                // However, the additional `Length` condition is required to prevent 'a' match 'a%a'.
                String prefix = matcher.group(1);
                String postfix = matcher.group(2);
                return Optional.of(new And(new GreaterThanOrEqual(new Length(input), new Literal(prefix.length() + postfix.length(), Types.INT)),
                        new And(new StartsWith(input, new Literal(prefix, Types.STRING)), new EndsWith(input, new Literal(postfix, Types.STRING)))
                ));
            }
            matcher = contains.matcher(pattern);
            if(matcher.matches()){
                String infix = matcher.group(1);
                return Optional.of(new Contains(input, new Literal(infix, Types.STRING)));
            }
            matcher = equalTo.matcher(pattern);
            if(matcher.matches()){
                String str = matcher.group(1);
                return Optional.of(new EqualTo(input, new Literal(str, Types.STRING)));
            }

            return Optional.empty();
        }

    }
}
