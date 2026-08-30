package github.jiangbyte.io.common.web.jackson;

import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Jackson Module：注册字符串友好的布尔/数值反序列化器。
 *
 * Author: Charlie
 */
public final class StringlyTypedJacksonModule extends SimpleModule {

    public static final String MODULE_NAME = "hei-stringly-typed";

    public StringlyTypedJacksonModule() {
        super(MODULE_NAME);
        addSerializer(Boolean.class, ToStringSerializer.instance);
        addSerializer(Boolean.TYPE, ToStringSerializer.instance);
        addSerializer(Byte.class, ToStringSerializer.instance);
        addSerializer(Byte.TYPE, ToStringSerializer.instance);
        addSerializer(Short.class, ToStringSerializer.instance);
        addSerializer(Short.TYPE, ToStringSerializer.instance);
        addSerializer(Integer.class, ToStringSerializer.instance);
        addSerializer(Integer.TYPE, ToStringSerializer.instance);
        addSerializer(Long.class, ToStringSerializer.instance);
        addSerializer(Long.TYPE, ToStringSerializer.instance);
        addSerializer(Float.class, ToStringSerializer.instance);
        addSerializer(Float.TYPE, ToStringSerializer.instance);
        addSerializer(Double.class, ToStringSerializer.instance);
        addSerializer(Double.TYPE, ToStringSerializer.instance);
        addSerializer(BigInteger.class, ToStringSerializer.instance);
        addSerializer(BigDecimal.class, ToStringSerializer.instance);

        addDeserializer(Boolean.class, StringlyBooleanDeserializer.INSTANCE);
        addDeserializer(Boolean.TYPE, StringlyBooleanDeserializer.INSTANCE);
        addDeserializer(Integer.class, StringlyNumberDeserializers.INTEGER);
        addDeserializer(Integer.TYPE, StringlyNumberDeserializers.INTEGER);
        addDeserializer(Long.class, StringlyNumberDeserializers.LONG);
        addDeserializer(Long.TYPE, StringlyNumberDeserializers.LONG);
        addDeserializer(Double.class, StringlyNumberDeserializers.DOUBLE);
        addDeserializer(Double.TYPE, StringlyNumberDeserializers.DOUBLE);
        addDeserializer(Float.class, StringlyNumberDeserializers.FLOAT);
        addDeserializer(Float.TYPE, StringlyNumberDeserializers.FLOAT);
        addDeserializer(Short.class, StringlyNumberDeserializers.SHORT);
        addDeserializer(Short.TYPE, StringlyNumberDeserializers.SHORT);
        addDeserializer(Byte.class, StringlyNumberDeserializers.BYTE);
        addDeserializer(Byte.TYPE, StringlyNumberDeserializers.BYTE);
        addDeserializer(BigInteger.class, StringlyNumberDeserializers.BIG_INTEGER);
        addDeserializer(BigDecimal.class, StringlyNumberDeserializers.BIG_DECIMAL);
    }
}
