package io.charlie.web.oj;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

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
public class GalaxyOJ {

    public static void main(String[] args) {
        SpringApplication.run(GalaxyOJ.class, args);
    }
}
