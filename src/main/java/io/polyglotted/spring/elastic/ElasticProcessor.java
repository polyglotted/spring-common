package io.polyglotted.spring.elastic;

import io.polyglotted.common.model.AuthToken;
import io.polyglotted.common.model.Subject;
import io.polyglotted.common.util.ListBuilder;
import io.polyglotted.spring.elastic.ElasticAuthFilter.ElasticClientException;
import io.polyglotted.spring.security.DefaultAuthToken;
import io.polyglotted.spring.web.SimpleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import static io.polyglotted.common.util.CollUtil.transformList;
import static io.polyglotted.common.util.HttpUtil.buildDelete;
import static io.polyglotted.common.util.HttpUtil.buildGet;
import static io.polyglotted.common.util.HttpUtil.buildPost;
import static io.polyglotted.common.util.HttpUtil.execute;
import static io.polyglotted.common.util.StrUtil.notNullOrEmpty;
import static io.polyglotted.common.util.StrUtil.safePrefix;
import static io.polyglotted.spring.errorhandling.ExceptionFactory.checkBadRequest;
import static io.polyglotted.spring.errorhandling.ExceptionFactory.unauthorisedException;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Slf4j @RequiredArgsConstructor @Component
public class ElasticProcessor implements Closeable {
    private static final String LOGIN_TEMPL = "{\"grant_type\":\"password\",\"username\":\"$userid\",\"password\":\"$passwd\"}";
    private static final String LOGOUT_TEMPL = "{\"token\":\"$token\"}";
    private final CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom()
        .setConnectTimeout(3000).setSocketTimeout(3000).build()).build();
    @Value("${idp.elastic.url:http://localhost:9200}") private String baseUri = null;

    @PreDestroy @Override public void close() throws IOException { httpClient.close(); }

    public AuthToken login(String userId, String password) {
        checkBadRequest(notNullOrEmpty(userId) && notNullOrEmpty(password), "Invalid credentials.");
        try {
            return AuthToken.buildWith(execute(httpClient, buildPost(baseUri, "/_xpack/security/oauth2/token").withBasicAuth(userId, password)
                .withJson(LOGIN_TEMPL.replace("$userid", userId).replace("$passwd", password))));
        } catch (Exception ex) {
            log.debug("not found or invalid creds: {}", userId); throw unauthorisedException(safePrefix(ex.getMessage(), " ("));
        }
    }

    public SimpleResponse logout(String token) {
        try {
            execute(httpClient, buildDelete(baseUri, "/_xpack/security/oauth2/token").withBearerAuth(token)
                .withJson(LOGOUT_TEMPL.replace("$token", token)));
        } catch (Exception ex) { log.warn("failed to logout " + token); }
        return new SimpleResponse("logged-out");
    }

    DefaultAuthToken authenticate(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION);
        if (header != null) {
            if (header.startsWith("Basic") || (header.startsWith("Bearer") && !header.contains("."))) {
                Subject subject = authenticateInternal(header);
                return new DefaultAuthToken(subject, header, authorities(subject.roles));
            }
            log.trace("no Bearer token or Basic Auth found in AUTHORISATION header");
        }
        log.trace("AUTHORISATION header not found");
        return null;
    }

    private Subject authenticateInternal(String authHeader) {
        try {
            return Subject.buildWith(execute(httpClient, buildGet(baseUri, "/_xpack/security/_authenticate").withAuth(authHeader)));
        } catch (Exception ex) { throw new ElasticClientException("failed to authenticate"); }
    }

    private static List<GrantedAuthority> authorities(List<String> roles) {
        return ListBuilder.<GrantedAuthority>immutableListBuilder()
            .add(new SimpleGrantedAuthority("ROLE_CONSUMER")).add(new SimpleGrantedAuthority("ROLE_CURATOR"))
            .addAll(transformList(roles, role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))).build();
    }
}