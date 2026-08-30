package github.jiangbyte.io.common.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置：注册 BCrypt PasswordEncoder Bean。
 *
 * Author: Charlie
 */
@AutoConfiguration
public class PasswordEncoderConfig {

    /** 注册 BCrypt 密码编码器。 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
