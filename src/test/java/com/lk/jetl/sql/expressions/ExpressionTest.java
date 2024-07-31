package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.GenericRow;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.analysis.CheckAnalysis;
import com.lk.jetl.sql.analysis.FunctionRegistryUtils;
import com.lk.jetl.sql.analysis.TypeCoercion;
import com.lk.jetl.sql.expressions.arithmetic.Add;
import com.lk.jetl.sql.expressions.conditional.If;
import com.lk.jetl.sql.expressions.string.Substring;
import com.lk.jetl.sql.optimizer.Optimizer;
import com.lk.jetl.sql.types.Types;
import org.junit.Test;

import java.util.List;

public class ExpressionTest {

    @Test
    public void testTrueEq() {
        System.out.println(Boolean.TRUE.equals(null)); // null 不instanceof 任何类
        System.out.println(null instanceof Boolean);
        System.out.println(null instanceof ExpressionTest);
        System.out.println(Boolean.TRUE.equals(true));
        System.out.println(Boolean.TRUE.equals(false));
    }

    @Test
    public void testAddNoCheck() {
        Expression expression = new Add(new BoundReference(0, Types.BIGINT), new Literal(100, Types.BIGINT));
        Row row = new GenericRow(1);
        System.out.println(expression.eval(row));
        row.update(0, 1);
        System.out.println(expression.eval(row) + ", " + expression.eval(row).getClass());
        row.update(0, 11);
        System.out.println(expression.eval(row) + ", " + expression.eval(row).getClass());
    }

    @Test
    public void testAddIfNoCheck() {
        Expression expression = new Add(
                new If(new BoundReference(0, Types.BOOLEAN),
                        new Add(new BoundReference(1, Types.BIGINT), new Literal(100L, Types.BIGINT)),
                        new Add(new BoundReference(2, Types.BIGINT), new Literal(200L, Types.BIGINT))
                ),
                new Literal(10000L, Types.BIGINT));
        Row row = new GenericRow(3);
        expression.open();
        System.out.println(expression);
        System.out.println(expression.eval(row));
        row.update(0, true);
        row.update(1, 5L);
        row.update(2, 5L);
        System.out.println(expression.eval(row) + ", " + expression.eval(row).getClass());
        row.update(0, false);
        System.out.println(expression.eval(row) + ", " + expression.eval(row).getClass());
    }

    @Test
    public void testIfCoercion() {
        Expression e = new If(new BoundReference(0, Types.BOOLEAN, "flag"),
                //new Add(new BoundReference(1, Types.INT), new Literal(100, Types.INT)),
                new Add(new BoundReference(1, Types.INT, "cnt1"), new Literal(100L, Types.BIGINT)),
                //new Add(new BoundReference(2, Types.INT), new Literal(200, Types.INT))
                new Add(new BoundReference(2, Types.BIGINT, "cnt2"), new Literal(200, Types.INT))
        );
        System.out.println(e);
        Expression expression = TypeCoercion.applyTypeCoercionRules(e);
        System.out.println(expression);
        expression = Optimizer.optimize(expression);
        CheckAnalysis.checkAnalysis(expression);
        System.out.println(expression);
        expression.open();
        Row row = new GenericRow(3);
        System.out.println(expression.eval(row));
        row.update(0, true);
        row.update(1, 5);
        row.update(2, 5L);
        System.out.println(expression.eval(row) + ", " + expression.eval(row).getClass());
        row.update(0, false);
        System.out.println(expression.eval(row) + ", " + expression.eval(row).getClass());
    }

    @Test
    public void testAddIfCheck() {
        Expression e = new Add(
                new If(new BoundReference(0, Types.BOOLEAN),
                        new Add(new BoundReference(1, Types.INT), new Literal(100, Types.INT)),
                        new Add(new BoundReference(2, Types.INT), new Literal(200L, Types.BIGINT))
                ),
                new Literal(10000L, Types.BIGINT));
        System.out.println(e);
        Expression expression = TypeCoercion.applyTypeCoercionRules(e);
        System.out.println(expression);
        expression = Optimizer.optimize(expression);
        CheckAnalysis.checkAnalysis(expression);
        System.out.println(expression);
        expression.open();
        Row row = new GenericRow(3);
        System.out.println(expression.eval(row));
        row.update(0, true);
        row.update(1, 5);
        row.update(2, 5);
        System.out.println(expression.eval(row) + ", " + expression.eval(row).getClass());
        row.update(0, false);
        System.out.println(expression.eval(row) + ", " + expression.eval(row).getClass());
    }

    @Test
    public void testSubstring() {
        Expression e = new Substring(
                new BoundReference(0, Types.STRING, "dt"),
                new Literal(1, Types.INT),
                new Literal(10, Types.INT));
        System.out.println(e);
        Expression expression = TypeCoercion.applyTypeCoercionRules(e);
        System.out.println(expression);
        expression = Optimizer.optimize(expression);
        CheckAnalysis.checkAnalysis(expression);
        System.out.println(expression);
        expression.open();
        Row row = new GenericRow(1);
        row.update(0, "2024-07-27 18:08");
        System.out.println(expression.eval(row) + ", " + expression.eval(row).getClass());
    }

    @Test
    public void testLookupFunction() {
        Expression substr = FunctionRegistryUtils.lookupFunction("substr",
                List.of(new BoundReference(0, Types.STRING, "dt"),
                        new Literal(1, Types.INT),
                        new Literal(10, Types.INT)
                ));
        System.out.println(substr);
        Expression e = FunctionRegistryUtils.lookupFunction("if",
                List.of(new BoundReference(0, Types.BOOLEAN),
                        new Add(new BoundReference(1, Types.INT), new Literal(100, Types.INT)),
                        new Add(new BoundReference(2, Types.INT), new Literal(200L, Types.BIGINT))
                ));
        System.out.println(e);
    }
}