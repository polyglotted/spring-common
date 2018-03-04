package io.polyglotted.spring.security;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.polyglotted.common.model.MapResult;
import io.polyglotted.common.util.ConversionUtil;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static com.google.common.collect.Lists.transform;
import static io.polyglotted.common.util.MapRetriever.STRING_LIST_CLASS;
import static io.polyglotted.common.util.MapRetriever.removeIfExists;

@Builder(builderClassName = "Builder")
@RequiredArgsConstructor
public final class Principal {
    @NonNull public final String userId;
    public final String name;
    public final String mobileNumber;
    public final String countryCode;
    public final String messenger;
    public final boolean activated;
    public final String mfaStatus;
    public final String mfaProvider;
    public final String mfaId;
    public final String email;
    public final String jobTitle;
    public final String timeZone;
    public final String language;
    public final LocalDateTime registered;
    public final Long issuedAt;
    public final Long expiry;
    public final ImmutableList<String> groups;
    public final ImmutableList<String> roles;
    public final ImmutableList<String> permissions;
    public final ImmutableMap<String, Object> tags;

    public List<GrantedAuthority> authorities() { return transform(roles, role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())); }

    public static class Builder {
        public Builder fillFrom(MapResult map) {
            return name(removeIfExists(map, "name")).mobileNumber(removeIfExists(map, "mobileNumber"))
                .countryCode(removeIfExists(map, "countryCode")).messenger(removeIfExists(map, "messenger"))
                .activated(removeIfExists(map, "activated", false)).mfaStatus(removeIfExists(map, "mfaStatus"))
                .mfaProvider(removeIfExists(map, "mfaProvider")).mfaId(removeIfExists(map, "mfaId")).email(removeIfExists(map, "email"))
                .jobTitle(removeIfExists(map, "jobTitle")).timeZone(removeIfExists(map, "timeZone")).language(removeIfExists(map, "language"))
                .registered(nullOrVal(map, "registered", Object.class, ConversionUtil::asLocalDateTime))
                .permissions(nullOrVal(map, "permissions", STRING_LIST_CLASS, ImmutableList::copyOf)).tags(ImmutableMap.copyOf(map));
        }

        private static <T, R> T nullOrVal(MapResult map, String prop, Class<R> clazz, Function<R, T> function) {
            return map.containsKey(prop) ? function.apply(clazz.cast(removeIfExists(map, prop))) : null;
        }
    }
}