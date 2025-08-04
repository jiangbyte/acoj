package io.charlie.app.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigDecimal;
import java.math.BigInteger;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 注册各种类型转换器
        registry.addConverter(new StringToBooleanConverter());
        registry.addConverter(new StringToIntegerConverter());
        registry.addConverter(new StringToLongConverter());
        registry.addConverter(new StringToDoubleConverter());
        registry.addConverter(new StringToFloatConverter());
        registry.addConverter(new StringToBigDecimalConverter());
        registry.addConverter(new StringToBigIntegerConverter());
    }

    public static class StringToBooleanConverter implements Converter<String, Boolean> {
        @Override
        public Boolean convert(String source) {
            if (isNullOrEmpty(source)) {
                return null;
            }
            return Boolean.parseBoolean(source);
        }
    }

    public static class StringToIntegerConverter implements Converter<String, Integer> {
        @Override
        public Integer convert(String source) {
            if (isNullOrEmpty(source)) {
                return null;
            }
            return Integer.parseInt(source.trim());
        }
    }

    public static class StringToLongConverter implements Converter<String, Long> {
        @Override
        public Long convert(String source) {
            if (isNullOrEmpty(source)) {
                return null;
            }
            return Long.parseLong(source.trim());
        }
    }

    public static class StringToDoubleConverter implements Converter<String, Double> {
        @Override
        public Double convert(String source) {
            if (isNullOrEmpty(source)) {
                return null;
            }
            return Double.parseDouble(source.trim());
        }
    }

    public static class StringToFloatConverter implements Converter<String, Float> {
        @Override
        public Float convert(String source) {
            if (isNullOrEmpty(source)) {
                return null;
            }
            return Float.parseFloat(source.trim());
        }
    }

    public static class StringToBigDecimalConverter implements Converter<String, BigDecimal> {
        @Override
        public BigDecimal convert(String source) {
            if (isNullOrEmpty(source)) {
                return null;
            }
            return new BigDecimal(source.trim());
        }
    }

    public static class StringToBigIntegerConverter implements Converter<String, BigInteger> {
        @Override
        public BigInteger convert(String source) {
            if (isNullOrEmpty(source)) {
                return null;
            }
            return new BigInteger(source.trim());
        }
    }

    // 辅助方法，检查字符串是否为null或空或"null"
    private static boolean isNullOrEmpty(String source) {
        return source == null || source.trim().isEmpty() || "null".equalsIgnoreCase(source.trim());
    }
}