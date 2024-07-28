package com.lk.jetl.sql.types;

public abstract class DataType extends AbstractDataType {

    @Override
    public boolean acceptsType(DataType other) {
        return sameType(other);
    }

    public String typeName(){
        String typeName = this.getClass().getSimpleName();
        if(typeName.endsWith("Type")){
            typeName = typeName.substring(0, typeName.length() - 4);
        }
        return typeName.toLowerCase();
    }

    @Override
    public String simpleString() {
        return typeName();
    }

    public boolean sameType(DataType other){
        return this.equals(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return simpleString();
    }
}
