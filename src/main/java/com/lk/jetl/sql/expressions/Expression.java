package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.analysis.TypeCheckResult;
import com.lk.jetl.sql.trees.TreeNode;
import com.lk.jetl.sql.types.AbstractDataType;
import com.lk.jetl.sql.types.DataType;

import java.io.Serializable;
import java.util.*;

public abstract class Expression extends TreeNode<Expression> implements Serializable {
    protected Boolean resolved;
    public void open() {
        if (args == null) {
            throw new IllegalArgumentException(getClass().getName() + "args not init");
        }
        for (Expression child : getChildren()) {
            child.open();
        }
    }

    public void close() {
        for (Expression child : getChildren()) {
            child.close();
        }
    }

    public boolean isFoldable() {
        return false;
    }

    public abstract Object eval(Row input);

    public abstract DataType getDataType();

    public boolean isResolved() {
        if (resolved == null) {
            resolved = isChildrenResolved() && checkInputDataTypes().isSuccess();
        }
        return resolved;
    }

    public boolean isChildrenResolved() {
        return getChildren().stream().allMatch(Expression::isResolved);
    }

    public boolean expectsInputTypes() {
        return false;
    }

    public List<AbstractDataType> inputTypes() {
        return null;
    }

    public TypeCheckResult checkInputDataTypes() {
        if (!expectsInputTypes()) {
            return TypeCheckResult.typeCheckSuccess();
        }
        List<Expression> inputs = getChildren();
        List<AbstractDataType> inputTypes = inputTypes();
        int len = Math.min(inputs.size(), inputTypes.size());
        for (int i = 0; i < len; i++) {
            Expression input = inputs.get(i);
            AbstractDataType expected = inputTypes.get(i);
            if (!expected.acceptsType(input.getDataType())) {
                String msg = String.format("argument %d requires %s type, however, %s is of %s type.",
                        i + 1, expected.simpleString(), input, input.getDataType());
                return TypeCheckResult.typeCheckFailure(msg);
            }
        }
        return TypeCheckResult.typeCheckSuccess();
    }

    public String prettyName() {
        return getClass().getSimpleName().toLowerCase();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(prettyName());
        sb.append('(');
        boolean first = true;
        if (args == null) {
            System.out.println(getClass());
        }
        for (Object arg : args) {
            if (arg instanceof List) {
                for (Object a : (List) arg) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(a.toString());
                }
            } else {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(arg.toString());
            }
        }
        sb.append(')');
        return sb.toString();
    }


/*    public Expression withNewChildren(List<Expression> newChildren) {
        assert newChildren.size() == getChildren().size();
        // boolean changed = false;
        LinkedList<Expression> remainingNewChildren = new LinkedList<>(newChildren);
        Set<Expression> containsChild = getContainsChild();
        Object[] newArgs = new Object[args.length];
        Object arg;
        for (int i = 0; i < args.length; i++) {
            arg = args[i];
            if (arg instanceof List) {
                newArgs[i] = ((List) arg).stream().map(x -> mapChild(x, containsChild, remainingNewChildren));
            } else if (arg instanceof Expression && containsChild.contains(arg)) {
                newArgs[i] = remainingNewChildren.removeFirst();
            } else {
                newArgs[i] = arg;
            }
        }

        return makeCopy(newArgs, false);
    }

    private Object mapChild(Object child, Set<Expression> containsChild, LinkedList<Expression> remainingNewChildren) {
        if (child instanceof Expression && containsChild.contains(child)) {
            return remainingNewChildren.removeFirst();
        }
        return child;
    }


    public Expression transformUp(Function<Expression, Expression> rule) {
        Expression afterRuleOnChildren = mapChildren(x -> x.transformUp(rule));
        if (this.equals(afterRuleOnChildren)) {
            return rule.apply(this);
        } else {
            return rule.apply(afterRuleOnChildren);
        }
    }


    public Expression mapChildren(Function<Expression, Expression> f) {
        if (!getContainsChild().isEmpty()) {
            return mapChildren(f, false);
        } else {
            return this;
        }
    }

    private Expression mapChildren(Function<Expression, Expression> f, boolean forceCopy) {
        boolean changed = false;
        Set<Expression> containsChild = getContainsChild();
        Object[] newArgs = new Object[args.length];
        Object arg;
        Expression newChild;
        for (int i = 0; i < args.length; i++) {
            arg = args[i];
            if (arg instanceof Expression && containsChild.contains(arg)) {
                newChild = f.apply((Expression) arg);
                if (forceCopy || !(newChild.equals(arg))) {
                    changed = true;
                    newArgs[i] = newChild;
                } else {
                    newArgs[i] = arg;
                }
            } else if (arg instanceof DataType) {
                newArgs[i] = arg;
            } else {
                newArgs[i] = arg;
            }
        }

        if (forceCopy || changed) {
            return makeCopy(newArgs, forceCopy);
        }

        return this;
    }

    private Expression makeCopy(Object[] newArgs, boolean allowEmptyArgs) {
        Constructor<?>[] allCtors = getClass().getConstructors();
        if (newArgs.length == 0 && allCtors.length == 0) {
            // This is a singleton object which doesn't have any constructor. Just return `this` as we
            // can't copy it.
            return this;
        }

        // Skip no-arg constructors that are just there for kryo.
        Constructor<?>[] ctors = Arrays.stream(allCtors).filter(x -> allowEmptyArgs || x.getParameterTypes().length != 0).toArray(Constructor<?>[]::new);
        if (ctors.length == 0) {
            System.err.println("No valid constructor");
        }

        Class[] argsArray = new Class[newArgs.length];
        for (int i = 0; i < newArgs.length; i++) {
            argsArray[i] = newArgs[i] == null ? null : newArgs[i].getClass();
        }

        Constructor<?> defaultCtor = null;
        for (Constructor<?> ctor : ctors) {
            if (ctor.getParameterTypes().length != newArgs.length) {
                continue;
            }
            if (ClassUtils.isAssignable(argsArray, ctor.getParameterTypes(), true)) {
                defaultCtor = ctor;
                break;
            }
        }

        if (defaultCtor == null) {
            throw new IllegalArgumentException(Arrays.toString(args));
        }

        try {
            return (Expression) defaultCtor.newInstance(newArgs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/
}
