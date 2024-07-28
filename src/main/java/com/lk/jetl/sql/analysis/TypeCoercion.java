package com.lk.jetl.sql.analysis;

import com.lk.jetl.sql.expressions.BinaryOperator;
import com.lk.jetl.sql.expressions.Cast;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.conditional.If;
import com.lk.jetl.sql.rule.Rule;
import com.lk.jetl.sql.types.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.lk.jetl.sql.types.Types.*;

public class TypeCoercion {

    private static List<Rule> typeCoercionRules = Arrays.asList(new ImplicitTypeCasts(), new IfCoercion());
    private static List<DataType> numericPrecedence = Arrays.asList(INT, BIGINT, DOUBLE);

    public static Expression applyTypeCoercionRules(Expression e){
        return e.transformUp(x -> {
            for (Rule rule : typeCoercionRules) {
                x = rule.apply(x, null);
            }
            return x;
        });
    }

    public static boolean haveSameType(List<DataType> types) {
        if (types.size() <= 1) {
            return true;
        }

        DataType head = types.get(0);
        for (int i = 1; i < types.size(); i++) {
            if (!types.get(i).sameType(head)) {
                return false;
            }
        }
        return true;
    }

    static Optional<DataType> findTightestCommonType(DataType t1, DataType t2) {
        if (t1.equals(t2)) {
            return Optional.of(t1);
        }

        if (t1 instanceof NullType) {
            return Optional.of(t2);
        }

        if (t2 instanceof NullType) {
            return Optional.of(t1);
        }

        if (t1 instanceof NumericType && t2 instanceof NumericType) {
            DataType t = null;
            for (DataType d : numericPrecedence) {
                if (d.equals(t1) || d.equals(t2)) {
                    t = d;
                }
            }
            return Optional.of(t);
        }

        return Optional.empty();
    }

    static Optional<DataType> findWiderTypeForTwo(DataType t1, DataType t2) {
        return findTightestCommonType(t1, t2).or(() -> stringPromotion(t1, t2));
    }

    static Optional<DataType> stringPromotion(DataType t1, DataType t2) {
        if (t1 instanceof StringType && t2 instanceof AtomicType) {
            return Optional.of(STRING);
        }

        if (t1 instanceof AtomicType && t2 instanceof StringType) {
            return Optional.of(STRING);
        }

        return Optional.empty();
    }

    static Expression castIfNotSameType(Expression expr, DataType dt) {
        if (!expr.getDataType().sameType(dt)) {
            return new Cast(expr, dt);
        } else {
            return expr;
        }
    }

    public static class IfCoercion extends Rule {
        @Override
        public Expression apply(Expression e, StructType schema) {
            if (!e.isChildrenResolved()) {
                return e;
            }

            if (e instanceof If) {
                If i = (If) e;
                Expression left = i.trueValue;
                Expression right = i.falseValue;
                if (!haveSameType(Arrays.asList(left.getDataType(), right.getDataType()))) {
                    return findWiderTypeForTwo(left.getDataType(), right.getDataType()).map(widestType -> {
                        Expression newLeft = castIfNotSameType(left, widestType);
                        Expression newRight = castIfNotSameType(right, widestType);
                        return new If(i.predicate, newLeft, newRight);
                    }).orElse(i);
                }
            }

            return e;
        }
    }

    public static class ImplicitTypeCasts extends Rule {
        @Override
        public Expression apply(Expression e, StructType schema) {
            if (!e.isChildrenResolved()) {
                return e;
            }

            if (e instanceof BinaryOperator) {
                BinaryOperator b = (BinaryOperator) e;
                Expression left = b.left;
                Expression right = b.right;
                return findTightestCommonType(left.getDataType(), right.getDataType()).map(commonType -> {
                    if(b.getInputType().acceptsType(commonType)){
                        // If the expression accepts the tightest common type, cast to that.
                        Expression newLeft = castIfNotSameType(left, commonType);
                        Expression newRight = castIfNotSameType(right, commonType);
                        return b.withNewChildren(Arrays.asList(newLeft, newRight));
                    }else{
                        return b;
                    }

                }).orElse(b);
            }

            return e;
        }
    }
}
