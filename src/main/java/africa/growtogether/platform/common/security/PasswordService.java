package africa.growtogether.platform.common.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public final class PasswordService {
    private final PasswordEncoder encoder;

    public PasswordService(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
