package com.lk.jetl.sql.plans;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.trees.TreeNode;
import com.lk.jetl.sql.types.DataType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class QueryPlan<T> extends TreeNode<T> {

    public final List<Expression> getExpressions() {
        List<Expression> expressions = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof Expression) {
                expressions.add((Expression) arg);
            } else if (arg instanceof Iterable) {
                for (Object o : ((Iterable) arg)) {
                    if (o instanceof Expression) {
                        expressions.add((Expression) o);
                    }
                }
            }
        }
        return expressions;
    }

    public T transformExpressionsUp(Function<Expression, Expression> f) {
        return mapExpressions(x -> x.transformUp(f));
    }

    public T mapExpressions(Function<Expression, Expression> f) {
        boolean changed = false;

        Object[] newArgs = new Object[args.length];
        Object arg;
        Object newChild;
        for (int i = 0; i < args.length; i++) {
            arg = args[i];
            newChild = recursiveTransformExpression(arg, f);
            if (!newChild.equals(arg)) {
                changed = true;
                newArgs[i] = newChild;
            } else {
                newArgs[i] = arg;
            }
        }

        if (changed) {
            return makeCopy(newArgs, false);
        }

        return (T) this;
    }

    private Object recursiveTransformExpression(Object arg, Function<Expression, Expression> f) {
        Expression newChild;
        if (arg instanceof Expression) {
            newChild = f.apply((Expression) arg);
            return newChild;
        } else if (arg instanceof List) {
            return ((List) arg).stream().map(x -> recursiveTransformExpression(x, f)).collect(Collectors.toList());
        } else if (arg instanceof DataType) {
            return arg;
        } else {
            return arg;
        }
    }

}
