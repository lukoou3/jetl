package com.lk.jetl.sql.expressions.predicate;

import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.expressions.Expression;

public class EqualNullSafe extends BinaryComparison {

    public EqualNullSafe(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public String symbol() {
        return "<=>";
    }

    @Override
    public Object eval(Row input) {
        Object input1 = left.eval(input);
        Object input2 = right.eval(input);
        if (input1 == null && input2 == null) {
            return true;
        } else if (input1 == null || input2 == null) {
            return false;
        } else {
            return getComparator().compare(input1, input2) == 0;
        }
    }
}
