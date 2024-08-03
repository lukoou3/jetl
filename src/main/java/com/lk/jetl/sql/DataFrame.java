package com.lk.jetl.sql;

import com.lk.jetl.rds.Partition;
import com.lk.jetl.rds.RDS;
import com.lk.jetl.sql.analysis.Analyzer;
import com.lk.jetl.sql.expressions.BoundReference;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.NamedExpression;
import com.lk.jetl.sql.functions.Predicate;
import com.lk.jetl.sql.functions.Projection;
import com.lk.jetl.sql.optimizer.Optimizer;
import com.lk.jetl.sql.parser.SqlParser;
import com.lk.jetl.sql.types.StructType;
import com.lk.jetl.sql.types.StructType.StructField;
import com.lk.jetl.sql.types.Types;
import com.lk.jetl.util.Iterator;

import java.util.Map;

public class DataFrame extends RDS<Row> {
    private final RDS<Row> rds;
    private final StructType schema;
    private final Map<String, Integer> nameIdxes;

    public DataFrame(RDS<Row> rds, StructType schema) {
        this.rds = rds;
        this.schema = schema;
        this.nameIdxes = BoundReference.nameIdxes(schema);
    }

    public StructType getSchema() {
        return schema;
    }

    public DataFrame filter(String conditionExpr){
        return filter(SqlParser.parseExpression(conditionExpr));
    }

    public DataFrame filter(Expression condition){
        Expression analysed = analyse(condition);
        Expression optimized = optimize(analysed);
        if(!optimized.getDataType().equals(Types.BOOLEAN)){
            throw new UnsupportedOperationException("filter expr return type should is boolean, but:" + condition);
        }
        Expression predicate = BoundReference.bindReference(optimized, schema, nameIdxes);
        if(!predicate.isResolved()){
            throw new RuntimeException(String.format("%s is not resolved", predicate));
        }
        return new DataFrame(rds.filter(new Predicate(predicate)), schema);
    }

    public DataFrame select(String expr){
        Expression[] cols = SqlParser.parseSelect(expr);
        return select(cols);
    }

    public DataFrame select(Expression... cols){
        Expression[] projects = new Expression[cols.length];
        StructField[] fields = new StructField[cols.length];
        for (int i = 0; i < cols.length; i++) {
            Expression analysed = analyse(cols[i]);
            fields[i] = new StructField(name(analysed), analysed.getDataType());
            Expression optimized = optimize(analysed);
            Expression project = BoundReference.bindReference(optimized, schema, nameIdxes);
            projects[i] = project;
        }
        return new DataFrame(rds.map(new Projection(projects)), new StructType(fields));
    }

    public DataFrame query(String sql){
        SqlParser.Query query = SqlParser.parseQuery(sql);
        DataFrame df = this;
        if(query.condition != null){
            df = df.filter(query.condition);
        }
        df = df.select(query.projects);
        return df;
    }

    @Override
    public Partition[] getPartitions() {
        return rds.getPartitions();
    }

    @Override
    public Iterator<Row> compute(Partition split) {
        return rds.compute(split);
    }

    private String name(Expression e){
        if(e instanceof NamedExpression){
            return ((NamedExpression) e).name();
        }

        return e.toString();
    }

    private Expression analyse(Expression e){
        Expression expression = Analyzer.analyse(e, schema);
        return expression;
    }

    private Expression optimize(Expression e){
        Expression expression =  Optimizer.optimize(e);
        return expression;
    }

    @Override
    public void open() throws Exception {
        super.open();
        rds.open();
    }

    @Override
    public void close() throws Exception {
        super.close();
        rds.close();
    }
}
