package com.lk.jetl.sql.util;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Option<A> implements Serializable {

    Option() {
    }

    public abstract boolean isEmpty();

    public boolean isDefined() {
        return !isEmpty();
    }

    public abstract A get();

    public A getOrElse(A defaultValue) {
        return isEmpty() ? defaultValue : get();
    }

    public A getOrElseGet(Supplier<? extends A> supplier) {
        return isEmpty() ? supplier.get() : get();
    }

    public final <B> Option<B> map(Function<? super A, ? extends B> mapper){
        if (isEmpty()) {
            return null;
        } else {
            return null;
        }
    }

    public final void forEach(Consumer<? super A> f){
        if(!isEmpty()){
            f.accept(get());
        }
    }

    public static final class Some<A> extends Option<A>{
        public final A value;

        Some(A value) {
            this.value = value;
        }


        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public A get() {
            return value;
        }
    }

    public static final class None<A> extends Option<A>{

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public A get() {
            throw new NoSuchElementException("None.get");
        }
    }
}
