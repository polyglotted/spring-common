package io.polyglotted.spring.security;

import io.polyglotted.spring.cognito.CognitoAuthFilter;
import io.polyglotted.spring.cognito.CognitoProcessor;
import io.polyglotted.spring.elastic.ElasticAuthFilter;
import io.polyglotted.spring.elastic.ElasticProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static io.polyglotted.common.util.ListBuilder.immutableList;
import static org.springframework.util.StringUtils.toStringArray;

@EnableWebSecurity @SuppressWarnings("unused")
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class DefaultSecurityConfigurer extends WebSecurityConfigurerAdapter {
    @Autowired private final DefaultAuthProvider defaultAuthProvider = null;
    @Autowired private CognitoProcessor cognitoProcessor = null;
    @Autowired private ElasticProcessor elasticProcessor = null;
    @Autowired private RestAuthEntryPoint restAuthEntryPoint = null;

    @Value("#{'${spring.authorised.endpoints:/api/**}'.split(',')}") private List<String> authorisedEndpoints = immutableList();
    @Value("#{'${spring.unauthorised.endpoints}'.split(',')}") private List<String> unauthorisedEndpoints = immutableList();
    @Value("#{'${spring.cors.headers:Authorization,Cache-Control,Content-Type,DNT,If-Modified-Since,Keep-Alive," +
        "User-Agent,X-Requested-With,X-Proxy-User,X-Session-Token,X-Realm,X-Real-IP,X-Forwarded-For}'.split(',')}")
    private List<String> corsHeaders = immutableList();

    @Override public void configure(AuthenticationManagerBuilder auth) throws Exception { auth.authenticationProvider(defaultAuthProvider); }

    @Override public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        web.httpFirewall(httpFirewall());
    }

    @Override protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.httpBasic()
          .and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
          .and()
            .exceptionHandling()
            .authenticationEntryPoint(restAuthEntryPoint)
          .and()
            .authorizeRequests()
                .antMatchers(toStringArray(unauthorisedEndpoints)).permitAll()
                .antMatchers(toStringArray(authorisedEndpoints)).authenticated()
                .anyRequest().authenticated()
          .and()
            .addFilterBefore(new CognitoAuthFilter(cognitoProcessor), BasicAuthenticationFilter.class)
            .addFilterBefore(new ElasticAuthFilter(elasticProcessor), BasicAuthenticationFilter.class)
            .formLogin();
        // @formatter:on
    }

    @Bean CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(immutableList("*"));
        configuration.setAllowedMethods(immutableList("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));
        configuration.setAllowedHeaders(corsHeaders);
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @SuppressWarnings("WeakerAccess") @Bean public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowUrlEncodedPercent(true);
        firewall.setAllowUrlEncodedPeriod(true);
        return firewall;
    }
}