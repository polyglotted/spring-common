package io.polyglotted.spring.cognito;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.GlobalSignOutRequest;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.TooManyRequestsException;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static io.polyglotted.common.util.MapBuilder.immutableMap;
import static io.polyglotted.common.util.MapBuilder.simpleMap;
import static io.polyglotted.common.util.StrUtil.notNullOrEmpty;
import static io.polyglotted.common.util.ThreadUtil.safeSleep;
import static io.polyglotted.spring.errorhandling.ExceptionFactory.checkBadRequest;
import static io.polyglotted.spring.errorhandling.ExceptionFactory.unauthorisedException;

@Slf4j @Controller
@SuppressWarnings({"unused", "WeakerAccess"})
public class CognitoLoginController extends AbstractCognito {

    @Autowired public CognitoLoginController(CognitoConfig config, AWSCognitoIdentityProvider cognitoClient) { super(config, cognitoClient); }

    @PostMapping(path = "/cognito/login", params = {"email", "password"}, produces = "application/json")
    @ResponseBody public AuthenticationResultType login(String email, String password) throws IOException {
        System.out.println(">>>> COGNITO LOGIN");
        checkBadRequest(notNullOrEmpty(email) && notNullOrEmpty(password), "invalid creds");
        try {
            AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH).withAuthParameters(simpleMap("USERNAME", email, "PASSWORD", password,
                    "SECRET_HASH", config.getClientSecret())).withClientId(config.getClientId()).withUserPoolId(config.getUserPoolId());
            AdminInitiateAuthResult authResponse = cognitoClient.adminInitiateAuth(authRequest);

            if (isNullOrEmpty(authResponse.getChallengeName())) { return authResponse.getAuthenticationResult(); }
            throw unauthorisedException("unexpected challenge on signin: " + authResponse.getChallengeName());

        } catch (UserNotFoundException | NotAuthorizedException ex) {
            log.debug("not found or invalid creds: {}", email); throw unauthorisedException(ex.getMessage());

        } catch (TooManyRequestsException ex) {
            log.warn("caught TooManyRequestsException, delaying then retrying"); safeSleep(250); return login(email, password);
        }
    }

    @PostMapping(path = "/cognito/logout", produces = "application/json")
    @ResponseBody public Map<String, Object> logout(@RequestBody AuthenticationResultType result) throws IOException {
        checkBadRequest(result != null && notNullOrEmpty(result.getAccessToken()), "invalid accessToken");
        cognitoClient.globalSignOut(new GlobalSignOutRequest().withAccessToken(result.getAccessToken()));
        return immutableMap("result", "logged-out");
    }
}