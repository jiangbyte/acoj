package io.charlie.web.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 20/09/2025
 * @description 网关启动类
 */
@Slf4j
//@EnableDubbo
@SpringBootApplication
public class GalaxyGateway {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GalaxyGateway.class, args);
        Environment env = run.getEnvironment();
        log.info("""
                                                \s
                         -------------------------------------------------
                             Application is running! Access URLs:
                             Local:    http://localhost:{}
                             Doc:      http://localhost:{}/doc.html
                         -------------------------------------------------
                        \s""",
                env.getProperty("server.port"),
                env.getProperty("server.port")
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
