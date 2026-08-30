package github.jiangbyte.io.common.web.jackson;

import github.jiangbyte.io.common.core.jackson.Sensitive;
import github.jiangbyte.io.common.core.jackson.SensitiveMasker;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * 按 {@link Sensitive} 配置输出脱敏后的字符串。
 *
 * Author: Charlie
 */
public class SensitiveJsonSerializer extends ValueSerializer<Object> {

    private final Sensitive sensitive;

    public SensitiveJsonSerializer(Sensitive sensitive) {
        this.sensitive = sensitive;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext ctxt) {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (value instanceof String str) {
            gen.writeString(SensitiveMasker.mask(str, sensitive));
            return;
        }
        // 非字符串：整段固定掩码，避免泄露 toString
        String allMask = sensitive == null || sensitive.mask() == null || sensitive.mask().isEmpty()
                ? "***"
                : sensitive.mask();
        gen.writeString(allMask);
    }
}
