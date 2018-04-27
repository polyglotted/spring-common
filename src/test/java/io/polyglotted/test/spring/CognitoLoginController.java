package io.polyglotted.test.spring;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import io.polyglotted.common.model.AuthToken;
import io.polyglotted.spring.cognito.CognitoProcessor;
import io.polyglotted.spring.web.SimpleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@SuppressWarnings({"unused", "WeakerAccess"}) @Slf4j @RestController
public class CognitoLoginController {
    @Autowired private CognitoProcessor cognitoProcessor = null;

    @PostMapping(path = "/cognito/login", params = {"email", "password"}, produces = "application/json")
    public AuthenticationResultType login(String email, String password) throws IOException { return cognitoProcessor.login(email, password); }

    @PostMapping(path = "/cognito/logout", produces = "application/json")
    public SimpleResponse logout(@RequestBody AuthToken result) throws IOException { return cognitoProcessor.logout(result); }
}