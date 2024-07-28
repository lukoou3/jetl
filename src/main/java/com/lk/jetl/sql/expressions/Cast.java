package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.types.*;

import java.util.function.Function;

import static com.lk.jetl.sql.types.Types.NULL;

public class Cast extends UnaryExpression{
    public final DataType dataType;
    private Function<Object, Object> cast;

    public Cast(Expression child, DataType dataType) {
        super(child);
        this.dataType = dataType;
        this.args = new Object[]{child, dataType};
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public String toString() {
        return String.format("cast(%s as %s)", child, dataType.simpleString());
    }

    @Override
    public void open() {
        super.open();
        DataType from = child.getDataType();
        DataType to = dataType;
        if(from.equals(to)){
            cast = x -> x;
        } else if (from.equals(NULL)) {
            // According to `canCast`, NullType can be casted to any type.
            // For primitive types, we don't reach here because the guard of `nullSafeEval`.
            // But for nested types like struct, we might reach here for nested null type field.
            // We won't call the returned function actually, but returns a placeholder.
            cast = x -> {throw new IllegalArgumentException("should not directly cast from NullType to" + to);};
        } else {
            if(to instanceof LongType){
                cast = castToLong(from);
            }else if(to instanceof DoubleType){
                cast = castToDouble(from);
            }else{
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    protected Object nullSafeEval(Object input) {
        return cast.apply(input);
    }

    private Function<Object, Object> castToLong(DataType from){
        if(from instanceof StringType){
            return x -> {
                try {
                    return Long.parseLong((String)x);
                } catch (NumberFormatException e) {
                    return null;
                }
            };
        }
        else if(from instanceof NumericType){
            return x -> ((Number)x).longValue();
        }

        throw new IllegalArgumentException();
    }

    private Function<Object, Object> castToDouble(DataType from){
        if(from instanceof StringType){
            return x -> {
                try {
                    return Double.parseDouble((String)x);
                } catch (NumberFormatException e) {
                    return null;
                }
            };
        }
        else if(from instanceof NumericType){
            return x -> ((Number)x).doubleValue();
        }

        throw new IllegalArgumentException();
    }
}
