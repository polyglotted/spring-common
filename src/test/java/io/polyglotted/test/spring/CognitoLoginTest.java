package io.polyglotted.test.spring;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import io.polyglotted.common.model.MapResult.SimpleMapResult;
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
        assertEntity(execute(url, POST, null, null, SimpleMapResult.class), BAD_REQUEST, "message", "Invalid credentials.");

        url = "/cognito/login?email=" + user1.getEmail() + "&password=";
        assertEntity(execute(url, POST, null, null, SimpleMapResult.class), BAD_REQUEST, "message", "Invalid credentials.");

        url = "/cognito/login?email=" + user1.getEmail() + "&password=fooBarBaz";
        assertEntity(execute(url, POST, null, null, SimpleMapResult.class), UNAUTHORIZED, "message", "Incorrect username or password.");

        url = "/cognito/login?email=foo@fooz.com&password=fooBarBaz";
        assertEntity(execute(url, POST, null, null, SimpleMapResult.class), UNAUTHORIZED, "message", "User does not exist.");
    }

    @Test
    public void authorizationFailure() throws Exception {
        AuthenticationResultType loginResult = loginUser(user2);
        try {
            assertEntity(doGet("/api/sample", loginResult, SimpleMapResult.class), OK, "result", "ok");
            assertEntity(execute("/api/sample-admin", GET, loginResult, null, SimpleMapResult.class),
                FORBIDDEN, "message", "User is not authorised to perform action.");

        } finally { logout(loginResult); }
        assertEntity(execute("/api/sample", GET, loginResult, null, SimpleMapResult.class), UNAUTHORIZED, "message", "Unauthorized");
    }

    @Test
    public void loginLogoutSuccess() throws Exception {
        AuthenticationResultType loginResult = loginUser(user1);
        try {
            assertEntity(doGet("/api/sample-admin", loginResult, SimpleMapResult.class), OK, "result", "admin");
        } finally { logout(loginResult); }
    }
}