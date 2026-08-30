package github.jiangbyte.io.common.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 请求参数过滤器：将 snake_case 查询/表单参数映射为 camelCase 供绑定。
 *
 * Author: Charlie
 */
public class SnakeCaseRequestParameterFilter extends OncePerRequestFilter {

    /** 将 snake_case 请求参数映射为 camelCase。 */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(new SnakeCaseParameterRequestWrapper(request), response);
    }

    static final class SnakeCaseParameterRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String[]> params;

        SnakeCaseParameterRequestWrapper(HttpServletRequest request) {
            super(request);
            Map<String, String[]> original = request.getParameterMap();
            Map<String, String[]> merged = new LinkedHashMap<>(original.size() * 2);
            for (Map.Entry<String, String[]> entry : original.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();
                merged.put(key, values);
                String camel = snakeToCamel(key);
                if (!camel.equals(key)) {
                    merged.putIfAbsent(camel, values);
                }
            }
            this.params = Collections.unmodifiableMap(merged);
        }

        @Override
        public String getParameter(String name) {
            String[] values = params.get(name);
            return values == null || values.length == 0 ? null : values[0];
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return params;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(params.keySet());
        }

        @Override
        public String[] getParameterValues(String name) {
            return params.get(name);
        }

        static String snakeToCamel(String name) {
            if (name == null || name.isEmpty() || name.indexOf('_') < 0) {
                return name;
            }
            StringBuilder sb = new StringBuilder(name.length());
            boolean upper = false;
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                if (c == '_') {
                    upper = true;
                    continue;
                }
                if (upper) {
                    sb.append(Character.toUpperCase(c));
                    upper = false;
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }
}
