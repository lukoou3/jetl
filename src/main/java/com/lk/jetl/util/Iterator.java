package com.lk.jetl.util;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Iterator<A> implements java.util.Iterator<A>{
    public static final Iterator empty = new Iterator() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException("next on empty iterator");
        }
    };

    public static <T> Iterator<T> empty() {
        return empty;
    }

    public abstract boolean hasNext();

    public abstract A next();

    public boolean isEmpty() {
        return !hasNext();
    }

    public <B> Iterator<B> map(Function<? super A, ? extends B> f) {
        return new Iterator<B>() {
            @Override
            public boolean hasNext() {
                return Iterator.this.hasNext();
            }

            @Override
            public B next() {
                return f.apply(Iterator.this.next());
            }
        };
    }

    public <B> Iterator<B> flatMap(Function<? super A, Iterator<B>> f) {
        return new Iterator<B>() {
            private Iterator<B> cur = empty;

            private void nextCur() {
                cur = null;
                cur = f.apply(Iterator.this.next());
            }

            @Override
            public boolean hasNext() {
                // Equivalent to cur.hasNext || self.hasNext && { nextCur(); hasNext }
                // but slightly shorter bytecode (better JVM inlining!)
                while (!cur.hasNext()) {
                    if (!Iterator.this.hasNext())
                        return false;
                    nextCur();
                }
                return true;
            }

            @Override
            public B next() {
                if (hasNext()) {
                    return cur.next();
                } else {
                    return (B) empty.next();
                }
            }
        };
    }

    public Iterator<A> filter(Predicate<? super A> p) {
        return new Iterator<A>() {
            private A hd = null;
            private boolean hdDefined = false;

            @Override
            public boolean hasNext() {
                if (hdDefined) {
                    return true;
                }
                do {
                    if (!Iterator.this.hasNext())
                        return false;
                    hd = Iterator.this.next();
                } while (!p.test(hd));
                hdDefined = true;
                return true;
            }

            @Override
            public A next() {
                if (hasNext()) {
                    hdDefined = false;
                    return hd;
                } else {
                    return (A) empty.next();
                }
            }
        };
    }

    public void forEach(Consumer<? super A> f) {
        while (hasNext())
            f.accept(next());
    }

    public static <T> Iterator<T> fromJava(java.util.Iterator<T> iter) {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public T next() {
                return iter.next();
            }
        };
    }
}
