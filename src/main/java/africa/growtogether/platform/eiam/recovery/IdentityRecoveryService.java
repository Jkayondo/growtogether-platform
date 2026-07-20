package africa.growtogether.platform.eiam.recovery;

import africa.growtogether.platform.common.security.PasswordService;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eiam.auth.UserSessionRepository;
import africa.growtogether.platform.eiam.user.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityRecoveryService {
    private final UserAccountRepository users; private final RecoveryTokenService tokens; private final PasswordService passwords; private final UserSessionRepository sessions; private final RecoveryProperties properties;
    public IdentityRecoveryService(UserAccountRepository users,RecoveryTokenService tokens,PasswordService passwords,UserSessionRepository sessions,RecoveryProperties properties){this.users=users;this.tokens=tokens;this.passwords=passwords;this.sessions=sessions;this.properties=properties;}
    @Transactional
    public Optional<RecoveryDispatch> requestPasswordReset(PasswordResetRequest request){return find(request.usernameOrEmail()).map(u->issue(u,RecoveryTokenPurpose.PASSWORD_RESET,properties.passwordResetSeconds()));}
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirm request){Instant now=Instant.now(); RecoveryToken token=tokens.consume(request.token(),RecoveryTokenPurpose.PASSWORD_RESET,now); UserAccount user=users.findByIdAndTenantId(token.getUserId(),token.getTenantId()).orElseThrow(InvalidRecoveryTokenException::new); user.changePasswordHash(passwords.hash(request.newPassword())); user.clearLoginSecurity(); sessions.revokeAllForUser(user.getTenantId(),user.getId(),"PASSWORD_RESET",now);}
    @Transactional
    public Optional<RecoveryDispatch> requestEmailVerification(EmailVerificationRequest request){UUID tenant=tenant(); Optional<UserAccount> user=(request.email()==null||request.email().isBlank())?Optional.empty():users.findByTenantIdAndEmailIgnoreCase(tenant,request.email()); return user.filter(u->!u.isEmailVerified()).map(u->issue(u,RecoveryTokenPurpose.EMAIL_VERIFICATION,properties.emailVerificationSeconds()));}
    @Transactional
    public void confirmEmailVerification(TokenConfirmation request){Instant now=Instant.now(); RecoveryToken token=tokens.consume(request.token(),RecoveryTokenPurpose.EMAIL_VERIFICATION,now); UserAccount user=users.findByIdAndTenantId(token.getUserId(),token.getTenantId()).orElseThrow(InvalidRecoveryTokenException::new); user.verifyEmail(now);}
    @Transactional
    public Optional<RecoveryDispatch> requestAccountRecovery(AccountRecoveryRequest request){return find(request.usernameOrEmail()).map(u->issue(u,RecoveryTokenPurpose.ACCOUNT_RECOVERY,properties.passwordResetSeconds()));}
    @Transactional
    public void confirmAccountRecovery(PasswordResetConfirm request){Instant now=Instant.now(); RecoveryToken token=tokens.consume(request.token(),RecoveryTokenPurpose.ACCOUNT_RECOVERY,now); UserAccount user=users.findByIdAndTenantId(token.getUserId(),token.getTenantId()).orElseThrow(InvalidRecoveryTokenException::new); user.changePasswordHash(passwords.hash(request.newPassword())); user.recoverAccount(); sessions.revokeAllForUser(user.getTenantId(),user.getId(),"ACCOUNT_RECOVERY",now);}
    private RecoveryDispatch issue(UserAccount user,RecoveryTokenPurpose purpose,long ttl){Instant now=Instant.now(); Instant expires=now.plusSeconds(ttl); return new RecoveryDispatch(tokens.issue(user.getTenantId(),user.getId(),purpose,expires,now),expires);}
    private Optional<UserAccount> find(String value){UUID tenant=tenant(); return users.findByTenantIdAndUsernameIgnoreCase(tenant,value).or(()->users.findByTenantIdAndEmailIgnoreCase(tenant,value));}
    private UUID tenant(){return RequestContextHolder.current().map(c->c.tenantId()).filter(v->v!=null&&!v.isBlank()).map(UUID::fromString).orElseThrow(()->new InvalidRecoveryTokenException());}
}
