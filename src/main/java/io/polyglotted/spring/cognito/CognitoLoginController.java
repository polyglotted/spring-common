package io.polyglotted.spring.cognito;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.GlobalSignOutRequest;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import io.polyglotted.common.model.AuthToken;
import io.polyglotted.spring.web.SimpleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static io.polyglotted.common.util.MapBuilder.simpleMap;
import static io.polyglotted.common.util.StrUtil.notNullOrEmpty;
import static io.polyglotted.common.util.StrUtil.nullOrEmpty;
import static io.polyglotted.common.util.StrUtil.safePrefix;
import static io.polyglotted.spring.errorhandling.ExceptionFactory.checkBadRequest;
import static io.polyglotted.spring.errorhandling.ExceptionFactory.unauthorisedException;

@SuppressWarnings({"unused", "WeakerAccess"}) @Slf4j @RestController
public class CognitoLoginController extends AbstractCognito {

    @Autowired public CognitoLoginController(CognitoConfig config, AWSCognitoIdentityProvider cognitoClient) { super(config, cognitoClient); }

    @PostMapping(path = "/cognito/login", params = {"email", "password"}, produces = "application/json")
    public AuthenticationResultType login(String email, String password) throws IOException {
        checkBadRequest(notNullOrEmpty(email) && notNullOrEmpty(password), "Invalid credentials.");
        try {
            AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH).withAuthParameters(simpleMap("USERNAME", email, "PASSWORD", password,
                    "SECRET_HASH", config.getClientSecret())).withClientId(config.getClientId()).withUserPoolId(config.getUserPoolId());
            AdminInitiateAuthResult authResponse = cognitoClient.adminInitiateAuth(authRequest);

            if (nullOrEmpty(authResponse.getChallengeName())) { return authResponse.getAuthenticationResult(); }
            throw unauthorisedException("Unexpected challenge on signin: " + authResponse.getChallengeName() + ".");

        } catch (UserNotFoundException | NotAuthorizedException ex) {
            log.debug("not found or invalid creds: {}", email); throw unauthorisedException(safePrefix(ex.getMessage(), " ("));
        }
    }

    @PostMapping(path = "/cognito/logout", produces = "application/json")
    public SimpleResponse logout(@RequestBody AuthToken result) throws IOException {
        cognitoClient.globalSignOut(new GlobalSignOutRequest().withAccessToken(result.accessToken));
        return new SimpleResponse("logged-out");
    }
}