package io.polyglotted.spring.cognito;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.GetUserRequest;
import com.amazonaws.services.cognitoidp.model.GetUserResult;
import com.amazonaws.services.cognitoidp.model.GlobalSignOutRequest;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.polyglotted.common.model.AuthToken;
import io.polyglotted.common.model.MapResult;
import io.polyglotted.common.model.Subject;
import io.polyglotted.spring.cognito.CognitoAuthFilter.NotCognitoException;
import io.polyglotted.spring.security.DefaultAuthToken;
import io.polyglotted.spring.web.SimpleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static io.polyglotted.common.model.MapResult.simpleResult;
import static io.polyglotted.common.model.Subject.subjectBuilder;
import static io.polyglotted.common.util.BaseSerializer.deserialize;
import static io.polyglotted.common.util.CollUtil.transformList;
import static io.polyglotted.common.util.MapBuilder.simpleMap;
import static io.polyglotted.common.util.MapRetriever.listVal;
import static io.polyglotted.common.util.StrUtil.notNullOrEmpty;
import static io.polyglotted.common.util.StrUtil.nullOrEmpty;
import static io.polyglotted.common.util.StrUtil.safePrefix;
import static io.polyglotted.spring.errorhandling.ExceptionFactory.checkBadRequest;
import static io.polyglotted.spring.errorhandling.ExceptionFactory.unauthorisedException;
import static java.util.Locale.ENGLISH;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Slf4j @Component @RequiredArgsConstructor
public class CognitoProcessor {
    private final CognitoConfig config;
    private final AWSCognitoIdentityProvider cognitoClient;
    @Autowired private ObjectMapper objectMapper = null;

    public AuthenticationResultType login(String email, String password) {
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

    public SimpleResponse logout(AuthToken token) {
        try {
            cognitoClient.globalSignOut(new GlobalSignOutRequest().withAccessToken(token.accessToken));
        } catch (Exception ex) { log.warn("failed to logout " + token.accessToken); }
        return new SimpleResponse("logged-out");
    }

    DefaultAuthToken authenticate(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION);
        if (header != null && header.startsWith("Bearer")) {
            String bearerToken = header.substring(7);
            List<String> roles = fetchRoles(bearerToken);
            return new DefaultAuthToken(getUser(bearerToken).roles(roles).build(), bearerToken, authorities(roles));
        }
        log.trace("No Bearer token found in HTTP Authorization header");
        return null;
    }

    private Subject.Builder getUser(String accessToken) {
        GetUserResult user = cognitoClient.getUser(new GetUserRequest().withAccessToken(accessToken));
        MapResult attributes = simpleResult();
        for (AttributeType type : user.getUserAttributes()) { attributes.put(type.getName().toLowerCase(ENGLISH), type.getValue()); }
        return subjectBuilder().username(attributes.removeVal("sub")).email(attributes.optStr("email"))
            .fullName(attributes.optStr("name")).metadata(attributes);
    }

    private List<String> fetchRoles(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) { throw new NotCognitoException("invalid token parts"); }
        return rolesFrom(deserialize(objectMapper, decodeBase64(parts[1])));
    }

    private static List<String> rolesFrom(MapResult map) { return transformList(listVal(map, "cognito:groups"), CognitoProcessor::groupToRole); }

    private static String groupToRole(String group) { return group.startsWith("ABACI_") ? group.substring(6) : group; }

    private static List<GrantedAuthority> authorities(List<String> roles) {
        return transformList(roles, role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }
}