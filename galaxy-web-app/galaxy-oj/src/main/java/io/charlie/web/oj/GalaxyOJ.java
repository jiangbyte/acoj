package io.charlie.web.oj;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description OJ启动器
 */
@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching  // 开启缓存
//@EnableDubbo
public class GalaxyOJ {

    @Value("${profiles.active:default}")
    private String activeProfile;

    @PostConstruct
    public void checkProfile() {
        log.info("=== @Value注入的profile: {} ===", activeProfile);
    }

    @Bean
    public CommandLineRunner printProfile() {
        return args -> {
            log.info("=== CommandLineRunner - Maven Profile Active: {} ===", activeProfile);
            System.out.println("=== Maven Profile Active: " + activeProfile + " ===");
        };
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GalaxyOJ.class, args);
        Environment env = run.getEnvironment();

        // 检查环境变量中的属性
        String mavenProfile = env.getProperty("profiles.active");
        log.info("=== Environment中的profiles.active: {} ===", mavenProfile);

        String port = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path");

        String displayContextPath = (contextPath == null || contextPath.isEmpty()) ? "" : contextPath;

        log.info("""
                                            \s
                     -------------------------------------------------
                         Application is running! Access URLs:
                         Local:    http://localhost:{}{}
                         Doc:      http://localhost:{}{}/doc.html
                     -------------------------------------------------
                    \s""",
                port,
                displayContextPath,
                port,
                displayContextPath
        );
    }

    @RequiredArgsConstructor
    @RestController
    static class IndexController {
        private final Environment env;

        @GetMapping("/")
        public String index() {
            return "UP";
        }

    }
}
