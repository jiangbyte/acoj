package github.jiangbyte.io.common.mybatis.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import github.jiangbyte.io.common.mybatis.datasource.DataSourceRoutingAspect;
import github.jiangbyte.io.common.mybatis.datasource.DataSourceStickyClearFilter;
import github.jiangbyte.io.common.mybatis.dialect.DbDialect;
import github.jiangbyte.io.common.mybatis.dialect.DbDialectDetector;
import github.jiangbyte.io.common.mybatis.dialect.DbDialectHolder;
import github.jiangbyte.io.common.mybatis.dialect.DbVendor;
import github.jiangbyte.io.common.mybatis.dialect.MysqlDialect;
import github.jiangbyte.io.common.mybatis.dialect.PostgreSqlDialect;
import github.jiangbyte.io.common.mybatis.handler.HeiMetaObjectHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * MyBatis-Plus 自动配置：注册方言、分页、元对象填充与数据源路由。
 *
 * Author: Charlie
 */
@AutoConfiguration
@ComponentScan("github.jiangbyte.io.common.mybatis")
public class MybatisAutoConfiguration {

    /** 按 master JDBC URL（或连接元数据）注册唯一方言 Bean。 */
    @Bean
    @ConditionalOnMissingBean
    public DbDialect dbDialect(Environment environment, ObjectProvider<DataSource> dataSourceProvider) {
        String jdbcUrl = firstNonBlank(
                environment.getProperty("spring.datasource.dynamic.datasource.master.url"),
                environment.getProperty("spring.datasource.url"));
        String productName = null;
        DbVendor vendor = DbDialectDetector.fromJdbcUrl(jdbcUrl);
        if (vendor == null) {
            DataSource dataSource = dataSourceProvider.getIfAvailable();
            if (dataSource != null) {
                try (Connection connection = dataSource.getConnection()) {
                    productName = connection.getMetaData().getDatabaseProductName();
                    vendor = DbDialectDetector.fromProductName(productName);
                } catch (Exception ignored) {
                    // fall through to require()
                }
            }
        }
        if (vendor == null) {
            vendor = DbDialectDetector.require(jdbcUrl, productName);
        }
        DbDialect dialect = switch (vendor) {
            case POSTGRESQL -> new PostgreSqlDialect();
            case MYSQL -> new MysqlDialect();
        };
        DbDialectHolder.set(dialect);
        return dialect;
    }

    /** 注册 MyBatis-Plus 拦截器（含按方言分页）。 */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(DbDialect dbDialect) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbDialect.mybatisPlusDbType()));
        return interceptor;
    }

    /** 注册元对象自动填充处理器。 */
    @Bean
    public HeiMetaObjectHandler heiMetaObjectHandler() {
        return new HeiMetaObjectHandler();
    }

    /** 注册读写数据源路由切面（不依赖应用根包扫描）。 */
    @Bean
    @ConditionalOnMissingBean
    public DataSourceRoutingAspect dataSourceRoutingAspect() {
        return new DataSourceRoutingAspect();
    }

    /** 请求结束清理写后粘主标记。 */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnMissingBean(name = "dataSourceStickyClearFilter")
    public FilterRegistrationBean<DataSourceStickyClearFilter> dataSourceStickyClearFilter() {
        FilterRegistrationBean<DataSourceStickyClearFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new DataSourceStickyClearFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        registration.setName("dataSourceStickyClearFilter");
        return registration;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}
