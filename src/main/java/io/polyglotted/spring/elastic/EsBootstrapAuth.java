package io.polyglotted.spring.elastic;

import io.polyglotted.common.model.AuthHeader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static io.polyglotted.common.model.AuthHeader.basicAuth;
import static io.polyglotted.common.util.StrUtil.notNullOrEmpty;

@NoArgsConstructor @AllArgsConstructor @Accessors(chain = true) @Getter @Setter
public class EsBootstrapAuth {
    String username = "elastic";
    String password = null;

    AuthHeader userAuth() { return notNullOrEmpty(username) && notNullOrEmpty(password) ? basicAuth(username, password) : null; }
}