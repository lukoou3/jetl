package com.lk.jetl.sql.expressions.conditional;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.expressions.ComplexTypeMergingExpression;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.Literal;
import com.lk.jetl.sql.types.Types;

import java.util.ArrayList;
import java.util.List;

public class CaseWhen extends ComplexTypeMergingExpression {
    public final List<Expression> branches;
    public final Expression elseValue;

    public CaseWhen(List<Expression> branches, Expression elseValue) {
        this.branches = branches;
        this.elseValue = elseValue;
        this.args = new Object[]{branches, elseValue};
    }

    public CaseWhen(List<Expression> branches) {
        this(branches, new Literal(null, Types.NULL));
    }

    @Override
    public List<Expression> getChildren() {
        List<Expression> children = new ArrayList<>(branches.size() + 1);
        children.addAll(branches);
        children.add(elseValue);
        return children;
    }

    @Override
    public Object eval(Row input) {
        int len = branches.size() / 2;
        for (int i = 0; i < len; i++) {
            if (Boolean.TRUE.equals(branches.get(i).eval(input))) {
                return branches.get(i + 1).eval(input);
            }
        }
        return elseValue.eval(input);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CASE");
        for (int i = 0; i < branches.size() / 2; i++) {
            sb.append(String.format(" WHEN %s THEN %s", branches.get(i), branches.get(i + 1)));
        }
        sb.append(String.format(" ELSE %s END", elseValue));
        return sb.toString();
    }
}
