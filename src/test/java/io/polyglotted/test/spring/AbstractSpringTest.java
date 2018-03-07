package io.polyglotted.test.spring;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import io.polyglotted.common.model.MapResult.SimpleMapResult;
import io.polyglotted.test.spring.CognitoDemo.IntegrationUser;
import junit.framework.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static io.polyglotted.common.util.ListBuilder.immutableList;
import static io.polyglotted.common.util.MapRetriever.reqdStr;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = CognitoDemo.class)
abstract class AbstractSpringTest {
    @Autowired @Qualifier("integrationUser1") IntegrationUser user1 = null;
    @Autowired @Qualifier("integrationUser2") IntegrationUser user2 = null;
    @Autowired TestRestTemplate restTemplate = null;

    AuthenticationResultType loginUser(IntegrationUser user) {
        String url = "/cognito/login?email=" + user.getEmail() + "&password=" + user.getPassword();
        AuthenticationResultType loginResult = doPost(url, null, null, AuthenticationResultType.class).getBody();
        assertThat(requireNonNull(loginResult).getAccessToken(), is(notNullValue()));
        return loginResult;
    }

    void logout(AuthenticationResultType loginResult) {
        assertEntity(doPost("/cognito/logout", null, loginResult, SimpleMapResult.class), OK, "result", "logged-out");
    }

    <T> ResponseEntity<T> doGet(String url, AuthenticationResultType key, Class<T> clazz) {
        return checkSuccess(execute(url, GET, key, null, clazz));
    }

    <T> ResponseEntity<T> doPost(String url, AuthenticationResultType key, Object body, Class<T> clazz) {
        return checkSuccess(execute(url, POST, key, body, clazz));
    }

    void doDelete(String url, AuthenticationResultType key) { checkSuccess(execute(url, DELETE, key, null, SimpleMapResult.class)); }

    <T> ResponseEntity<T> execute(String url, HttpMethod method, AuthenticationResultType key, Object body, Class<T> clazz) {
        HttpHeaders headers = new HttpHeaders(); headers.setAccept(immutableList(MediaType.APPLICATION_JSON));
        if (key != null) { headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + key.getAccessToken()); }
        return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), clazz);
    }

    private static <T> ResponseEntity<T> checkSuccess(ResponseEntity<T> responseEntity) {
        if (responseEntity.getStatusCodeValue() != OK.value()) {
            throw new AssertionFailedError(responseEntity.getStatusCode().getReasonPhrase() + ":" + String.valueOf(responseEntity.getBody()));
        }
        return responseEntity;
    }

    static void assertEntity(ResponseEntity<SimpleMapResult> responseEntity, HttpStatus status, String prop, String message) {
        assertThat(responseEntity.getStatusCodeValue(), is(status.value()));
        SimpleMapResult result = responseEntity.getBody();
        assertThat(result, is(notNullValue()));
        assertThat(reqdStr(requireNonNull(result), prop), is(message));
    }
}