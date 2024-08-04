package com.lk.jetl.sql.connector.inline;

import com.alibaba.fastjson2.JSON;
import com.lk.jetl.configuration.ReadonlyConfig;
import com.lk.jetl.configuration.util.OptionRule;
import com.lk.jetl.format.DecodingFormat;
import com.lk.jetl.serialization.DeserializationSchema;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.connector.SourceProvider;
import com.lk.jetl.sql.factories.DecodingFormatFactory;
import com.lk.jetl.sql.factories.FactoryUtil;
import com.lk.jetl.sql.factories.SourceTableFactory;
import com.lk.jetl.sql.formats.json.JsonRowDeserializationSchema;
import com.lk.jetl.sql.types.StructType;
import com.lk.jetl.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.lk.jetl.sql.connector.inline.InlineConnectorOptions.*;

public class InlineTableFactory implements SourceTableFactory {
    public static final String IDENTIFIER = "inline";

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public SourceProvider getSourceProvider(Context context) {
        StructType dataType = context.getPhysicalDataType(); // 列类型
        ReadonlyConfig options = context.getOptions();
        DecodingFormat<Row> decodingFormat = FactoryUtil.discoverDecodingFormat(DecodingFormatFactory.class, ReadonlyConfig.fromMap(options.get(FactoryUtil.FORMAT)), options.get(FactoryUtil.FORMAT_TYPE));
        DeserializationSchema<Row> deserialization = decodingFormat.createRuntimeDecoder(dataType);
        List<Row> datas = parseData(options.get(DATA), options.get(TYPE), deserialization);
        return new InlineSourceProvider(dataType, datas, options.get(FactoryUtil.PARALLELISM),
                options.get(ROWS_PER_SECOND), options.get(NUMBER_OF_ROWS), options.get(MILLIS_PER_ROW));
    }

    @Override
    public OptionRule optionRule() {
        return OptionRule.builder()
                .required(DATA)
                .optional(
                        TYPE,
                        ROWS_PER_SECOND,
                        NUMBER_OF_ROWS,
                        MILLIS_PER_ROW,
                        FactoryUtil.PARALLELISM,
                        FactoryUtil.FORMAT
                ).build();
    }

    private List<Row> parseData(String data, InlineDataType type, DeserializationSchema<Row> deserialization){
        List<Row> datas;
        try {
            deserialization.open();
            if(JSON.isValidArray(data)){
                List<String> dataArray = JSON.parseArray(data, String.class);
                datas = new ArrayList<>(dataArray.size());
                for (int i = 0; i < dataArray.size(); i++) {
                    datas.add(deserialization.deserialize(getDataBytes(dataArray.get(i), type)));
                }
            }else{
                datas = new ArrayList<>(1);
                datas.add(deserialization.deserialize(getDataBytes(data, type)));
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

    private byte[] getDataBytes(String data, InlineDataType type){
        if(InlineDataType.STRING == type){
            return data.getBytes(StandardCharsets.UTF_8);
        } else if (InlineDataType.HEX == type){
            return StringUtils.hexStringToByte(data);
        } else if (InlineDataType.BASE64 == type){
            return Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8));
        }else{
            throw new IllegalArgumentException("Unsupported type:" + type);
        }
    }
}
