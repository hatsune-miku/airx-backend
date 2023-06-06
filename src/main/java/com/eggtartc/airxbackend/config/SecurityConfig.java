package com.eggtartc.airxbackend.config;

import com.eggtartc.airxbackend.security.AirXJwtDecoder;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Logger;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("classpath:public.crt")
    RSAPublicKey jwtPublicKey;

    @Value("classpath:private.key")
    RSAPrivateKey jwtPrivateKey;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        Logger.getLogger("SecurityConfig")
            .info("Security config activated.");

        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.cors(AbstractHttpConfigurer::disable);
        httpSecurity.oauth2ResourceServer(cfg -> {
            cfg.jwt(jwtCfg -> {});
        });
        httpSecurity.sessionManagement(cfg -> {
            cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        });
        httpSecurity.exceptionHandling(cfg -> {
            cfg.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint());
            cfg.accessDeniedHandler(new BearerTokenAccessDeniedHandler());
        });

        httpSecurity.authorizeHttpRequests(cfg -> {
            cfg.requestMatchers("/auth/token").permitAll();
            cfg.requestMatchers("/").permitAll();
            cfg.requestMatchers("/debug/**").permitAll();
            cfg.anyRequest().authenticated();
        });

        return httpSecurity.build();
    }

    @Bean
    CorsConfigurationSource corsDisabler() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.applyPermitDefaultValues();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOrigin("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return new AirXJwtDecoder(
            NimbusJwtDecoder.withPublicKey(jwtPublicKey).build()
        );
    }

    @Bean
    JwtEncoder jwtEncoder() {
        JWK whatIsAJWK = new RSAKey.Builder(jwtPublicKey)
            .privateKey(jwtPrivateKey)
            .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(whatIsAJWK)));
    }
}
