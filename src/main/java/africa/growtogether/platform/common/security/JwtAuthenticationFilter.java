package africa.growtogether.platform.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public final class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtDecoder decoder;
    private final SecurityErrorWriter errorWriter;

    public JwtAuthenticationFilter(JwtDecoder decoder, SecurityErrorWriter errorWriter) {
        this.decoder = decoder;
        this.errorWriter = errorWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        try {
            Jwt jwt = decoder.decode(authorization.substring(7));
            GtPrincipal principal = principal(jwt);
            var authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, jwt.getTokenValue(), authorities(principal));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            errorWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED,
                "GT-AUTH-002", "The access token is invalid or expired.");
        }
    }

    private static GtPrincipal principal(Jwt jwt) {
        return new GtPrincipal(
            UUID.fromString(jwt.getSubject()),
            jwt.getClaimAsString("username"),
            UUID.fromString(jwt.getClaimAsString("tenant_id")),
            set(jwt.getClaimAsStringList("roles")),
            set(jwt.getClaimAsStringList("permissions")),
            UUID.fromString(jwt.getClaimAsString("session_id"))
        );
    }

    private static Set<String> set(Collection<String> values) {
        return values == null ? Set.of() : Set.copyOf(values);
    }

    private static Set<SimpleGrantedAuthority> authorities(GtPrincipal principal) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        principal.roles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        principal.permissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        return Set.copyOf(authorities);
    }
}
