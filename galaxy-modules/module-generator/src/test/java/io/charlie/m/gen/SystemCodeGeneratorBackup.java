package io.charlie.m.gen;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.builder.CustomFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.model.ClassAnnotationAttributes;
import io.charlie.galaxy.pojo.CommonEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.type.JdbcType;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: charlie-zhang-code
 * @Date: 2025/2/17
 * @Description: 代码生成器
 */
public class SystemCodeGeneratorBackup {

    public static List<Module> getModuleList() {
        List<Module> modules = new ArrayList<>();
        modules.add(new Module(
                "core",
                "banner",
                "io.charlie.app.core.modular.sys",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("sys_banner"))
        );
        modules.add(new Module(
                "core",
                "user",
                "io.charlie.app.core.modular.sys",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("sys_user"))
        );
        modules.add(new Module(
                "core",
                "role",
                "io.charlie.app.core.modular.sys",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("sys_role"))
        );
//        modules.add(new Module(
//                "core",
//                "dict",
//                "io.charlie.app.core.modular.sys",
//                "param",
//                "entity",
//                "controller",
//                "service",
//                "mapper",
//                "service.impl",
//                List.of("sys_dict_data", "sys_dict_type"))
//        );
        modules.add(new Module(
                "core",
                "dict",
                "io.charlie.app.core.modular.sys",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("sys_dict"))
        );
        modules.add(new Module(
                "core",
                "group",
                "io.charlie.app.core.modular.sys",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("sys_group"))
        );
        modules.add(new Module(
                "core",
                "notice",
                "io.charlie.app.core.modular.sys",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("sys_notice"))
        );
        modules.add(new Module(
                "core",
                "config",
                "io.charlie.app.core.modular.sys",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("sys_config"))
        );
        modules.add(new Module(
                "core",
                "article",
                "io.charlie.app.core.modular.sys",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("sys_article"))
        );
        // =================================================
        modules.add(new Module(
                "core",
                "category",
                "io.charlie.app.core.modular",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("pro_category"))
        );
        modules.add(new Module(
                "core",
                "tag",
                "io.charlie.app.core.modular",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("pro_tag"))
        );
        //  ======================================
        modules.add(new Module(
                "core",
                "problem",
                "io.charlie.app.core.modular.problem",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("pro_problem"))
        );
        modules.add(new Module(
                "core",
                "solved",
                "io.charlie.app.core.modular.problem",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("pro_solved"))
        );
        modules.add(new Module(
                "core",
                "submit",
                "io.charlie.app.core.modular.problem",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("pro_submit"))
        );
        modules.add(new Module(
                "core",
                "similarity.task",
                "io.charlie.app.core.modular.problem",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("pro_similarity_task"))
        );
        modules.add(new Module(
                "core",
                "similarity.result",
                "io.charlie.app.core.modular.problem",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("pro_similarity_result"))
        );
        //  ======================================
        modules.add(new Module(
                "core",
                "set",
                "io.charlie.app.core.modular.set",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("pro_set"))
        );
        modules.add(new Module(
                "core",
                "solved",
                "io.charlie.app.core.modular.set",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("pro_set_solved"))
        );
        modules.add(new Module(
                "core",
                "submit",
                "io.charlie.app.core.modular.set",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("pro_set_submit"))
        );
        modules.add(new Module(
                "core",
                "progress",
                "io.charlie.app.core.modular.set",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("pro_set_progress"))
        );
        modules.add(new Module(
                "core",
                "similarity.task",
                "io.charlie.app.core.modular.set",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("pro_set_similarity_task"))
        );
        modules.add(new Module(
                "core",
                "similarity.result",
                "io.charlie.app.core.modular.set",
                "param",
                "entity",
                "controller",
                "service",
                "mapper",
                "service.impl",
                List.of("pro_set_similarity_result"))
        );
        return modules;
    }

    @Data
    @AllArgsConstructor
    static class Module {
        private String gModule;
        private String moduleName;
        private String basePackagePath;

        // 参数报包名
        private String paramPackageName;
        private String entityPackageName;
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
                "jdbc:mysql://localhost:3306/astro_code?serverTimezone=Asia/Shanghai&tinyInt1isBit=true",
                "root",
                "123456");

        List<Module> modules = getModuleList();

        for (Module module : modules) {
            FastAutoGenerator.create(datasourceBuilder)
                    .dataSourceConfig(builder ->
                            builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                                // 兼容旧版本转换成Integer
                                if (JdbcType.TINYINT == metaInfo.getJdbcType()) {
                                    return DbColumnType.INTEGER;
                                }
                                return typeRegistry.getColumnType(metaInfo);
                            })
                    )
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
                                    .parent(module.getBasePackagePath() + "." + module.getModuleName()) // 设置父包名
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
                                            .packageName(module.getParamPackageName())
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("EditParam.java") // 文件名称
                                            .templatePath("/backend/EditParam.java.ftl") // 生成模板路径
                                            .packageName(module.getParamPackageName())
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("IdParam.java") // 文件名称
                                            .templatePath("/backend/IdParam.java.ftl") // 生成模板路径
                                            .packageName(module.getParamPackageName())
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("PageParam.java") // 文件名称
                                            .templatePath("/backend/PageParam.java.ftl") // 生成模板路径
                                            .packageName(module.getParamPackageName())
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),

                                    new CustomFile.Builder()
                                            .fileName("Controller.java")
                                            .templatePath("/backend/Controller.java.ftl")
                                            .packageName(module.getControllerPackageName())
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("Mapper.java")
                                            .templatePath("/backend/Mapper.java.ftl")
                                            .packageName(module.getMapperPackageName())
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName(".java")
                                            .templatePath("/backend/IEntity.java.ftl")
                                            .packageName(module.getEntityPackageName())
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),

                                    new CustomFile.Builder()
                                            .fileName("Service.java")
                                            .templatePath("/backend/Service.java.ftl")
                                            .packageName(module.getServicePackageName())
                                            .enableFileOverride() // 覆盖已生成文件
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("ServiceImpl.java")
                                            .templatePath("/backend/ServiceImpl.java.ftl")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .packageName(module.getServiceImplPackageName())
                                            .build(),

                                    new CustomFile.Builder()
                                            .fileName("Api.ts")
                                            .templatePath("/admin/Api.ts.ftl")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .packageName("api.admin")
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("index.vue")
//                                            .formatNameFunction(tableInfo -> "")
                                            .templatePath("/admin/index.vue.ftl")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .packageName("api.admin.vue")
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("form.vue")
//                                            .formatNameFunction(tableInfo -> "")
                                            .templatePath("/admin/form.vue.ftl")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .packageName("api.admin.vue")
                                            .build(),
                                    new CustomFile.Builder()
                                            .fileName("detail.vue")
//                                            .formatNameFunction(tableInfo -> "")
                                            .templatePath("/admin/detail.vue.ftl")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .packageName("api.admin.vue")
                                            .build(),

                                    new CustomFile.Builder()
                                            .fileName("Api.ts")
                                            .templatePath("/frontend/Api.ts.ftl")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .packageName("api.frontend")
                                            .build(),

                                    new CustomFile.Builder()
                                            .fileName("Type.d.ts")
                                            .templatePath("/frontend/Type.d.ts.ftl")
                                            .enableFileOverride() // 覆盖已生成文件
                                            .packageName("api.type")
                                            .build()
                            )))
                    .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                    .execute();
        }
    }
}