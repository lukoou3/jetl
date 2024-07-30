package com.lk.jetl.sql.expressions.predicate;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.analysis.TypeCheckResult;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.types.AtomicType;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class In extends Expression {
    public final Expression value;
    public final List<Expression> list;

    public In(Expression value, List<Expression> list) {
        this.value = value;
        this.list = list;
        this.args = new Object[]{value, list};
    }

    @Override
    public List<Expression> getChildren() {
        List<Expression> children = new ArrayList<>(list.size() + 1);
        children.add(value);
        children.addAll(list);
        return children;
    }

    @Override
    public DataType getDataType() {
        return Types.BOOLEAN;
    }

    @Override
    public boolean isFoldable() {
        return getChildren().stream().anyMatch(x -> x.isFoldable());
    }

    @Override
    public String toString() {
        return String.format("%s IN (%s)", value,list.stream().map(Expression::toString).collect(Collectors.joining(",")));
    }

    @Override
    public TypeCheckResult checkInputDataTypes() {
        for (Expression l : list) {
            if(!l.getDataType().equals(value.getDataType())){
                return TypeCheckResult.typeCheckFailure(String.format("Arguments must be same type but were: %s != %s", l.getDataType(), value.getDataType()));
            }
        }
        if(!(value.getDataType() instanceof AtomicType)){
            return TypeCheckResult.typeCheckFailure("not support type:" + value.getDataType());
        }
        return TypeCheckResult.typeCheckSuccess();
    }

    @Override
    public Object eval(Row input) {
        Object evaluatedValue = value.eval(input);
        if(evaluatedValue == null){
            return null;
        }

        boolean hasNull = false;

        for (int i = 0; i < list.size(); i++) {
            Object v = list.get(i).eval(input);
            if(v == null){
                hasNull = true;
            }else if (evaluatedValue.equals(v)) {
                return true;
            }
        }

        if (hasNull) {
            return null;
        } else {
            return false;
        }
    }

}
