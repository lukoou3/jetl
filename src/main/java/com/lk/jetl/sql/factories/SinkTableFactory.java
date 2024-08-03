package com.lk.jetl.sql.factories;

import com.lk.jetl.sql.connector.SinkProvider;

public interface SinkTableFactory extends TableFactory {
    SinkProvider getSinkProvider(Context context);
}
