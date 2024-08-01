package com.lk.jetl.rds;

import java.io.Serializable;

public abstract class Partition implements Serializable {
    public abstract int index();

    @Override
    public int hashCode() {
        return index();
    }

}
