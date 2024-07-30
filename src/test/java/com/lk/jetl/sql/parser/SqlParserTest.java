package com.lk.jetl.sql.parser;

import com.lk.jetl.sql.expressions.*;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.arithmetic.*;
import com.lk.jetl.sql.expressions.conditional.CaseWhen;
import com.lk.jetl.sql.expressions.nvl.IsNotNull;
import com.lk.jetl.sql.expressions.nvl.IsNull;
import com.lk.jetl.sql.expressions.predicate.*;
import com.lk.jetl.sql.expressions.predicate.GreaterThan;
import com.lk.jetl.sql.expressions.string.Like;
import com.lk.jetl.sql.expressions.string.RLike;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.Types;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
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
    public void testWhere() throws Exception {
        String sql = "select name, age, substr(name, 1, 4) name2 from table t where age > 1 and cate = '' and cate2 > '' and cate3 is not null";
        Statement statement = CCJSqlParserUtil.parse(sql);
        System.out.println(statement);
        PlainSelect selectBody = ((PlainSelect) statement);
        List<SelectItem<?>> selectItems = selectBody.getSelectItems();
        for (SelectItem<?> selectItem : selectItems) {
            System.out.println(selectItem + ":" + selectItem.getExpression().getClass().getSimpleName());
        }
        net.sf.jsqlparser.expression.Expression where = selectBody.getWhere();
        System.out.println(where.getClass());
        System.out.println(where);
        where = CCJSqlParserUtil.parseExpression("name is null and age in (1, 2, 3)");
        System.out.println(where.getClass());
        System.out.println(where);

        where = CCJSqlParserUtil.parseExpression("name like 'aaa%'");
        System.out.println(where.getClass());
        where = CCJSqlParserUtil.parseExpression("name not like 'aaa%'");
        System.out.println(where);

        where = CCJSqlParserUtil.parseExpression("name regexp '[0-9]+'");
        System.out.println(where.getClass());
        where = CCJSqlParserUtil.parseExpression("name not like 'aaa%'");
        System.out.println(where);

        where = CCJSqlParserUtil.parseExpression("case col when 1 then 'a' when 2 then 'b' else 'c' end");
        System.out.println(where.getClass());
        where = CCJSqlParserUtil.parseExpression("case when col = 1 then 'a' when col = 2 then 'b' else 'c' end");
        System.out.println(where);
        where = CCJSqlParserUtil.parseExpression("case when col = 1 then 'a' when col = 2 then 'b' end");
        System.out.println(where);
    }

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

    @Test
    public void testWhereToExpression() throws Exception {
        String[] strs = new String[]{
                "age > 1 and age < 4 and age <= 1 and cate = '' and cate = null",
                "cate2 is null and cate3 is not null and not a",
                "name1 like 'aaa%' or name2 not like 'aaa%'",
                "name1 regexp '[0-9]+' and name1 not regexp '[0-9]a'",
                "age between 1 and 10",
                "case col when 1 then 'a' when 2 then 'b' else 'c' end",
                "case when col = 1 then 'a' when col = 2 then 'b' else 'c' end",
        };
        net.sf.jsqlparser.expression.Expression[] exps = new net.sf.jsqlparser.expression.Expression[strs.length];
        Expression[] expressions = new Expression[strs.length];
        for (int i = 0; i < strs.length; i++) {
            exps[i] = CCJSqlParserUtil.parseExpression(strs[i]);
            expressions[i] = jsqlExprToExpression(exps[i]);
            System.out.println(exps[i] + ":" + exps[i].getClass().getSimpleName());
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
                switch (like.getLikeKeyWord()){
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
                return like.isNot()? new Not(e): e;
            } else {
                throw new UnsupportedOperationException(expr.getClass().getSimpleName() + " for " + expr);
            }
        } else if (expr instanceof IsNullExpression) {
            IsNullExpression isNull = ((IsNullExpression) expr);
            Expression child = jsqlExprToExpression(isNull.getLeftExpression());
            return isNull.isNot()? new IsNotNull(child): new IsNull(child);
        } else if (expr instanceof NotExpression) {
            NotExpression not = ((NotExpression) expr);
            Expression child = jsqlExprToExpression(not.getExpression());
            return new Not(child);
        } else if (expr instanceof Between) {
            Between between = ((Between) expr);
            Expression value = jsqlExprToExpression(between.getLeftExpression());
            Expression start = jsqlExprToExpression(between.getBetweenExpressionStart());
            Expression end = jsqlExprToExpression(between.getBetweenExpressionEnd());
            Expression e = new And(new GreaterThanOrEqual(value, start), new LessThanOrEqual(value, end));
            return between.isNot()? new Not(e): e;
        } else if (expr instanceof CaseExpression) {
            CaseExpression c = ((CaseExpression) expr);
            List<WhenClause> whenClauseList = c.getWhenClauses();
            List<Expression> branches = new ArrayList<>(whenClauseList.size() * 2);
            if(c.getSwitchExpression() == null){
                for (WhenClause whenClause : whenClauseList) {
                    branches.add(jsqlExprToExpression(whenClause.getWhenExpression()));
                    branches.add(jsqlExprToExpression(whenClause.getThenExpression()));
                }
            }else{
                Expression switchExpression = jsqlExprToExpression(c.getSwitchExpression());
                for (WhenClause whenClause : whenClauseList) {
                    branches.add(new EqualTo(switchExpression, jsqlExprToExpression(whenClause.getWhenExpression())));
                    branches.add(jsqlExprToExpression(whenClause.getThenExpression()));
                }
            }
            return c.getElseExpression() == null?new CaseWhen(branches):new CaseWhen(branches, jsqlExprToExpression(c.getElseExpression()));
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