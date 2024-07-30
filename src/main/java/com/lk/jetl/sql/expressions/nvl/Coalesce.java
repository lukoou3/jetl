package com.lk.jetl.sql.expressions.nvl;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.expressions.ComplexTypeMergingExpression;
import com.lk.jetl.sql.expressions.Expression;

import java.util.List;

public class Coalesce extends ComplexTypeMergingExpression {
    public final List<Expression> children;

    public Coalesce(List<Expression> children) {
        this.children = children;
        this.args = new Object[]{children};
    }

    @Override
    public List<Expression> getChildren() {
        return children;
    }

    @Override
    public Object eval(Row input) {
        Object result = null;
        for (int i = 0; i < children.size(); i++) {
            result = children.get(i).eval(input);
            if(result != null){
                return result;
            }
        }
        return result;
    }


}
