package io.polyglotted.test.spring;

import io.polyglotted.common.model.AuthToken;
import io.polyglotted.common.model.MapResult;
import io.polyglotted.test.spring.CognitoDemo.IntegrationUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RunWith(SpringJUnit4ClassRunner.class)
public class ElasticLoginTest extends AbstractSpringTest {

    @Test
    public void loginLogoutSuccess() throws Exception {
        IntegrationUser user = new IntegrationUser("elastic", "SteelEye");
        AuthToken token = elasticLogin(user);
        try {
            assertEntity(doGet("/api/sample", token, MapResult.class), OK, "result", "ok");
        } finally { elasticLogout(token); }

        assertEntity(execute("/api/sample-admin", GET, user.basicHeader(), null, MapResult.class), OK, "result", "admin");
        assertEntity(execute("/api/sample", GET, token, null, MapResult.class), UNAUTHORIZED, "message", "Unauthorized");
    }

    private AuthToken elasticLogin(IntegrationUser user) {
        String url = "/elastic/login?userId=" + user.getEmail() + "&password=" + user.getPassword();
        AuthToken authToken = AuthToken.buildWith(doPost(url, null, null, MapResult.class).getBody());
        assertThat(requireNonNull(authToken).accessToken, is(notNullValue()));
        return authToken;
    }

    private void elasticLogout(AuthToken token) { assertEntity(doGet("/elastic/logout", token, MapResult.class), OK, "result", "logged-out"); }
}