package github.jiangbyte.io.common.mybatis.config;

import org.dromara.cache.service.BothCacheService;
import org.dromara.cache.service.RedisCacheService;
import org.dromara.cache.service.TransCacheManager;
import org.dromara.common.constant.TransConfig;
import org.dromara.common.spring.SpringContextUtil;
import org.dromara.core.trans.convert.Convert;
import org.dromara.core.trans.util.ConvertUtil;
import org.dromara.trans.advice.ReleaseTransCacheAdvice;
import org.dromara.trans.aop.TransMethodResultAop;
import org.dromara.trans.controller.TransProxyController;
import org.dromara.trans.ds.DataSourceSetter;
import org.dromara.trans.listener.TransMessageListener;
import org.dromara.trans.service.impl.AutoTransService;
import org.dromara.trans.service.impl.DictionaryTransService;
import org.dromara.trans.service.impl.EnumTransService;
import org.dromara.trans.service.impl.RpcTransService;
import org.dromara.trans.service.impl.SimpleTransService;
import org.dromara.trans.service.impl.TransService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Easy-Trans 与 Spring Boot 4 兼容自动配置：过滤不兼容导入并注册翻译相关 Bean。
 *
 * Author: Charlie
 */
@AutoConfiguration(afterName = "org.dromara.trans.config.EasyTransMybatisPlusConfig")
@Import(TransPojoOnlyResponseBodyAdvice.class)
public class EasyTransBoot4AutoConfiguration implements InitializingBean {

    @Value("${easy-trans.multiple-data-sources:false}")
    private boolean multipleDataSources;

    @Autowired(required = false)
    private DataSourceSetter dataSourceSetter;

    /** 注册翻译服务门面。 */
    @Bean
    public TransService transService() {
        return new TransService();
    }

    /** 注册自动翻译服务。 */
    @Bean
    @DependsOn("springContextUtil")
    public AutoTransService autoTransService() {
        return new AutoTransService();
    }

    /**
     * 官方方法签名带有未使用的 {@link SimpleTransService} 参数（仅约束装配顺序）。
     * 此处去掉该参数：SimpleTrans 的创建已由 {@code afterName = EasyTransMybatisPlusConfig}
     * 保证；再依赖同配置类内的 SimpleTrans 会与 {@code @ConditionalOnBean} 评估时机冲突。
     */
    @Bean
    public DictionaryTransService dictionaryTransService() {
        return new DictionaryTransService();
    }

    /** 注册翻译缓存释放 Advice。 */
    @Bean
    public ReleaseTransCacheAdvice releaseTransCacheAdvice() {
        return new ReleaseTransCacheAdvice();
    }

    /** 注册枚举翻译服务。 */
    @Bean
    public EnumTransService enumTransService() {
        return new EnumTransService();
    }

    /** 注册简单关联翻译服务。 */
    @Bean
    @Primary
    @ConditionalOnBean(SimpleTransService.SimpleTransDiver.class)
    public SimpleTransService simpleTransService(
            SimpleTransService.SimpleTransDiver dirver,
            RpcTransService rpcTransService) {
        SimpleTransService result = new SimpleTransService();
        result.regsiterTransDiver(dirver);
        return result;
    }

    /**
     * 供 {@link RpcTransService} 使用的 HTTP 客户端。
     * <p>
     * 刻意不注入 Boot 4 的 {@code org.springframework.boot.restclient.RestTemplateBuilder}：
     * 本模块未强制依赖 {@code spring-boot-restclient}；官方写法的价值主要在统一超时/消息转换与
     * {@code @LoadBalanced}，而当前 {@code easy-trans.is-enable-cloud=false}，直连 {@code RestTemplate} 足够。
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return new RestTemplate(factory);
    }

    /** 注册翻译缓存管理器。 */
    @Bean
    public TransCacheManager transCacheManager() {
        return new TransCacheManager();
    }

    /** 注册双写缓存服务。 */
    @Bean
    public BothCacheService bothCacheService() {
        return new BothCacheService();
    }

    /** 注册 RPC 翻译服务。 */
    @Bean
    @ConditionalOnBean(SimpleTransService.SimpleTransDiver.class)
    public RpcTransService rpcTransService(
            SimpleTransService.SimpleTransDiver dirver,
            RestTemplate restTemplate) {
        RpcTransService result = new RpcTransService();
        result.regsiterTransDiver(dirver);
        result.setRestTemplate(restTemplate);
        return result;
    }

    /** 注册翻译代理 Controller。 */
    @Bean
    @ConditionalOnBean(SimpleTransService.SimpleTransDiver.class)
    public TransProxyController transProxyController(SimpleTransService.SimpleTransDiver dirver) {
        TransProxyController result = new TransProxyController();
        result.setSimpleTransDiver(dirver);
        return result;
    }

    /** 注册方法结果翻译 AOP。 */
    @Bean
    public TransMethodResultAop transMethodResultAop() {
        return new TransMethodResultAop();
    }

    /** 注册翻译消息监听器。 */
    @Bean
    @ConditionalOnProperty(name = "easy-trans.is-enable-redis", havingValue = "true")
    public TransMessageListener transMessageListener() {
        return new TransMessageListener();
    }

    @Bean
    @ConditionalOnProperty(name = "easy-trans.is-enable-redis", havingValue = "true")
    RedisMessageListenerContainer container(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("trans"));
        container.setTopicSerializer(new StringRedisSerializer());
        return container;
    }

    @Bean
    @ConditionalOnProperty(name = "easy-trans.is-enable-redis", havingValue = "true")
    MessageListenerAdapter listenerAdapter(TransMessageListener receiver, RedisTemplate<?, ?> redisTemplate) {
        MessageListenerAdapter result = new MessageListenerAdapter(receiver, "handelMsg");
        result.setSerializer(redisTemplate.getValueSerializer());
        return result;
    }

    /** 注册 Redis 翻译缓存服务。 */
    @Bean
    @ConditionalOnProperty(name = "easy-trans.is-enable-redis", havingValue = "true")
    public RedisCacheService redisCacheService(RedisTemplate<?, ?> redisTemplate, AutoTransService autoTransService) {
        RedisCacheService redisCacheService = new RedisCacheService();
        redisCacheService.setRedisTemplate(redisTemplate);
        redisCacheService.setStrRedisTemplate(redisTemplate);
        autoTransService.setRedisTransCache(redisCacheService);
        return redisCacheService;
    }

    /** 注册 SpringContextUtil。 */
    @Bean("springContextUtil")
    public SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }

    @Autowired(required = false)
    public void setConvertUtil(Convert convert) {
        ConvertUtil.setConvert(convert);
    }

    /** 初始化后校验多数据源等配置。 */
    @Override
    public void afterPropertiesSet() {
        TransConfig.MULTIPLE_DATA_SOURCES = this.multipleDataSources;
        if (TransConfig.MULTIPLE_DATA_SOURCES && dataSourceSetter == null) {
            throw new IllegalArgumentException(
                    "easytrans 如果开启多数据源支持，需要自定义 DataSourceSetter 来切换数据源");
        }
        TransConfig.dataSourceSetter = this.dataSourceSetter;
    }
}
