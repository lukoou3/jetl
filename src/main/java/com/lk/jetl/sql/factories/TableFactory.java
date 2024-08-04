package com.lk.jetl.sql.factories;

import com.lk.jetl.configuration.ReadonlyConfig;
import com.lk.jetl.sql.types.StructType;

import java.util.Map;

public interface TableFactory extends Factory {

    public static class Context {
        private final StructType schema;
        private final StructType physicalDataType;
        private final ReadonlyConfig options;

        public Context(StructType schema, StructType physicalDataType, ReadonlyConfig options) {
            this.schema = schema;
            this.physicalDataType = physicalDataType;
            this.options = options;
        }

        public StructType getSchema() {
            return schema;
        }

        public StructType getPhysicalDataType() {
            return physicalDataType;
        }

        public ReadonlyConfig getOptions() {
            return options;
        }

    }
}