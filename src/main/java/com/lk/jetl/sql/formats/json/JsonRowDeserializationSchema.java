package com.lk.jetl.sql.formats.json;

import com.lk.jetl.serialization.DeserializationSchema;
import com.lk.jetl.sql.Row;
import com.lk.jetl.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class JsonRowDeserializationSchema implements DeserializationSchema<Row> {
    private static final Logger LOG = LoggerFactory.getLogger(JsonRowDeserializationSchema.class);
    private static final int MAX_CHARS_LENGTH = 1024 * 16;
    private final StructType dataType;
    private final boolean ignoreParseErrors;
    private final JsonToRowConverter converter;
    private final char[] tmpChars = new char[MAX_CHARS_LENGTH];

    public JsonRowDeserializationSchema(StructType dataType, boolean ignoreParseErrors) {
        this.dataType = dataType;
        this.ignoreParseErrors = ignoreParseErrors;
        this.converter = new JsonToRowConverter(dataType, ignoreParseErrors);
    }

    @Override
    public Row deserialize(byte[] bytes) {
        String message = decodeUTF8(bytes, 0, bytes.length);
        // 进行类型校验, 类型不一致时会尝试进行类型转换
        return converter.convert(message);
    }

    private String decodeUTF8(byte[] input, int offset, int byteLen) {
        char[] chars = MAX_CHARS_LENGTH < byteLen? new char[byteLen]: tmpChars;
        int len = decodeUTF8Strict(input, offset, byteLen, chars);
        if (len < 0) {
            return defaultDecodeUTF8(input, offset, byteLen);
        }
        return new String(chars, 0, len);
    }

    private static int decodeUTF8Strict(byte[] sa, int sp, int len, char[] da) {
        final int sl = sp + len;
        int dp = 0;
        int dlASCII = Math.min(len, da.length);

        // ASCII only optimized loop
        while (dp < dlASCII && sa[sp] >= 0) {
            da[dp++] = (char) sa[sp++];
        }

        while (sp < sl) {
            int b1 = sa[sp++];
            if (b1 >= 0) {
                // 1 byte, 7 bits: 0xxxxxxx
                da[dp++] = (char) b1;
            } else if ((b1 >> 5) == -2 && (b1 & 0x1e) != 0) {
                // 2 bytes, 11 bits: 110xxxxx 10xxxxxx
                if (sp < sl) {
                    int b2 = sa[sp++];
                    if ((b2 & 0xc0) != 0x80) { // isNotContinuation(b2)
                        return -1;
                    } else {
                        da[dp++] = (char) (((b1 << 6) ^ b2) ^ (((byte) 0xC0 << 6) ^ ((byte) 0x80)));
                    }
                    continue;
                }
                return -1;
            } else if ((b1 >> 4) == -2) {
                // 3 bytes, 16 bits: 1110xxxx 10xxxxxx 10xxxxxx
                if (sp + 1 < sl) {
                    int b2 = sa[sp++];
                    int b3 = sa[sp++];
                    if ((b1 == (byte) 0xe0 && (b2 & 0xe0) == 0x80)
                            || (b2 & 0xc0) != 0x80
                            || (b3 & 0xc0) != 0x80) { // isMalformed3(b1, b2, b3)
                        return -1;
                    } else {
                        char c =
                                (char)
                                        ((b1 << 12)
                                                ^ (b2 << 6)
                                                ^ (b3
                                                ^ (((byte) 0xE0 << 12)
                                                ^ ((byte) 0x80 << 6)
                                                ^ ((byte) 0x80))));
                        if (Character.isSurrogate(c)) {
                            return -1;
                        } else {
                            da[dp++] = c;
                        }
                    }
                    continue;
                }
                return -1;
            } else if ((b1 >> 3) == -2) {
                // 4 bytes, 21 bits: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
                if (sp + 2 < sl) {
                    int b2 = sa[sp++];
                    int b3 = sa[sp++];
                    int b4 = sa[sp++];
                    int uc =
                            ((b1 << 18)
                                    ^ (b2 << 12)
                                    ^ (b3 << 6)
                                    ^ (b4
                                    ^ (((byte) 0xF0 << 18)
                                    ^ ((byte) 0x80 << 12)
                                    ^ ((byte) 0x80 << 6)
                                    ^ ((byte) 0x80))));
                    // isMalformed4 and shortest form check
                    if (((b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80 || (b4 & 0xc0) != 0x80)
                            || !Character.isSupplementaryCodePoint(uc)) {
                        return -1;
                    } else {
                        da[dp++] = Character.highSurrogate(uc);
                        da[dp++] = Character.lowSurrogate(uc);
                    }
                    continue;
                }
                return -1;
            } else {
                return -1;
            }
        }
        return dp;
    }

    private static String defaultDecodeUTF8(byte[] bytes, int offset, int len) {
        return new String(bytes, offset, len, StandardCharsets.UTF_8);
    }

}
