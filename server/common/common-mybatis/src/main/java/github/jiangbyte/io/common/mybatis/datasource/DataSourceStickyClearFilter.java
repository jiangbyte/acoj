package github.jiangbyte.io.common.mybatis.datasource;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

/**
 * 请求结束清理写后粘主 ThreadLocal。
 *
 * Author: Charlie
 */
public class DataSourceStickyClearFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            DataSourceSticky.clear();
        }
    }
}
