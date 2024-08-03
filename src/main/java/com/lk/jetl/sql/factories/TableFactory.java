package com.lk.jetl.sql.factories;

import com.lk.jetl.sql.types.StructType;

import java.util.Map;

public interface TableFactory extends Factory {

    public static class Context {
        private final StructType schema;
        private final StructType physicalDataType;
        private final Map<String, Object> options;

        public Context(StructType schema, StructType physicalDataType, Map<String, Object> options) {
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

        public Map<String, Object> getOptions() {
            return options;
        }

    }
}