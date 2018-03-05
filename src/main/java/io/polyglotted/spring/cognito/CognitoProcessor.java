package io.polyglotted.spring.cognito;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.GetUserRequest;
import com.amazonaws.services.cognitoidp.model.GetUserResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.polyglotted.common.model.MapResult;
import io.polyglotted.common.model.MapResult.SimpleMapResult;
import io.polyglotted.spring.security.DefaultAuthToken;
import io.polyglotted.spring.security.Principal;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.google.common.collect.Lists.transform;
import static io.polyglotted.common.model.MapResult.simpleResult;
import static io.polyglotted.common.util.MapRetriever.listVal;
import static io.polyglotted.common.util.MapRetriever.removeVal;
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
            Principal principal = enhance(getUser(bearerToken), bearerToken).build();
            return new DefaultAuthToken(principal, bearerToken, principal.authorities());
        }
        log.trace("No Bearer token found in HTTP Authorization header");
        return null;
    }

    private Principal.Builder getUser(String accessToken) {
        GetUserResult user = cognitoClient.getUser(new GetUserRequest().withAccessToken(accessToken));

        MapResult attributeMap = simpleResult();
        for (AttributeType type : user.getUserAttributes()) { attributeMap.put(type.getName().toLowerCase(ENGLISH), type.getValue()); }
        return Principal.builder().userId(removeVal(attributeMap, "sub")).fillFrom(attributeMap);
    }

    private Principal.Builder enhance(Principal.Builder builder, String token) {
        MapResult jwt = parseJwt(token);
        return builder.expiry(asTime(jwt, "exp")).issuedAt(asTime(jwt, "iat")).roles(ImmutableList.copyOf(roles(jwt)));
    }

    @SneakyThrows private MapResult parseJwt(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) { throw new IllegalArgumentException("invalid token parts"); }
        return objectMapper.readValue(decodeBase64(parts[1]), SimpleMapResult.class);
    }

    private static List<String> roles(MapResult map) { return transform(listVal(map, "cognito:groups"), CognitoProcessor::groupToRole); }

    private static String groupToRole(String group) { return group.startsWith("ABACI_") ? group.substring(6) : group; }

    private static Long asTime(MapResult map, String prop) {
        Integer integer = (Integer) map.get(prop); return integer == null ? null : integer.longValue() * 1000;
    }
}