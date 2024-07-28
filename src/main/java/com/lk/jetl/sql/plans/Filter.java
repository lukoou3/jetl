package com.lk.jetl.sql.plans;

public class Filter extends UnaryNode {
    public final Exception condition;
    public final LogicalPlan child;

    public Filter(Exception condition, LogicalPlan child) {
        this.condition = condition;
        this.child = child;
        this.args = new Object[]{condition, child};
    }

    @Override
    public LogicalPlan getChild() {
        return child;
    }
}
