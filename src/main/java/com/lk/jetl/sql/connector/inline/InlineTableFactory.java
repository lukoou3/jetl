package com.lk.jetl.sql.connector.inline;

import com.alibaba.fastjson2.JSON;
import com.lk.jetl.serialization.DeserializationSchema;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.connector.SourceProvider;
import com.lk.jetl.sql.factories.SourceTableFactory;
import com.lk.jetl.sql.formats.json.JsonRowDeserializationSchema;
import com.lk.jetl.sql.types.StructType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InlineTableFactory implements SourceTableFactory {
    public static final String IDENTIFIER = "inline";

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public SourceProvider getSourceProvider(Context context) {
        StructType dataType = context.getPhysicalDataType(); // 列类型
        Map<String, Object> options = context.getOptions();
        List<Row> datas = parseData((String) options.get("data"),
                new JsonRowDeserializationSchema(dataType, false));
        return new InlineSourceProvider(dataType, datas, 2, 10, 21, 500);
    }

    private List<Row> parseData(String data, DeserializationSchema<Row> deserialization){
        List<Row> datas;
        try {
            deserialization.open();
            if(JSON.isValidArray(data)){
                List<String> dataArray = JSON.parseArray(data, String.class);
                datas = new ArrayList<>(dataArray.size());
                for (int i = 0; i < dataArray.size(); i++) {
                    datas.add(deserialization.deserialize(dataArray.get(i).getBytes(StandardCharsets.UTF_8)));
                }
            }else{
                datas = new ArrayList<>(1);
                datas.add(deserialization.deserialize(data.getBytes(StandardCharsets.UTF_8)));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            try {
                deserialization.close();
            } catch (Exception e) {
            }
        }
        return datas;
    }
}
