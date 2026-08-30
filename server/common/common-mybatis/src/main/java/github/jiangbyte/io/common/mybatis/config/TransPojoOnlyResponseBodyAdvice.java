package github.jiangbyte.io.common.mybatis.config;

import github.jiangbyte.io.common.core.domain.ApiResponse;
import org.dromara.core.trans.vo.VO;
import org.dromara.trans.service.impl.TransService;
import org.dromara.trans.utils.TransUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 响应体 Advice：仅对实现 TransPojo 的返回值执行字典/关联翻译。
 *
 * Author: Charlie
 */
@RestControllerAdvice
@ConditionalOnProperty(name = "easy-trans.is-enable-global", havingValue = "true")
public class TransPojoOnlyResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger log = LoggerFactory.getLogger(TransPojoOnlyResponseBodyAdvice.class);

    @Value("${easy-trans.is-enable-tile:false}")
    private Boolean isEnableTile;

    @Autowired
    private TransService transService;

    /** 判断是否对本返回类型启用翻译。 */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /** 写出前对 TransPojo 响应执行翻译。 */
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body == null) {
            return null;
        }
        try {
            if (body instanceof ApiResponse<?> apiResponse) {
                Object data = apiResponse.getData();
                Object translated = translateRoot(data);
                if (translated != data) {
                    @SuppressWarnings({"rawtypes", "unchecked"})
                    ApiResponse raw = apiResponse;
                    raw.setData(translated);
                }
                return apiResponse;
            }
            return translateRoot(body);
        } catch (Exception ex) {
            log.error("翻译错误", ex);
            return body;
        }
    }

    private Object translateRoot(Object root) throws Exception {
        if (root == null) {
            return null;
        }
        if (root instanceof VO) {
            return TransUtil.transOne(
                    root, transService, Boolean.TRUE.equals(isEnableTile), new ArrayList<>(), null, null);
        }
        if (root instanceof Collection<?> collection && hasVoElement(collection)) {
            return TransUtil.transOne(
                    root, transService, Boolean.TRUE.equals(isEnableTile), new ArrayList<>(), null, null);
        }
        return root;
    }

    private static boolean hasVoElement(Collection<?> collection) {
        for (Object item : collection) {
            if (item instanceof VO) {
                return true;
            }
        }
        return false;
    }
}
