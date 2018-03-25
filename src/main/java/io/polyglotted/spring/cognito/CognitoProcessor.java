package io.polyglotted.spring.cognito;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.GetUserRequest;
import com.amazonaws.services.cognitoidp.model.GetUserResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.polyglotted.common.model.MapResult;
import io.polyglotted.common.model.Subject;
import io.polyglotted.spring.cognito.CognitoAuthFilter.NotCognitoException;
import io.polyglotted.spring.security.DefaultAuthToken;
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
import static io.polyglotted.common.util.MapRetriever.listVal;
import static java.util.Locale.ENGLISH;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Slf4j @Component
public class CognitoProcessor extends AbstractCognito {
    @Autowired private ObjectMapper objectMapper = null;

    @Autowired public CognitoProcessor(CognitoConfig config, AWSCognitoIdentityProvider cognitoClient) { super(config, cognitoClient); }

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