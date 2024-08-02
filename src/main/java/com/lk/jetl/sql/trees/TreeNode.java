package com.lk.jetl.sql.trees;

import com.lk.jetl.sql.types.DataType;
import org.apache.commons.lang3.ClassUtils;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class TreeNode<T> implements Serializable {
    protected Object[] args;
    private transient Set<T> _containsChild;

    public abstract List<T> getChildren();

    public Set<T> getContainsChild() {
        if (_containsChild == null) {
            List<T> children = getChildren();
            _containsChild = new HashSet<>();
            for (T child : children) {
                _containsChild.add(child);
            }
        }
        return _containsChild;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(args);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || obj.getClass() != this.getClass()){
            return false;
        }

        if(this == obj){
            return true;
        }

        Object[] otherArgs = ((TreeNode) obj).args;
        if(args.length != otherArgs.length){
            return false;
        }

        for (int i = 0; i < args.length; i++) {
            if(!args[i].equals(otherArgs[i])){
                return false;
            }
        }

        return true;
    }

    // 先应用于 child 再应用与 parent
    public void forEachUp(Consumer<T> f) {
        getChildren().forEach(f);
        f.accept((T)this);
    }

    public T withNewChildren(List<T> newChildren) {
        assert newChildren.size() == getChildren().size();
        // boolean changed = false;
        LinkedList<T> remainingNewChildren = new LinkedList<>(newChildren);
        Set<T> containsChild = getContainsChild();
        Object[] newArgs = new Object[args.length];
        Object arg;
        for (int i = 0; i < args.length; i++) {
            arg = args[i];
            if (arg instanceof List) {
                newArgs[i] = ((List) arg).stream().map(x -> mapChild(x, containsChild, remainingNewChildren));
            } else if (arg instanceof TreeNode && containsChild.contains(arg)) {
                newArgs[i] = remainingNewChildren.removeFirst();
            } else {
                newArgs[i] = arg;
            }
        }

        return makeCopy(newArgs, false);
    }

    private Object mapChild(Object child, Set<T> containsChild, LinkedList<T> remainingNewChildren) {
        if (child instanceof TreeNode && containsChild.contains(child)) {
            return remainingNewChildren.removeFirst();
        }
        return child;
    }

    public T transformUp(Function<T, T> rule) {
        T afterRuleOnChildren = mapChildren(x -> ((TreeNode<T>)x).transformUp(rule));
        if (this.equals(afterRuleOnChildren)) {
            return rule.apply((T)this);
        } else {
            return rule.apply(afterRuleOnChildren);
        }
    }

    public T mapChildren(Function<T, T> f) {
        if (!getContainsChild().isEmpty()) {
            return mapChildren(f, false);
        } else {
            return (T)this;
        }
    }

    private T mapChildren(Function<T, T> f, boolean forceCopy) {
        final boolean[] changed = {false};
        Set<T> containsChild = getContainsChild();

        Object[] newArgs = new Object[args.length];
        Object arg;
        for (int i = 0; i < args.length; i++) {
            arg = args[i];
            newArgs[i] = mapChild(arg, f, forceCopy, containsChild, changed);
        }

        if (forceCopy || changed[0]) {
            return makeCopy(newArgs, forceCopy);
        }

        return (T)this;
    }

    private Object mapChild(Object arg, Function<T, T> f, boolean forceCopy, Set<T> containsChild, boolean[] changed){
        T newChild;
        if (arg instanceof TreeNode && containsChild.contains(arg)) {
            newChild = f.apply((T) arg);
            if (forceCopy || !(newChild.equals(arg))) {
                changed[0] = true;
                return newChild;
            } else {
                return  arg;
            }
        }else if (arg instanceof List) {
            return ((List) arg).stream().map(x -> mapChild(x, f, forceCopy, containsChild, changed)).collect(Collectors.toList());
        } else if (arg instanceof DataType) {
            return  arg;
        } else {
            return  arg;
        }
    }

    protected T makeCopy(Object[] newArgs, boolean allowEmptyArgs) {
        Constructor<?>[] allCtors = getClass().getConstructors();
        if (newArgs.length == 0 && allCtors.length == 0) {
            // This is a singleton object which doesn't have any constructor. Just return `this` as we
            // can't copy it.
            return (T)this;
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
            return (T) defaultCtor.newInstance(newArgs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
