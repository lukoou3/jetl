package com.lk.jetl.sql.parser;

import com.alibaba.fastjson2.JSON;
import com.lk.jetl.sql.GenericRow;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.analysis.Analyzer;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.optimizer.Optimizer;
import com.lk.jetl.sql.types.DataType;
import com.lk.jetl.sql.types.StructType;
import com.lk.jetl.sql.types.Types;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlParserTest {

    @Test
    public void testSqlParser() throws Exception {
        StructType schema = Types.parseStructType("struct<id:bigint, name:string, age:int, age1:int, age2:bigint, counts:array<int>>");
        String sql = "select id, name, substr(name, 1, 1) name1, split(name, ',')[2] name2, age1, age2, age1 + age2 ag3, counts, counts[1] count1, counts[2] count2 from table where age between 20 and 30";
        PlainSelect select = (PlainSelect)CCJSqlParserUtil.parse(sql);
        List<SelectItem<?>> selectItems = select.getSelectItems();
        Expression[] expressions = new Expression[selectItems.size()];
        for (int i = 0; i < selectItems.size(); i++) {
            expressions[i] = SqlParser.jsqlExpressionConvert(selectItems.get(i).getExpression());
            System.out.println(expressions[i]);
        }
        Expression where = SqlParser.jsqlExpressionConvert(select.getWhere());
        System.out.println(where);
        System.out.println(StringUtils.repeat('#', 60));
        for (int i = 0; i < expressions.length; i++) {
            expressions[i] = Analyzer.analyse(expressions[i], schema);
            assert expressions[i].isResolved() : expressions[i];
            expressions[i] = Optimizer.optimize(expressions[i]);
            System.out.println(expressions[i]);
        }
        where = Analyzer.analyse(where, schema);
        where = Optimizer.optimize(where);
        System.out.println(where);
        System.out.println(StringUtils.repeat('#', 60));
        for (Expression expression : expressions) {
            expression.open();
        }
        Row[] datas = new Row[]{
             new GenericRow(new Object[]{1L, "ab,cd,ef", 18, 20, 21L, new Object[]{1, 2, 3, 4}}),
             new GenericRow(new Object[]{2L, "ab,cd,ef", 20, 120, 21L, new Object[]{1, 2, 3, 4}}),
             new GenericRow(new Object[]{3L, "ab,c", 25, 220, 21L, new Object[]{10, 20, 30, 40}}),
             new GenericRow(new Object[]{4L, "abc,12,3", 30, 320, 21L, new Object[]{100, 200}}),
             new GenericRow(new Object[]{5L, "ab,c4,4", 31, 420, 21L, new Object[]{1, 2, 3, 4}}),
        };
        Object w;
        Object[] rsts = new Object[expressions.length];
        for (Row row : datas) {
            w = where.eval(row);
            if(Boolean.TRUE.equals(w)){
                for (int i = 0; i < expressions.length; i++) {
                    rsts[i] = expressions[i].eval(row);
                }
                System.out.println(JSON.toJSONString(rsts));
            }else{
                System.out.println("filter");
            }
        }
    }

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
        String sql = "select a.c[0] a1, a.c.b[0] a2, a[0] aa, a['a'] ab, a[0][1] ac, a['0']['1'] ad, s.a.z z1, s.z z, `s.z` z2, s['z'] z3,t.*,*,t.name, age, substr(name, 1, 4) name2, age + 1 age2, age * 2 age3, age - 2 age4, cast(a as int2) a, int(a) b, 1 c, -2 d from table t";
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
            expressions[i] = SqlParser.jsqlExpressionConvert(selectItems.get(i).getExpression());
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
            expressions[i] = SqlParser.jsqlExpressionConvert(exps[i]);
            System.out.println(exps[i] + ":" + exps[i].getClass().getSimpleName());
            System.out.println(expressions[i]);
        }
    }


}