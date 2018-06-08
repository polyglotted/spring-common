package io.polyglotted.spring.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component @Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleCorsFilter extends GenericFilterBean {
    @Value("${spring.cors.headers:Authorization, Cache-Control, Content-Type, DNT, If-Modified-Since, Keep-Alive, " +
        "User-Agent, X-Requested-With, X-Proxy-User, X-Session-Token, X-Realm, X-Real-IP, X-Forwarded-For}")
    private String corsHeaders = null;

    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpRes = (HttpServletResponse) response;
        httpRes.setHeader("Access-Control-Allow-Origin", "*");
        httpRes.setHeader("Access-Control-Allow-Methods", "DELETE, GET, HEAD, OPTIONS, POST, PUT");
        httpRes.setHeader("Access-Control-Allow-Headers", corsHeaders);
        httpRes.setHeader("Access-Control-Allow-Credentials", "true");
        httpRes.setHeader("Access-Control-Max-Age", "3600");
        chain.doFilter(request, response);
    }
}