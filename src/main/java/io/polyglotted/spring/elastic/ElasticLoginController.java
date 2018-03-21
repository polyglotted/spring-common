package io.polyglotted.spring.elastic;

import io.polyglotted.common.model.AuthToken;
import io.polyglotted.spring.elastic.ElasticAuthFilter.ElasticClientException;
import io.polyglotted.spring.web.SimpleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static io.polyglotted.common.util.StrUtil.notNullOrEmpty;
import static io.polyglotted.common.util.StrUtil.safePrefix;
import static io.polyglotted.spring.errorhandling.ExceptionFactory.checkBadRequest;
import static io.polyglotted.spring.errorhandling.ExceptionFactory.unauthorisedException;

@SuppressWarnings({"unused", "WeakerAccess"}) @Slf4j @RestController
public class ElasticLoginController {
    @Autowired private ElasticProcessor elasticProcessor = null;

    @PostMapping(path = "/elastic/login", params = {"userId", "password"}, produces = "application/json")
    public AuthToken login(String userId, String password) throws IOException {
        checkBadRequest(notNullOrEmpty(userId) && notNullOrEmpty(password), "Invalid credentials.");
        try {
            return elasticProcessor.login(userId, password);
        } catch (ElasticClientException ex) {
            log.debug("not found or invalid creds: {}", userId); throw unauthorisedException(safePrefix(ex.getMessage(), " ("));
        }
    }

    @PostMapping(path = "/elastic/logout", produces = "application/json")
    public SimpleResponse logout(@RequestBody AuthToken result) throws IOException {
        elasticProcessor.logout(result.accessToken);
        return new SimpleResponse("logged-out");
    }
}