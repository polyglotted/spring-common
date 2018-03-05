package io.polyglotted.spring.security;

import io.polyglotted.spring.cognito.CognitoAuthFilter;
import io.polyglotted.spring.cognito.CognitoProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static io.polyglotted.common.util.ListBuilder.immutableList;

@EnableWebSecurity @SuppressWarnings("unused")
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class DefaultSecurityConfigurer extends WebSecurityConfigurerAdapter {
    @Autowired private final DefaultAuthProvider defaultAuthProvider = null;
    @Autowired private CognitoProcessor cognitoProcessor = null;
    @Autowired private RestAuthEntryPoint restAuthEntryPoint = null;

    @Override public void configure(AuthenticationManagerBuilder auth) throws Exception { auth.authenticationProvider(defaultAuthProvider); }

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
                .antMatchers("/").permitAll()
                .antMatchers("/cognito/login").permitAll()
                .antMatchers("/cognito/logout").permitAll()
                .antMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
          .and()
            .addFilterBefore(new CognitoAuthFilter(cognitoProcessor), BasicAuthenticationFilter.class)
            .formLogin();
        // @formatter:on
    }

    @Bean CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(immutableList("*"));
        configuration.setAllowedMethods(immutableList("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));
        configuration.setAllowedHeaders(immutableList("Authorization", "Cache-Control", "Content-Type",
            "DNT", "If-Modified-Since", "Keep-Alive", "User-Agent", "X-Requested-With", "X-Proxy-User",
            "X-Session-Token", "X-Realm", "X-Real-IP", "X-Forwarded-For"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}