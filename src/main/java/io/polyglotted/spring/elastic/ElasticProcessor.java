package io.polyglotted.spring.elastic;

import io.polyglotted.common.model.AuthToken;
import io.polyglotted.common.model.Subject;
import io.polyglotted.common.util.HttpConfig;
import io.polyglotted.common.util.ListBuilder;
import io.polyglotted.spring.elastic.ElasticAuthFilter.ElasticClientException;
import io.polyglotted.spring.security.DefaultAuthToken;
import io.polyglotted.spring.web.SimpleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.io.Closeable;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.List;

import static io.polyglotted.common.util.CollUtil.fluent;
import static io.polyglotted.common.util.HttpConfig.httpClient;
import static io.polyglotted.common.util.HttpUtil.buildDelete;
import static io.polyglotted.common.util.HttpUtil.buildGet;
import static io.polyglotted.common.util.HttpUtil.buildPost;
import static io.polyglotted.common.util.HttpUtil.execute;
import static io.polyglotted.common.util.ListBuilder.immutableList;
import static io.polyglotted.common.util.ResourceUtil.urlResource;
import static io.polyglotted.common.util.StrUtil.notNullOrEmpty;
import static io.polyglotted.common.util.StrUtil.safePrefix;
import static io.polyglotted.common.util.ThreadUtil.safeSleep;
import static io.polyglotted.spring.errorhandling.ExceptionFactory.checkBadRequest;
import static io.polyglotted.spring.errorhandling.ExceptionFactory.unauthorisedException;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

@SuppressWarnings("unused") @Slf4j @Component @RequiredArgsConstructor(access = PRIVATE)
public class ElasticProcessor implements Closeable {
    private static final String LOGIN_TEMPL = "{\"grant_type\":\"password\",\"username\":\"$userid\",\"password\":\"$passwd\"}";
    private static final String LOGOUT_TEMPL = "{\"token\":\"$token\"}";
    private final CloseableHttpClient httpClient;
    private final String baseUri;
    private final EsBootstrapAuth auth;

    @Autowired public ElasticProcessor(@Qualifier("idpEsHttpConfig") HttpConfig config, @Qualifier("idpEsUserAuth") EsBootstrapAuth auth) {
        this(createHttpClient(config), config.url(), auth);
    }

    @PreDestroy @Override public void close() throws IOException { httpClient.close(); }

    public AuthToken login(String userId, String password) {
        checkBadRequest(notNullOrEmpty(userId) && notNullOrEmpty(password), "Invalid credentials.");
        try {
            return AuthToken.buildWith(execute(httpClient, buildPost(baseUri, "/_xpack/security/oauth2/token")
                .withBasicAuth(auth.username, auth.password).withJson(LOGIN_TEMPL.replace("$userid", userId).replace("$passwd", password))));
        } catch (Exception ex) {
            ex.printStackTrace();
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
            .add(new SimpleGrantedAuthority("ROLE_CONSUMER"))
            .addAll(fluent(roles).transformAndConcat(ElasticProcessor::transformRole))
            .build();
    }

    private static List<GrantedAuthority> transformRole(String role) {
        return !"superuser".equals(role) ? immutableList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())) : immutableList(
            new SimpleGrantedAuthority("ROLE_CURATOR"),
            new SimpleGrantedAuthority("ROLE_MODELER"),
            new SimpleGrantedAuthority("ROLE_GATE_KEEPER"),
            new SimpleGrantedAuthority("ROLE_ADMINISTRATOR")
        );
    }

    @SuppressWarnings({"ConstantConditions"})
    private static CloseableHttpClient createHttpClient(HttpConfig config) {
        for (int i = 0; i <= 300; i++) {
            try {
                URL trustStore = urlResource(ElasticProcessor.class, "elastic-spring-ca.p12");
                log.debug("connecting to " + config.url()); return httpClient(config.setTrustStore(trustStore.toString()));
            } catch (Exception ioe) {
                if (ioe instanceof ConnectException) { safeSleep(1000); }
                else { throw ioe; }
            }
        }
        throw new IllegalStateException("create elasticProcesor client failed");
    }
}