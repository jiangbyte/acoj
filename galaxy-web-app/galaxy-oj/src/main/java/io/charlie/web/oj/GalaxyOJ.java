package io.charlie.web.oj;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description TODO
 */
@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching  // 开启缓存
public class GalaxyOJ {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GalaxyOJ.class, args);
        Environment env = run.getEnvironment();
        log.info("""
                                                \s
                         -------------------------------------------------
                             Application is running! Access URLs:
                             Local:    http://localhost:{}{}
                             Doc:      http://localhost:{}{}/doc.html
                         -------------------------------------------------
                        \s""",
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path"),
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path")
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
