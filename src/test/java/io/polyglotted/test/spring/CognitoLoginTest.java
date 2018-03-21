package io.polyglotted.test.spring;

import io.polyglotted.common.model.AuthToken;
import io.polyglotted.common.model.MapResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RunWith(SpringJUnit4ClassRunner.class)
public class CognitoLoginTest extends AbstractSpringTest {

    @Test
    public void loginFailure() throws Exception {
        String url = "/cognito/login?email=&password=fooBarBaz";
        assertEntity(execute(url, POST, null, null, MapResult.class), BAD_REQUEST, "message", "Invalid credentials.");

        url = "/cognito/login?email=" + user1.getEmail() + "&password=";
        assertEntity(execute(url, POST, null, null, MapResult.class), BAD_REQUEST, "message", "Invalid credentials.");

        url = "/cognito/login?email=" + user1.getEmail() + "&password=fooBarBaz";
        assertEntity(execute(url, POST, null, null, MapResult.class), UNAUTHORIZED, "message", "Incorrect username or password.");

        url = "/cognito/login?email=foo@fooz.com&password=fooBarBaz";
        assertEntity(execute(url, POST, null, null, MapResult.class), UNAUTHORIZED, "message", "User does not exist.");
    }

    @Test
    public void authorizationFailure() throws Exception {
        AuthToken token = loginUser(user2);
        try {
            assertEntity(doGet("/api/sample", token, MapResult.class), OK, "result", "ok");
            assertEntity(execute("/api/sample-admin", GET, token, null, MapResult.class),
                FORBIDDEN, "message", "User is not authorised to perform action.");
        } finally { logout(token); }
    }

    @Test
    public void loginLogoutSuccess() throws Exception {
        AuthToken token = loginUser(user1);
        try {
            assertEntity(doGet("/api/sample-admin", token, MapResult.class), OK, "result", "admin");
        } finally { logout(token); }
        assertEntity(execute("/api/sample", GET, token, null, MapResult.class), UNAUTHORIZED, "message", "Unauthorized");
    }
}