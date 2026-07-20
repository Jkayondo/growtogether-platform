package africa.growtogether.platform.eiam.recovery;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecoveryTokenService {
    private final RecoveryTokenRepository repository;
    private final SecureRandom random = new SecureRandom();
    public RecoveryTokenService(RecoveryTokenRepository repository){this.repository=repository;}
    @Transactional
    public String issue(UUID tenantId, UUID userId, RecoveryTokenPurpose purpose, Instant expiresAt, Instant now) {
        repository.findAllByTenantIdAndUserIdAndPurposeAndConsumedAtIsNullAndInvalidatedAtIsNull(tenantId,userId,purpose)
            .forEach(token -> token.invalidate(now));
        byte[] bytes=new byte[32]; random.nextBytes(bytes); String raw=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RecoveryToken token=new RecoveryToken(userId,purpose,hash(raw),expiresAt); token.setTenantId(tenantId); repository.save(token); return raw;
    }
    @Transactional
    public RecoveryToken consume(String raw, RecoveryTokenPurpose purpose, Instant now) {
        RecoveryToken token=repository.findByTokenHashAndPurpose(hash(raw),purpose).orElseThrow(InvalidRecoveryTokenException::new);
        token.consume(now); return token;
    }
    String hash(String raw){ try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8))); } catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);} }
}
