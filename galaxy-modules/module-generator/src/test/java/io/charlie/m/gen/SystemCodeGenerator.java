package io.charlie.m.gen;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.builder.CustomFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.model.ClassAnnotationAttributes;
import io.charlie.galaxy.pojo.CommonEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: charlie-zhang-code
 * @Date: 2025/2/17
 * @Description: 代码生成器
 */
public class SystemCodeGenerator {
    @Data
    @AllArgsConstructor
    static class Module {
        private String gModule;
        private String moduleName;
        private String basePackagePath;

        // 参数报包名
        private String paramPackageName;
        private String controllerPackageName;
        private String servicePackageName;
        private String mapperPackageName;
        private String serviceImplPackageName;

        private List<String> tables;

        public Module(String gModule, String moduleName, List<String> tables) {
            this.gModule = gModule;
            this.moduleName = moduleName;
            this.tables = tables;
        }
    }

    public static void main(String[] args) {
        String outputDir = Paths.get("E:\\MyProjects\\inception\\inception-java\\inception-system").toFile().getAbsolutePath();

        DataSourceConfig.Builder datasourceBuilder = new DataSourceConfig.Builder(
                "jdbc:mysql://localhost:3306/astro_code?serverTimezone=Asia/Shanghai",
                "root",
                "123456");

        List<Module> modules = new ArrayList<>();

//        modules.add(new Module("system","io.charlie.app.system.modular.banner", List.of("sys_banner")));
//        modules.add(new Module("system","io.charlie.app.system.modular.user", List.of("sys_user")));
//        modules.add(new Module("system","io.charlie.app.system.modular.role", List.of("sys_role")));
//        modules.add(new Module("system","io.charlie.app.system.modular.dict", List.of("sys_dict_data", "sys_dict_type")));
//        modules.add(new Module("system","io.charlie.app.system.modular.group", List.of("sys_group")));
//        modules.add(new Module("system","io.charlie.app.system.modular.notice", List.of("sys_notice")));
//
//        modules.add(new Module("problem","io.charlie.app.problem.modular.category", List.of("pro_category")));
//        modules.add(new Module("problem","io.charlie.app.problem.modular.tag", List.of("pro_tag")));
//        modules.add(new Module("problem","io.charlie.app.problem.modular.problem", List.of("pro_problem")));
//        modules.add(new Module("problem","io.charlie.app.problem.modular.problem.submit", List.of("pro_submit")));
//        modules.add(new Module("problem","io.charlie.app.problem.modular.problem.solved", List.of("pro_solved")));
//        modules.add(new Module("problem","io.charlie.app.problem.modular.set", List.of("pro_set")));
//        modules.add(new Module("problem","io.charlie.app.problem.modular.set.submit", List.of("pro_set_submit")));
//        modules.add(new Module("problem","io.charlie.app.problem.modular.set.solved", List.of("pro_set_solved")));
//        modules.add(new Module("problem","io.charlie.app.problem.modular.set.progress", List.of("pro_set_progress")));

        modules.add(new Module("core", "io.charlie.app.core.modular.banner", List.of("sys_banner")));
        modules.add(new Module("core", "io.charlie.app.core.modular.user", List.of("sys_user")));
        modules.add(new Module("core", "io.charlie.app.core.modular.role", List.of("sys_role")));
        modules.add(new Module("core", "io.charlie.app.core.modular.dict", List.of("sys_dict_data", "sys_dict_type")));
        modules.add(new Module("core", "io.charlie.app.core.modular.group", List.of("sys_group")));
        modules.add(new Module("core", "io.charlie.app.core.modular.notice", List.of("sys_notice")));
        modules.add(new Module("core", "io.charlie.app.core.modular.notice", List.of("sys_notice")));
        modules.add(new Module("core", "io.charlie.app.core.modular.config", List.of("sys_config")));
        modules.add(new Module("core", "io.charlie.app.core.modular.article", List.of("sys_article")));

        modules.add(new Module("core", "io.charlie.app.core.modular.category", List.of("pro_category")));
        modules.add(new Module("core", "io.charlie.app.core.modular.tag", List.of("pro_tag")));
        modules.add(new Module("core", "io.charlie.app.core.modular.problem", List.of("pro_problem")));
        modules.add(new Module("core", "io.charlie.app.core.modular.problem.submit", List.of("pro_submit")));
        modules.add(new Module("core", "io.charlie.app.core.modular.problem.solved", List.of("pro_solved")));
        modules.add(new Module("core", "io.charlie.app.core.modular.set", List.of("pro_set")));
        modules.add(new Module("core", "io.charlie.app.core.modular.set.submit", List.of("pro_set_submit")));
        modules.add(new Module("core", "io.charlie.app.core.modular.set.solved", List.of("pro_set_solved")));
        modules.add(new Module("core", "io.charlie.app.core.modular.set.progress", List.of("pro_set_progress")));

        for (Module module : modules) {
            FastAutoGenerator.create(datasourceBuilder)
                    /**
                     * 全局配置
                     * 可以配置作者、目录这些
                     */
                    .globalConfig(builder -> {
                        builder.author("Charlie Zhang") // 作者
                                .disableOpenDir() // 禁止打开输出目录
                                .enableSpringdoc()
                                .outputDir("E:\\MyProjects\\astro-code-oj\\gen"); // 指定输出目录
                    })

                    /**
                     * 包配置
                     */
                    .packageConfig(builder -> builder
                                    .parent(module.getModuleName()) // 设置父包名
//                        .entity("model.entity") // 实体类包
//                        .service("service")
//                        .mapper("mapper")
//                        .controller("controller")
//                        .xml("mapper")
//                                    .pathInfo(Collections.singletonMap(OutputFile.xml, outputDir + "/src/main/resources/mapper")) // 设置mapperXml生成路径
                    )

                    /**
                     * 策略配置
                     */
                    .strategyConfig(builder -> builder
                                    .addInclude(module.getTables()) // 设置需要生成的表名
//                                .addTablePrefix("sys_") // 设置过滤表前缀

                                    /**
                                     * Entity
                                     */
                                    .entityBuilder()
//                                .enableFileOverride() // 覆盖已生成文件
                                    .superClass(CommonEntity.class) // 父类
//                                    .addSuperEntityColumns("create_time", "create_user", "update_time", "update_user", "deleted")
                                    .enableLombok(new ClassAnnotationAttributes("@Data", "lombok.Data")) // 开启 lombok
                                    .naming(NamingStrategy.underline_to_camel) // 数据库表映射到实体的命名策略
                                    .formatFileName("%s")

                                    /**
                                     * Service
                                     */
                                    .serviceBuilder()
//                                .enableFileOverride()
                                    .formatServiceFileName("%sService")
                                    .formatServiceImplFileName("%sServiceImpl")

                                    /**
                                     * Mapper
                                     */
                                    .mapperBuilder()
                                    .mapperAnnotation(Mapper.class)
//                                .enableBaseResultMap()
//                                .enableBaseColumnList()
                                    .formatMapperFileName("%sMapper")
                                    .formatXmlFileName("%sMapper")

                                    /**
                                     * Controller
                                     */
                                    .controllerBuilder()
//                        .enableFileOverride() // 覆盖已生成文件
                                    .enableHyphenStyle()
                                    .enableRestStyle() // @RestController
                                    .formatFileName("%sController")
                    )
                    .injectionConfig(injectConfig -> injectConfig
                            .customMap(Map.of("GModule", module.getGModule()))
                            .customFile(List.of(
                                    new CustomFile.Builder()
                                            .fileName("AddParam.java") // 文件名称
                                            .templatePath("/backend/AddParam.java.ftl") // 生成模板路径
                                            .packageName("param")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("EditParam.java") // 文件名称
                                            .templatePath("/backend/EditParam.java.ftl") // 生成模板路径
                                            .packageName("param")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("IdParam.java") // 文件名称
                                            .templatePath("/backend/IdParam.java.ftl") // 生成模板路径
                                            .packageName("param")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("PageParam.java") // 文件名称
                                            .templatePath("/backend/PageParam.java.ftl") // 生成模板路径
                                            .packageName("param")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),

                                    new CustomFile.Builder()
                                            .fileName("Controller.java")
                                            .templatePath("/backend/Controller.java.ftl")
                                            .packageName("controller")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("Mapper.java")
                                            .templatePath("/backend/Mapper.java.ftl")
                                            .packageName("mapper")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),
//                                    new CustomFile.Builder()
//                                            .fileName("Mapper.xml")
//                                            .templatePath("/backend/Mapper.xml.ftl")
//                                            .packageName("mapper.mapping")
//                                            .enableFileOverride() // 覆盖已生成文件
//                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName(".java")
                                            .templatePath("/backend/IEntity.java.ftl")
                                            .packageName("entity")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),

                                    new CustomFile.Builder()
                                            .fileName("Service.java")
                                            .templatePath("/backend/Service.java.ftl")
                                            .packageName("service")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("ServiceImpl.java")
                                            .templatePath("/backend/ServiceImpl.java.ftl")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .packageName("service.impl")
                                            .build(),

                                    new CustomFile.Builder()
                                            .fileName("Api.ts")
                                            .templatePath("/admin/Api.ts.ftl")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .packageName("api.admin")
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("index.vue")
                                            .formatNameFunction(tableInfo -> "")
                                            .templatePath("/admin/index.vue.ftl")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .packageName("api.admin.vue")
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("form.vue")
                                            .formatNameFunction(tableInfo -> "")
                                            .templatePath("/admin/form.vue.ftl")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .packageName("api.admin.vue")
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("detail.vue")
                                            .formatNameFunction(tableInfo -> "")
                                            .templatePath("/admin/detail.vue.ftl")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .packageName("api.admin.vue")
                                            .build(),

                                    new CustomFile.Builder()
                                            .fileName("Api.ts")
                                            .templatePath("/frontend/Api.ts.ftl")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .packageName("api.frontend")
                                            .build()
                            )))
                    .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                    .execute();
        }
    }
}