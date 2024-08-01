package com.lk.jetl.sql.parser;

import com.lk.jetl.sql.expressions.*;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.arithmetic.*;
import com.lk.jetl.sql.expressions.conditional.CaseWhen;
import com.lk.jetl.sql.expressions.nvl.IsNotNull;
import com.lk.jetl.sql.expressions.nvl.IsNull;
import com.lk.jetl.sql.expressions.predicate.*;
import com.lk.jetl.sql.expressions.predicate.GreaterThan;
import com.lk.jetl.sql.expressions.regexp.Like;
import com.lk.jetl.sql.expressions.regexp.RLike;
import com.lk.jetl.sql.expressions.string.StringTrim;
import com.lk.jetl.sql.expressions.string.StringTrimLeft;
import com.lk.jetl.sql.expressions.string.StringTrimRight;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.Types;
import com.lk.jetl.sql.util.Option;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.List;

public class SqlParser {

    public static Expression parser(String sql) {


        return null;
    }

    static Expression jsqlExpressionConvert(net.sf.jsqlparser.expression.Expression expr) {
        if (expr instanceof Column) {
            Column col = (Column) expr;
            String columnName = (col).getColumnName();
            if (col.getArrayConstructor() == null) {
                return new UnresolvedAttribute(List.of(columnName));
            } else {
                ArrayConstructor constructor = col.getArrayConstructor();
                if (constructor.getExpressions().size() == 1 && !constructor.isArrayKeyword()) {
                    return new UnresolvedExtractValue(new UnresolvedAttribute(List.of(columnName)), jsqlExpressionConvert(constructor.getExpressions().get(0)));
                }
            }
        } else if (expr instanceof ArrayExpression) {
            ArrayExpression array = (ArrayExpression) expr;
            if (array.getIndexExpression() != null) {
                return new UnresolvedExtractValue(jsqlExpressionConvert(array.getObjExpression()), jsqlExpressionConvert(array.getIndexExpression()));
            }
        } else if (expr instanceof Function) {
            Function function = (Function) expr;
            String name = function.getName();
            ExpressionList<?> parameters = function.getParameters();
            List<Expression> arguments = new ArrayList<>(parameters.size());
            for (int i = 0; i < parameters.size(); i++) {
                arguments.add(jsqlExpressionConvert(parameters.get(i)));
            }
            return new UnresolvedFunction(name, arguments);
        } else if (expr instanceof TrimFunction) {
            TrimFunction trim = (TrimFunction) expr;
            TrimFunction.TrimSpecification trimAtion = trim.getTrimSpecification();
            Expression srcStr = null;
            Expression trimStr = null;
            if (trim.getFromExpression() == null) {
                srcStr = jsqlExpressionConvert(trim.getExpression());
            } else if (trim.getExpression() == null) {
                srcStr = jsqlExpressionConvert(trim.getFromExpression());
            } else {
                srcStr = trim.isUsingFromKeyword() ? jsqlExpressionConvert(trim.getFromExpression()) : jsqlExpressionConvert(trim.getExpression());
                trimStr = trim.isUsingFromKeyword() ? jsqlExpressionConvert(trim.getExpression()) : jsqlExpressionConvert(trim.getFromExpression());
            }
            if(trimAtion == null || trimAtion == TrimFunction.TrimSpecification.BOTH){
                return new StringTrim(srcStr, Option.option(trimStr));
            } else if (trimAtion == TrimFunction.TrimSpecification.LEADING) {
                return new StringTrimLeft(srcStr, Option.option(trimStr));
            } else if (trimAtion == TrimFunction.TrimSpecification.TRAILING) {
                return new StringTrimRight(srcStr, Option.option(trimStr));
            }else{
                throw new UnsupportedOperationException(expr.getClass().getSimpleName() + " for Function trim doesn't support with type" + trimAtion + ". Please use BOTH, LEADING or TRAILING as trim type");
            }
        } else if (expr instanceof CastExpression) {
            CastExpression cast = (CastExpression) expr;
            Expression expression = jsqlExpressionConvert(cast.getLeftExpression());
            DataType dataType = Types.parseDataType(cast.getColDataType().getDataType());
            return new Cast(expression, dataType);
        } else if (expr instanceof BinaryExpression) {
            BinaryExpression binary = (BinaryExpression) expr;
            Expression left = jsqlExpressionConvert(binary.getLeftExpression());
            Expression right = jsqlExpressionConvert(binary.getRightExpression());
            if (binary instanceof AndExpression) {
                return new And(left, right);
            } else if (binary instanceof OrExpression) {
                return new Or(left, right);
            } else if (binary instanceof Addition) {
                return new Add(left, right);
            } else if (binary instanceof Subtraction) {
                return new Subtract(left, right);
            } else if (binary instanceof Multiplication) {
                return new Multiply(left, right);
            } else if (binary instanceof Division) {
                return new Divide(left, right);
            } else if (binary instanceof Modulo) {
                return new Remainder(left, right);
            } else if (binary instanceof net.sf.jsqlparser.expression.operators.relational.GreaterThan) {
                return new GreaterThan(left, right);
            } else if (binary instanceof net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals) {
                return new GreaterThanOrEqual(left, right);
            } else if (binary instanceof EqualsTo) {
                return new EqualTo(left, right);
            } else if (binary instanceof MinorThan) {
                return new LessThan(left, right);
            } else if (binary instanceof MinorThanEquals) {
                return new LessThanOrEqual(left, right);
            } else if (binary instanceof LikeExpression) {
                LikeExpression like = ((LikeExpression) expr);
                Expression e;
                switch (like.getLikeKeyWord()) {
                    case LIKE:
                        e = new Like(left, right);
                        break;
                    case RLIKE:
                    case REGEXP:
                        e = new RLike(left, right);
                        break;
                    default:
                        throw new UnsupportedOperationException(expr.toString());
                }
                return like.isNot() ? new Not(e) : e;
            } else {
                throw new UnsupportedOperationException(expr.getClass().getSimpleName() + " for " + expr);
            }
        } else if (expr instanceof IsNullExpression) {
            IsNullExpression isNull = ((IsNullExpression) expr);
            Expression child = jsqlExpressionConvert(isNull.getLeftExpression());
            return isNull.isNot() ? new IsNotNull(child) : new IsNull(child);
        } else if (expr instanceof NotExpression) {
            NotExpression not = ((NotExpression) expr);
            Expression child = jsqlExpressionConvert(not.getExpression());
            return new Not(child);
        } else if (expr instanceof Between) {
            Between between = ((Between) expr);
            Expression value = jsqlExpressionConvert(between.getLeftExpression());
            Expression start = jsqlExpressionConvert(between.getBetweenExpressionStart());
            Expression end = jsqlExpressionConvert(between.getBetweenExpressionEnd());
            Expression e = new And(new GreaterThanOrEqual(value, start), new LessThanOrEqual(value, end));
            return between.isNot() ? new Not(e) : e;
        } else if (expr instanceof CaseExpression) {
            CaseExpression c = ((CaseExpression) expr);
            List<WhenClause> whenClauseList = c.getWhenClauses();
            List<Expression> branches = new ArrayList<>(whenClauseList.size() * 2);
            if (c.getSwitchExpression() == null) {
                for (WhenClause whenClause : whenClauseList) {
                    branches.add(jsqlExpressionConvert(whenClause.getWhenExpression()));
                    branches.add(jsqlExpressionConvert(whenClause.getThenExpression()));
                }
            } else {
                Expression switchExpression = jsqlExpressionConvert(c.getSwitchExpression());
                for (WhenClause whenClause : whenClauseList) {
                    branches.add(new EqualTo(switchExpression, jsqlExpressionConvert(whenClause.getWhenExpression())));
                    branches.add(jsqlExpressionConvert(whenClause.getThenExpression()));
                }
            }
            return c.getElseExpression() == null ? new CaseWhen(branches) : new CaseWhen(branches, jsqlExpressionConvert(c.getElseExpression()));
        } else if (expr instanceof LongValue) {
            long longVal = ((LongValue) expr).getValue();
            if (longVal <= Integer.MAX_VALUE && longVal >= Integer.MIN_VALUE) {
                return new Literal((int) longVal, Types.INT);
            } else {
                return new Literal(longVal, Types.BIGINT);
            }
        } else if (expr instanceof DoubleValue) {
            double value = ((DoubleValue) expr).getValue();
            return new Literal(value, Types.DOUBLE);
        } else if (expr instanceof StringValue) {
            String value = ((StringValue) expr).getValue();
            return new Literal(value, Types.STRING);
        } else if (expr instanceof NullValue) {
            return new Literal(null, Types.NULL);
        }

        throw new UnsupportedOperationException(expr.getClass().getSimpleName() + " for " + expr);
    }
}
