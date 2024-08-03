package com.lk.jetl.sql.factories;

import com.lk.jetl.sql.connector.SourceProvider;

public interface SourceTableFactory extends TableFactory {
    SourceProvider getSourceProvider(Context context);
}
