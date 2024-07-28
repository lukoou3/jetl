package com.lk.jetl.sql.plans;

import com.lk.jetl.sql.expressions.Alias;

import java.util.List;

public class Project extends UnaryNode{
    public final List<Alias> projectList;
    public final LogicalPlan child;

    public Project(List<Alias> projectList, LogicalPlan child) {
        this.projectList = projectList;
        this.child = child;
        this.args = new Object[]{projectList, child};
    }

    @Override
    public LogicalPlan getChild() {
        return child;
    }
}
