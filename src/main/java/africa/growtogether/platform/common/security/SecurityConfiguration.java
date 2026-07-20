package africa.growtogether.platform.common.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({JwtProperties.class, africa.growtogether.platform.eiam.auth.AuthProperties.class, africa.growtogether.platform.eiam.recovery.RecoveryProperties.class, africa.growtogether.platform.eiam.mfa.MfaProperties.class})
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
        TenantBoundaryFilter tenantBoundaryFilter, SecurityErrorWriter errors) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**", "/api/v1/system/status", "/v3/api-docs/**", "/swagger-ui/**", "/api/v1/eiam/auth/login", "/api/v1/eiam/auth/mfa", "/api/v1/eiam/auth/refresh", "/api/v1/eiam/auth/logout", "/api/v1/eiam/auth/password-reset/**", "/api/v1/eiam/auth/account-recovery/**", "/api/v1/eiam/email-verification/**", "/api/v1/eiam/invitations/accept").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, exception) -> errors.write(response, 401,
                    "GT-AUTH-001", "Authentication is required."))
                .accessDeniedHandler((request, response, exception) -> errors.write(response, 403,
                    "GT-AUTH-003", "Access is denied.")))
            .headers(headers -> headers
                .contentTypeOptions(options -> {})
                .frameOptions(frame -> frame.deny()))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(tenantBoundaryFilter, JwtAuthenticationFilter.class)
            .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecretKey jwtSecretKey(JwtProperties properties) {
        byte[] bytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalStateException("GT JWT secret must be at least 32 bytes.");
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey key) {
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }
}
