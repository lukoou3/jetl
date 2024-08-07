package com.lk.jetl.sql.expressions.aggregate;

import com.lk.jetl.sql.GenericRow;
import com.lk.jetl.sql.JoinedRow;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.expressions.AttributeReference;
import com.lk.jetl.sql.expressions.BoundReference;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.NamedExpression;
import com.lk.jetl.sql.types.StructType;
import com.lk.jetl.sql.types.Types;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AggregateExpressionTest{
    @Test
    public void test() {
        // child
        DeclarativeAggregate aggregate = new Average(new BoundReference(0, Types.BIGINT));
        DeclarativeAggregate[] aggregateExpressions = new DeclarativeAggregate[]{aggregate};
        Expression[] resultAttribute = new Expression[]{
                new AttributeReference(aggregate.toString(),aggregate.getDataType())
        };

        //initExpressions
        List<Expression> initExpressions = new ArrayList<>();
        for (DeclarativeAggregate agg : aggregateExpressions) {
            for (Expression expression : agg.initialValues()) {
                initExpressions.add(expression);
            }
        }
        // 创建初始化buffer
        Row aggBuffer = new GenericRow(initExpressions.size());
        for (int i = 0; i < initExpressions.size(); i++) {
            aggBuffer.update(i, initExpressions.get(i).eval(null));
        }
        System.out.println(aggBuffer);

        // processRow, generateProcessRow
        // map阶段, mergeExpressions
        StructType mapSchema = Types.parseStructType("age:bigint, sum:double, count:bigint");
        Map<String, Integer>  mapNameIdxes = BoundReference.nameIdxes(mapSchema);
        List<Expression> updateExpressions = new ArrayList<>();
        for (DeclarativeAggregate agg : aggregateExpressions) {
            for (Expression expression : agg.updateExpressions()) {
                Expression bindReference = BoundReference.bindReference(expression, mapSchema, mapNameIdxes);
                updateExpressions.add(bindReference);
            }
        }
        System.out.println(updateExpressions);

        System.out.println(StringUtils.repeat('*', 50));

        Row[] datas = new Row[]{
                new GenericRow(new Object[]{1}),
                new GenericRow(new Object[]{2}),
                new GenericRow(new Object[]{3}),
                new GenericRow(new Object[]{4}),
                new GenericRow(new Object[]{5}),
        };

        // 更新updateProjection
        JoinedRow joinedRow = new JoinedRow();
        joinedRow.withRight(aggBuffer);
        // 这个怕单个aggregate updateExpressions相互依赖吧
        Object[] buffer = new Object[updateExpressions.size()];

        for (int i = 0; i < updateExpressions.size(); i++) {
            updateExpressions.get(i).open();
        }
        for (Row row : datas) {
            for (int i = 0; i < updateExpressions.size(); i++) {
                buffer[i] = updateExpressions.get(i).eval(joinedRow.withLeft(row));
            }
            for (int i = 0; i < aggBuffer.size(); i++) {
                aggBuffer.update(i, buffer[i]);
            }
            System.out.println(aggBuffer);
        }

        System.out.println(StringUtils.repeat('*', 50));

        // 计算最终结果
        // reduce阶段, resultExpressions
        StructType reduceSchema = Types.parseStructType("sum:double, count:bigint");
        Map<String, Integer>  reduceNameIdxes = BoundReference.nameIdxes(reduceSchema);
        List<Expression> resultExpressions = new ArrayList<>();
        for (DeclarativeAggregate agg : aggregateExpressions) {
            Expression bindReference = BoundReference.bindReference(agg.evaluateExpression(), reduceSchema, reduceNameIdxes);
            resultExpressions.add(bindReference);
        }
        for (int i = 0; i < resultExpressions.size(); i++) {
            resultExpressions.get(i).open();
        }

        Row aggResult = new GenericRow(resultAttribute.length);
        for (int i = 0; i < resultExpressions.size(); i++) {
            aggResult.update(i, resultExpressions.get(i).eval(aggBuffer));
        }

        System.out.println(aggResult);

        //最后就是把key和聚合值合在一起输出
        // 从这里可以看到为啥spark sql中AttributeReference有exprId属性了，就是为了处理多个表还有这种聚合函数内部buffer的情况
    }

}