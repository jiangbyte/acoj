package github.jiangbyte.io.common.web.jackson;

import github.jiangbyte.io.common.core.jackson.Sensitive;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 仅对标注了 {@link Sensitive} 的属性做响应脱敏（Jackson 3：ValueSerializerModifier）。
 *
 * <p>支持字段或 getter 上的注解；Lombok 生成 getter 时仍识别字段上的 {@code @Sensitive}。
 *
 * Author: Charlie
 */
public final class SensitiveNameJacksonModule extends SimpleModule {

    public static final String MODULE_NAME = "hei-sensitive-annotation";

    public SensitiveNameJacksonModule() {
        super(MODULE_NAME);
        setSerializerModifier(new ValueSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(
                    SerializationConfig config,
                    BeanDescription.Supplier beanDesc,
                    List<BeanPropertyWriter> beanProperties) {
                for (BeanPropertyWriter writer : beanProperties) {
                    Sensitive sensitive = resolveSensitive(writer);
                    if (sensitive != null) {
                        ValueSerializer<Object> ser = new SensitiveJsonSerializer(sensitive);
                        writer.assignSerializer(ser);
                    }
                }
                return beanProperties;
            }
        });
    }

    private static Sensitive resolveSensitive(BeanPropertyWriter writer) {
        AnnotatedMember member = writer.getMember();
        if (member != null) {
            Sensitive onMember = member.getAnnotation(Sensitive.class);
            if (onMember != null) {
                return onMember;
            }
        }
        if (member == null) {
            return null;
        }
        String name = writer.getName();
        for (Class<?> type = member.getDeclaringClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                return field.getAnnotation(Sensitive.class);
            } catch (NoSuchFieldException ignored) {
                // try superclass
            }
        }
        return null;
    }
}
