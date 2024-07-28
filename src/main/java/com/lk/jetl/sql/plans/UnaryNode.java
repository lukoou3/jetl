package com.lk.jetl.sql.plans;

import java.util.List;

public abstract class UnaryNode extends LogicalPlan{
    public abstract LogicalPlan getChild();

    @Override
    public final List<LogicalPlan> getChildren() {
        return List.of(getChild());
    }
}
