package com.lk.jetl.sql.analysis;

import com.lk.jetl.sql.expressions.BinaryOperator;
import com.lk.jetl.sql.expressions.Cast;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.conditional.If;
import com.lk.jetl.sql.expressions.predicate.In;
import com.lk.jetl.sql.rule.Rule;
import com.lk.jetl.sql.types.*;
import com.lk.jetl.util.Option;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.lk.jetl.sql.types.Types.*;

public class TypeCoercion {

    private static List<Rule> typeCoercionRules = Arrays.asList(new ImplicitTypeCasts(), new InConversion(), new IfCoercion());
    private static List<DataType> numericPrecedence = Arrays.asList(INT, BIGINT, DOUBLE);

    public static Expression applyTypeCoercionRules(Expression e){
        for (Rule rule : typeCoercionRules) {
            e = rule.apply(e);
        }
        return e;
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

    static Option<DataType> findTightestCommonType(DataType t1, DataType t2) {
        if (t1.equals(t2)) {
            return Option.of(t1);
        }

        if (t1 instanceof NullType) {
            return Option.of(t2);
        }

        if (t2 instanceof NullType) {
            return Option.of(t1);
        }

        if (t1 instanceof NumericType && t2 instanceof NumericType) {
            DataType t = null;
            for (DataType d : numericPrecedence) {
                if (d.equals(t1) || d.equals(t2)) {
                    t = d;
                }
            }
            return Option.of(t);
        }

        return Option.empty();
    }

    static Option<DataType> findWiderTypeForTwo(DataType t1, DataType t2) {
        return findTightestCommonType(t1, t2).OrElseGet(() -> stringPromotion(t1, t2));
    }

    static Option<DataType> findWiderCommonType(List<DataType> dataTypes) {
        // findWiderTypeForTwo doesn't satisfy the associative law, i.e. (a op b) op c may not equal
        // to a op (b op c). This is only a problem for StringType or nested StringType in ArrayType.
        // Excluding these types, findWiderTypeForTwo satisfies the associative law. For instance,
        // (TimestampType, IntegerType, StringType) should have StringType as the wider common type.
        Option<DataType> commonType = Option.of(NULL);
        for (DataType dataType : dataTypes) {
            if(commonType.isDefined()){
                commonType = findWiderTypeForTwo(commonType.get(), dataType);
            }else{
                commonType = Option.empty();
            }
        }
        return commonType;
    }

    static Option<DataType> stringPromotion(DataType t1, DataType t2) {
        if (t1 instanceof StringType && t2 instanceof AtomicType) {
            return Option.of(STRING);
        }

        if (t1 instanceof AtomicType && t2 instanceof StringType) {
            return Option.of(STRING);
        }

        return Option.empty();
    }

    static Expression castIfNotSameType(Expression expr, DataType dt) {
        if (!expr.getDataType().sameType(dt)) {
            return new Cast(expr, dt);
        } else {
            return expr;
        }
    }

    public static class IfCoercion extends Rule {
        protected Expression applyChild(Expression e) {
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
                    }).getOrElse(i);
                }
            }

            return e;
        }
    }

    public static class InConversion extends Rule {

        protected Expression applyChild(Expression e) {
            if (!e.isChildrenResolved()) {
                return e;
            }

            if (e instanceof In) {
                In i = (In) e;
                DataType valueDataType = i.value.getDataType();
                boolean different = i.list.stream().map(x -> x.getDataType()).collect(Collectors.toList()).stream()
                        .anyMatch(x -> !valueDataType.equals(x));
                if(different){
                    return findWiderCommonType(i.getChildren().stream().map(x -> x.getDataType()).collect(Collectors.toList()))
                            .map(finalDataType -> {
                                return i.withNewChildren(i.getChildren().stream().map(x -> castIfNotSameType(x, finalDataType)).collect(Collectors.toList()));
                            })
                            .getOrElse(i);

                }
            }

            return e;
        }
    }

    public static class ImplicitTypeCasts extends Rule {
        @Override
        public Expression applyChild(Expression e) {
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

                }).getOrElse(b);
            }

            return e;
        }
    }
}
