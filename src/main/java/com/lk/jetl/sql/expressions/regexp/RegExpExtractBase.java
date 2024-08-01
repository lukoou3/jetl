package com.lk.jetl.sql.expressions.regexp;

import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.TernaryExpression;
import com.lk.jetl.sql.types.AbstractDataType;
import com.lk.jetl.sql.types.Types;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RegExpExtractBase extends TernaryExpression {
    protected final Expression subject;
    protected final Expression regexp;
    protected final Expression idx;

    // last regex in string, we will update the pattern iff regexp value changed.
    private transient String lastRegex;
    // last regex pattern, we cache it for performance concern
    private transient Pattern pattern;

    public RegExpExtractBase(Expression subject, Expression regexp, Expression idx) {
        this.subject = subject;
        this.regexp = regexp;
        this.idx = idx;
        this.args = new Object[]{subject, regexp, idx};
    }

    @Override
    public boolean expectsInputTypes() {
        return true;
    }

    @Override
    public List<AbstractDataType> inputTypes() {
        return List.of(Types.STRING, Types.STRING, Types.INT);
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(subject, regexp, idx);
    }

    protected Matcher getLastMatcher(String s, String p){
        if(!p.equals(lastRegex)){
            // regex value changed
            lastRegex = p;
            pattern = Pattern.compile(lastRegex);
        }

        return pattern.matcher(s);
    }

    public static void checkGroupIndex(int groupCount, int groupIndex){
        if (groupIndex < 0) {
            throw new IllegalArgumentException("The specified group index cannot be less than zero");
        } else if (groupCount < groupIndex) {
            throw new IllegalArgumentException(String.format("Regex group count is %d, but the specified group index is %d", groupCount, groupIndex));
        }
    }
}
