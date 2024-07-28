package com.lk.jetl.sql.plans;

public abstract class LogicalPlan extends QueryPlan<LogicalPlan>{
    protected Boolean resolved;

    public boolean isResolved() {
        if (resolved == null) {
            resolved = getExpressions().stream().allMatch(x -> x.isResolved()) & isChildrenResolved();
        }
        return resolved;
    }

    public boolean isChildrenResolved() {
        return getChildren().stream().allMatch(LogicalPlan::isResolved);
    }
}
