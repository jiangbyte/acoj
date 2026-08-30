package github.jiangbyte.io.sys.modules.job.support;

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.job.JobHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务处理器注册表：收集容器中所有 JobHandler Bean，按真实类名（去代理）建立索引。
 *
 * Author: Charlie
 */
@Component
public class JobInvoker {

    private final Map<String, JobHandler> handlerMap;

    public JobInvoker(ApplicationContext applicationContext) {
        Map<String, JobHandler> handlers = applicationContext.getBeansOfType(JobHandler.class);
        Map<String, JobHandler> indexed = new HashMap<>();
        handlers.values().forEach(handler -> {
            Class<?> userClass = ClassUtils.getUserClass(handler.getClass());
            indexed.putIfAbsent(userClass.getName(), handler);
        });
        this.handlerMap = Map.copyOf(indexed);
    }

    /** 按处理器标识解析 JobHandler；不存在时抛出业务异常。 */
    public JobHandler resolve(String handler) {
        JobHandler resolved = handlerMap.get(handler);
        if (resolved == null) {
            throw new BizException("Job handler not found: " + handler);
        }
        return resolved;
    }
}
