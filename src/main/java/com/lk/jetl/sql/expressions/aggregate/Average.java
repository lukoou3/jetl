package com.lk.jetl.sql.expressions.aggregate;

import com.lk.jetl.sql.analysis.TypeCheckResult;
import com.lk.jetl.sql.expressions.AttributeReference;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.Literal;
import com.lk.jetl.sql.expressions.arithmetic.Add;
import com.lk.jetl.sql.expressions.arithmetic.Divide;
import com.lk.jetl.sql.expressions.conditional.If;
import com.lk.jetl.sql.expressions.nvl.Coalesce;
import com.lk.jetl.sql.expressions.nvl.IsNull;
import com.lk.jetl.sql.types.*;

import java.util.List;

import static com.lk.jetl.sql.analysis.TypeCoercion.cast;
import static com.lk.jetl.sql.types.Types.BIGINT;
import static com.lk.jetl.sql.types.Types.DOUBLE;

public class Average extends DeclarativeAggregate {
    public final Expression child;

    public Average(Expression child) {
        this.child = child;
        this.args = new Object[]{child};
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(child);
    }

    @Override
    public DataType getDataType() {
        return DOUBLE;
    }

    @Override
    public TypeCheckResult checkInputDataTypes() {
        DataType dataType = child.getDataType();
        if(dataType instanceof NumericType || dataType instanceof NullType){
            return TypeCheckResult.typeCheckSuccess();
        }else{
            return TypeCheckResult.typeCheckFailure("function average requires numeric types, not " + dataType);
        }
    }

    private transient AttributeReference _sum;
    private transient AttributeReference _count;

    AttributeReference sum(){
        if (_sum == null) {
            _sum = new AttributeReference("sum", DOUBLE);
        }
        return _sum;
    }

    AttributeReference count(){
        if (_count == null) {
            _count = new AttributeReference("count", BIGINT);
        }
        return _count;
    }

    @Override
    public List<Expression> initialValues() {
        return List.of(new Literal(0D, DOUBLE), new Literal(0L, BIGINT));
    }

    @Override
    public List<AttributeReference> aggBufferAttributes() {
        return List.of(sum(), count());
    }

    @Override
    public List<Expression> updateExpressions() {
        return List.of(
                /* sum = */
                new Add(sum(), new Coalesce(List.of(cast(child, DOUBLE), new Literal(0D,DOUBLE)))),
                /* count = */
                new If(new IsNull(child), count(), new Add(count(), new Literal(1L, BIGINT)))
        );
    }

    @Override
    public Expression evaluateExpression() {
        return new Divide(sum(), cast(count(), DOUBLE));
    }

}
