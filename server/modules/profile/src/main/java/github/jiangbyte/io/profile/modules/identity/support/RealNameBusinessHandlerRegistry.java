package github.jiangbyte.io.profile.modules.identity.support;

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.profile.modules.identity.service.RealNameBusinessHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 按 {@code business_type} 路由 {@link RealNameBusinessHandler}。
 *
 * Author: Charlie
 */
@Component
public class RealNameBusinessHandlerRegistry {

    private final Map<String, RealNameBusinessHandler> handlers;

    public RealNameBusinessHandlerRegistry(List<RealNameBusinessHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(
                        handler -> handler.businessType().toUpperCase(Locale.ROOT),
                        Function.identity(),
                        (left, right) -> left));
    }

    public RealNameBusinessHandler require(String businessType) {
        if (businessType == null || businessType.isBlank()) {
            throw new BizException("business_type is required");
        }
        RealNameBusinessHandler handler = handlers.get(businessType.trim().toUpperCase(Locale.ROOT));
        if (handler == null) {
            throw new BizException("Unsupported business_type: " + businessType);
        }
        return handler;
    }
}
