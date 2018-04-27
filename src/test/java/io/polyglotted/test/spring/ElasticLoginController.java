package io.polyglotted.test.spring;

import io.polyglotted.common.model.AuthToken;
import io.polyglotted.spring.elastic.ElasticProcessor;
import io.polyglotted.spring.web.SimpleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@SuppressWarnings({"unused", "WeakerAccess"}) @Slf4j @RestController
public class ElasticLoginController {
    @Autowired private ElasticProcessor elasticProcessor = null;

    @PostMapping(path = "/elastic/login", params = {"userId", "password"}, produces = "application/json")
    public AuthToken login(String userId, String password) throws IOException { return elasticProcessor.login(userId, password); }

    @PostMapping(path = "/elastic/logout", produces = "application/json")
    public SimpleResponse logout(@RequestBody AuthToken result) throws IOException { return elasticProcessor.logout(result.accessToken); }
}