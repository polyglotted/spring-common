package io.polyglotted.test.spring;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import io.polyglotted.common.model.MapResult.SimpleMapResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RunWith(SpringJUnit4ClassRunner.class)
public class CognitoLoginTest extends AbstractSpringTest {

    @Test
    public void loginFailure() throws Exception {
        String url = "/cognito/login?email=&password=fooBarBaz";
        ResponseEntity<SimpleMapResult> responseEntity = restTemplate.postForEntity(url, "{}", SimpleMapResult.class);
        assertEntity(responseEntity, BAD_REQUEST, "message", "Invalid credentials.");

        url = "/cognito/login?email=" + user1.getEmail() + "&password=";
        responseEntity = restTemplate.postForEntity(url, "{}", SimpleMapResult.class);
        assertEntity(responseEntity, BAD_REQUEST, "message", "Invalid credentials.");

        url = "/cognito/login?email=" + user1.getEmail() + "&password=fooBarBaz";
        responseEntity = restTemplate.postForEntity(url, "{}", SimpleMapResult.class);
        assertEntity(responseEntity, UNAUTHORIZED, "message", "Incorrect username or password.");

        url = "/cognito/login?email=foo@fooz.com&password=fooBarBaz";
        responseEntity = restTemplate.postForEntity(url, "{}", SimpleMapResult.class);
        assertEntity(responseEntity, UNAUTHORIZED, "message", "User does not exist.");
    }

    @Test
    public void authorizationFailure() throws Exception {
        AuthenticationResultType loginResult = loginUser(restTemplate, user2);

        ResponseEntity<SimpleMapResult> responseEntity = restTemplate.exchange("/api/sample", GET,
            buildRequest(null, loginResult.getAccessToken()), SimpleMapResult.class);
        assertEntity(responseEntity, OK, "result", "ok");

        responseEntity = restTemplate.exchange("/api/sample-admin", GET,
            buildRequest(null, loginResult.getAccessToken()), SimpleMapResult.class);
        assertEntity(responseEntity, FORBIDDEN, "message", "User is not authorised to perform action.");

        logout(restTemplate, loginResult);

        responseEntity = restTemplate.exchange("/api/sample", GET,
            buildRequest(null, loginResult.getAccessToken()), SimpleMapResult.class);
        assertEntity(responseEntity, UNAUTHORIZED, "message", "Unauthorized");
    }

    @Test
    public void loginLogoutSuccess() throws Exception {
        AuthenticationResultType loginResult = loginUser(restTemplate, user1);
        try {
            ResponseEntity<SimpleMapResult> responseEntity = restTemplate.exchange("/api/sample-admin", GET,
                buildRequest(null, loginResult.getAccessToken()), SimpleMapResult.class);
            assertEntity(responseEntity, OK, "result", "admin");

        } finally { logout(restTemplate, loginResult); }
    }
}