package com.lk.jetl.sql;

import com.lk.jetl.JEtlContext;
import com.lk.jetl.rds.ParallelCollectionRDS;
import com.lk.jetl.sql.types.StructType;
import com.lk.jetl.sql.types.Types;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DataFrameTest {
    static final Logger LOG = LoggerFactory.getLogger(DataFrameTest.class);
    @Test
    public void test() throws Exception {
        StructType schema = Types.parseStructType("struct<id:bigint, name:string, language:int, math:int>");
        List<Row> datas = List.of(
                new GenericRow(new Object[]{1L, "1_2_3_4", 78, 80}),
                new GenericRow(new Object[]{2L, "1_2_3_4_5", 80, 80}),
                new GenericRow(new Object[]{3L, "1_2", 82, 90}),
                new GenericRow(new Object[]{4L, "1_2_", 90, 90}),
                new GenericRow(new Object[]{5L, "1", 92, 95})
        );
        DataFrame df = new DataFrame(new ParallelCollectionRDS<>(datas, 1), schema);
        DataFrame rstDf = df.filter("language between 80 and 90")
                .select("id, name, split(name, '_')[2] name2, split(name, '_') names, language, math, language + math total, id + language + math total2");
        System.out.println(rstDf.getSchema().treeString());
        JEtlContext.runJob(rstDf.sink(x -> {
            LOG.warn(x.toString());
        }));
    }

}