package github.jiangbyte.io.common.oss.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 对象存储自动配置入口：装配 S3 兼容实现。
 *
 * Author: Charlie
 */
@AutoConfiguration
@EnableConfigurationProperties(OssProperties.class)
public class OssAutoConfiguration {
}
