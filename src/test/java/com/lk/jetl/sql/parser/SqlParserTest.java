package com.lk.jetl.sql.parser;

import com.lk.jetl.sql.expressions.*;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.arithmetic.*;
import com.lk.jetl.sql.expressions.predicate.GreaterThan;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.Types;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SqlParserTest {

    @Test
    public void testSelectItem() throws Exception {
        String sql = "select s.a.z z1, s.z z, `s.z` z2, s['z'] z3,t.*,*,t.name, age, substr(name, 1, 4) name2, age + 1 age2, age * 2 age3, age - 2 age4, cast(a as int2) a, int(a) b, 1 c, -2 d from table t";
        Statement statement = CCJSqlParserUtil.parse(sql);
        System.out.println(statement);
        PlainSelect selectBody = ((PlainSelect) statement);
        List<SelectItem<?>> selectItems = selectBody.getSelectItems();
        for (SelectItem<?> selectItem : selectItems) {
            System.out.println(selectItem + ":" + selectItem.getExpression().getClass().getSimpleName());
        }
    }

    @Test
    public void testSelectItemToExpression() throws Exception {
        String sql = "select name, age, substr(name, 1, 4) name2, if(age > 10, age, age + 10), age + 1 age2, age * 2 age3, age - 2 age4, cast(a as int) a, 1 b from table";
        Statement statement = CCJSqlParserUtil.parse(sql);
        System.out.println(statement);
        PlainSelect selectBody = ((PlainSelect) statement);
        List<SelectItem<?>> selectItems = selectBody.getSelectItems();
        Expression[] expressions = new Expression[selectItems.size()];
        for (int i = 0; i < selectItems.size(); i++) {
            expressions[i] = jsqlExprToExpression(selectItems.get(i).getExpression());
            System.out.println(selectItems.get(i) + ":" + selectItems.get(i).getExpression().getClass().getSimpleName());
            System.out.println(expressions[i]);
        }

    }

    public Expression jsqlExprToExpression(net.sf.jsqlparser.expression.Expression expr) {
        if (expr instanceof Column) {
            String columnName = ((Column) expr).getColumnName();
            return new UnresolvedAttribute(List.of(columnName));
        } else if (expr instanceof Function) {
            Function function = (Function) expr;
            String name = function.getName();
            ExpressionList<?> parameters = function.getParameters();
            List<Expression> arguments = new ArrayList<>(parameters.size());
            for (int i = 0; i < parameters.size(); i++) {
                arguments.add(jsqlExprToExpression(parameters.get(i)));
            }
            return new UnresolvedFunction(name, arguments);
        } else if (expr instanceof CastExpression) {
            CastExpression cast = (CastExpression) expr;
            Expression expression = jsqlExprToExpression(cast.getLeftExpression());
            DataType dataType = Types.parseDataType(cast.getColDataType().getDataType());
            return new Cast(expression, dataType);
        } else if (expr instanceof BinaryExpression) {
            BinaryExpression binary = (BinaryExpression) expr;
            Expression left = jsqlExprToExpression(binary.getLeftExpression());
            Expression right = jsqlExprToExpression(binary.getRightExpression());
            if (binary instanceof Addition) {
                return new Add(left, right);
            } else if (binary instanceof Subtraction) {
                return new Subtract(left, right);
            } else if (binary instanceof Multiplication) {
                return new Multiply(left, right);
            } else if (binary instanceof Division) {
                return new Divide(left, right);
            } else if (binary instanceof Modulo) {
                return new Remainder(left, right);
            }  else if (binary instanceof net.sf.jsqlparser.expression.operators.relational.GreaterThan) {
                return new GreaterThan(left, right);
            } else {
                throw new UnsupportedOperationException(binary.getClass().getSimpleName());
            }
        }else if (expr instanceof LongValue) {
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
        }

        throw new UnsupportedOperationException(expr.getClass().getSimpleName());
    }
}