package github.jiangbyte.io.common.web.jackson;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 数值反序列化器集合：兼容字符串形式的数字输入。
 *
 * Author: Charlie
 */
public final class StringlyNumberDeserializers {

    private StringlyNumberDeserializers() {
    }

    public static final ValueDeserializer<Integer> INTEGER = new ValueDeserializer<>() {
        @Override
        public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String text = readNumberString(p, ctxt, Integer.class);
            if (text == null) {
                return null;
            }
            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException ex) {
                return (Integer) ctxt.handleWeirdStringValue(Integer.class, text, "not an int");
            }
        }
    };

    public static final ValueDeserializer<Long> LONG = new ValueDeserializer<>() {
        @Override
        public Long deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String text = readNumberString(p, ctxt, Long.class);
            if (text == null) {
                return null;
            }
            try {
                return Long.valueOf(text);
            } catch (NumberFormatException ex) {
                return (Long) ctxt.handleWeirdStringValue(Long.class, text, "not a long");
            }
        }
    };

    public static final ValueDeserializer<Double> DOUBLE = new ValueDeserializer<>() {
        @Override
        public Double deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String text = readNumberString(p, ctxt, Double.class);
            if (text == null) {
                return null;
            }
            try {
                return Double.valueOf(text);
            } catch (NumberFormatException ex) {
                return (Double) ctxt.handleWeirdStringValue(Double.class, text, "not a double");
            }
        }
    };

    public static final ValueDeserializer<Float> FLOAT = new ValueDeserializer<>() {
        @Override
        public Float deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String text = readNumberString(p, ctxt, Float.class);
            if (text == null) {
                return null;
            }
            try {
                return Float.valueOf(text);
            } catch (NumberFormatException ex) {
                return (Float) ctxt.handleWeirdStringValue(Float.class, text, "not a float");
            }
        }
    };

    public static final ValueDeserializer<Short> SHORT = new ValueDeserializer<>() {
        @Override
        public Short deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String text = readNumberString(p, ctxt, Short.class);
            if (text == null) {
                return null;
            }
            try {
                return Short.valueOf(text);
            } catch (NumberFormatException ex) {
                return (Short) ctxt.handleWeirdStringValue(Short.class, text, "not a short");
            }
        }
    };

    public static final ValueDeserializer<Byte> BYTE = new ValueDeserializer<>() {
        @Override
        public Byte deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String text = readNumberString(p, ctxt, Byte.class);
            if (text == null) {
                return null;
            }
            try {
                return Byte.valueOf(text);
            } catch (NumberFormatException ex) {
                return (Byte) ctxt.handleWeirdStringValue(Byte.class, text, "not a byte");
            }
        }
    };

    public static final ValueDeserializer<BigInteger> BIG_INTEGER = new ValueDeserializer<>() {
        @Override
        public BigInteger deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String text = readNumberString(p, ctxt, BigInteger.class);
            if (text == null) {
                return null;
            }
            try {
                return new BigInteger(text);
            } catch (NumberFormatException ex) {
                return (BigInteger) ctxt.handleWeirdStringValue(BigInteger.class, text, "not a biginteger");
            }
        }
    };

    public static final ValueDeserializer<BigDecimal> BIG_DECIMAL = new ValueDeserializer<>() {
        @Override
        public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String text = readNumberString(p, ctxt, BigDecimal.class);
            if (text == null) {
                return null;
            }
            try {
                return new BigDecimal(text);
            } catch (NumberFormatException ex) {
                return (BigDecimal) ctxt.handleWeirdStringValue(BigDecimal.class, text, "not a bigdecimal");
            }
        }
    };

    private static String readNumberString(JsonParser p, DeserializationContext ctxt, Class<?> target)
            throws JacksonException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
            return p.getText();
        }
        if (token == JsonToken.VALUE_TRUE) {
            return "1";
        }
        if (token == JsonToken.VALUE_FALSE) {
            return "0";
        }
        if (token == JsonToken.VALUE_STRING) {
            String raw = p.getText();
            if (raw == null) {
                return null;
            }
            String text = raw.trim();
            return text.isEmpty() ? null : text;
        }
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        ctxt.handleUnexpectedToken(target, p);
        return null;
    }
}
