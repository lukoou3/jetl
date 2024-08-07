package com.lk.jetl.sql;

public class JoinedRow extends Row {
    private Row row1;
    private Row row2;

    public JoinedRow() {
    }

    public JoinedRow(Row row1, Row row2) {
        this.row1 = row1;
        this.row2 = row2;
    }

    public JoinedRow with(Row r1, Row r2){
        row1 = r1;
        row2 = r2;
        return this;
    }

    public JoinedRow withLeft(Row newLeft){
        row1 = newLeft;
        return this;
    }

    public JoinedRow withRight(Row newRight){
        row2 = newRight;
        return this;
    }

    @Override
    public int size() {
        return row1.size() + row2.size();
    }

    @Override
    public Object get(int i) {
        if (i < row1.size()) {
            return row1.get(i);
        } else {
            return row2.get(i - row1.size());
        }
    }

    @Override
    public void update(int i, Object value) {
        if (i < row1.size()) {
            row1.update(i, value);
        } else {
            row2.update(i - row1.size(), value);
        }
    }
}
