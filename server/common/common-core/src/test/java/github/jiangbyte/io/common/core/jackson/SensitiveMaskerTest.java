package github.jiangbyte.io.common.core.jackson;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Author: Charlie
 */
class SensitiveMaskerTest {

    @Test
    void allUsesFixedMask() {
        assertEquals("***", SensitiveMasker.mask("secret", ann(SensitiveStrategy.ALL, 0, -1, 0, 0, '*', "***")));
        assertEquals("###", SensitiveMasker.mask("secret", ann(SensitiveStrategy.ALL, 0, -1, 0, 0, '*', "###")));
    }

    @Test
    void rangeMatchesSubstringSemantics() {
        // "123456789".substring(3, 7) → "4567" masked
        assertEquals("123****89", SensitiveMasker.maskRange("123456789", 3, 7, '*'));
        assertEquals("***456789", SensitiveMasker.maskRange("123456789", 0, 3, '*'));
        assertEquals("123******", SensitiveMasker.maskRange("123456789", 3, -1, '*'));
        assertEquals("123456789", SensitiveMasker.maskRange("123456789", 5, 5, '*'));
        assertEquals("123456789", SensitiveMasker.maskRange("123456789", 20, 30, '*'));
    }

    @Test
    void keepPrefixAndSuffix() {
        assertEquals("138****5678", SensitiveMasker.maskKeep("13812345678", 3, 4, '*'));
        assertEquals("abcdefghij", SensitiveMasker.maskKeep("abcdefghij", 5, 5, '*'));
        assertEquals("a********j", SensitiveMasker.maskKeep("abcdefghij", 1, 1, '*'));
    }

    @Test
    void nullPassthrough() {
        assertNull(SensitiveMasker.mask(null, ann(SensitiveStrategy.ALL, 0, -1, 0, 0, '*', "***")));
    }

    private static Sensitive ann(
            SensitiveStrategy strategy,
            int from,
            int to,
            int keepPrefix,
            int keepSuffix,
            char maskChar,
            String mask) {
        return new Sensitive() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Sensitive.class;
            }

            @Override
            public SensitiveStrategy strategy() {
                return strategy;
            }

            @Override
            public int from() {
                return from;
            }

            @Override
            public int to() {
                return to;
            }

            @Override
            public int keepPrefix() {
                return keepPrefix;
            }

            @Override
            public int keepSuffix() {
                return keepSuffix;
            }

            @Override
            public char maskChar() {
                return maskChar;
            }

            @Override
            public String mask() {
                return mask;
            }
        };
    }
}
