package africa.growtogether.platform.eiam.authorization;

import africa.growtogether.platform.common.security.GtPrincipal;
import africa.growtogether.platform.eiam.audit.*;
import java.util.*;
import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import static africa.growtogether.platform.eiam.authorization.PolicyDtos.*;

@Service
public class EnterpriseAuthorizationService {
 private final AuthorizationPolicyRepository policies; private final AuditEventService audit;
 public EnterpriseAuthorizationService(AuthorizationPolicyRepository policies,AuditEventService audit){this.policies=policies;this.audit=audit;}
 @Transactional(readOnly=true)
 public DecisionView evaluate(AuthorizationRequest request){
  GtPrincipal principal=principal(); UUID tenant=AuthorizationPolicyService.tenant();
  List<AuthorizationPolicy> candidates=policies.findAllByTenantIdAndActiveTrueAndResourceTypeIgnoreCaseAndActionIgnoreCaseOrderByPriorityDescCodeAsc(tenant,request.resourceType(),request.action());
  List<AuthorizationPolicy> matched=candidates.stream().filter(p->matches(p,principal,request)).toList();
  AuthorizationPolicy deny=matched.stream().filter(p->p.getEffect()==PolicyEffect.DENY).findFirst().orElse(null);
  AuthorizationPolicy allow=matched.stream().filter(p->p.getEffect()==PolicyEffect.ALLOW).findFirst().orElse(null);
  AuthorizationDecision decision=deny!=null?AuthorizationDecision.DENY:allow!=null?AuthorizationDecision.ALLOW:AuthorizationDecision.DENY;
  String reason=deny!=null?"Explicit deny policy matched: "+deny.getCode():allow!=null?"Allow policy matched: "+allow.getCode():"No applicable allow policy matched; default deny applied.";
  audit.record(new RecordAuditEventCommand("AUTHORIZATION.POLICY."+decision,AuditEventCategory.AUTHORIZATION,decision==AuthorizationDecision.ALLOW?AuditOutcome.SUCCESS:AuditOutcome.DENIED,decision==AuthorizationDecision.ALLOW?SecuritySeverity.INFO:SecuritySeverity.MEDIUM,request.resourceType(),request.resourceId(),reason,Map.of("action",request.action(),"matchedPolicies",matched.stream().map(AuthorizationPolicy::getCode).toList())));
  return new DecisionView(decision,reason,matched.stream().map(AuthorizationPolicy::getCode).toList(),principal.roles(),principal.permissions());
 }
 private boolean matches(AuthorizationPolicy p,GtPrincipal principal,AuthorizationRequest r){
  if(p.getRequiredPermission()!=null&&!principal.permissions().contains(p.getRequiredPermission()))return false;
  if(p.getRequiredRole()!=null&&!principal.roles().contains(p.getRequiredRole()))return false;
  if(p.isOwnerOnly()&&(r.resourceOwnerId()==null||!r.resourceOwnerId().equals(principal.userId())))return false;
  return Math.max(1,r.assuranceLevel())>=p.getMinimumAal();
 }
 private GtPrincipal principal(){var a=SecurityContextHolder.getContext().getAuthentication(); if(a==null||!(a.getPrincipal() instanceof GtPrincipal p))throw new AuthorizationPolicyException("An authenticated principal is required."); return p;}
}
