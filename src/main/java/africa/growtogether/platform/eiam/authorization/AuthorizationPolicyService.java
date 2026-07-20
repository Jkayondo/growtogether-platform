package africa.growtogether.platform.eiam.authorization;

import africa.growtogether.platform.common.web.RequestContextHolder;
import java.util.*;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import static africa.growtogether.platform.eiam.authorization.PolicyDtos.*;

@Service
public class AuthorizationPolicyService {
 private final AuthorizationPolicyRepository repository;
 public AuthorizationPolicyService(AuthorizationPolicyRepository repository){this.repository=repository;}
 @Transactional public PolicyView create(UpsertPolicy c){UUID tenant=tenant(); if(repository.existsByTenantIdAndCodeIgnoreCase(tenant,c.code()))throw new AuthorizationPolicyException("A policy with this code already exists."); AuthorizationPolicy p=new AuthorizationPolicy(c.code(),c.name(),c.description(),c.resourceType(),c.action(),c.effect(),c.priority(),c.requiredPermission(),c.requiredRole(),c.ownerOnly(),c.minimumAal(),c.active()); p.setTenantId(tenant); return PolicyView.from(repository.save(p));}
 @Transactional(readOnly=true) public List<PolicyView> list(){return repository.findAllByTenantIdOrderByPriorityDescCodeAsc(tenant()).stream().map(PolicyView::from).toList();}
 @Transactional(readOnly=true) public PolicyView get(UUID id){return PolicyView.from(require(id));}
 @Transactional public PolicyView update(UUID id,UpsertPolicy c){AuthorizationPolicy p=require(id); if(!p.getCode().equalsIgnoreCase(c.code())&&repository.existsByTenantIdAndCodeIgnoreCase(tenant(),c.code()))throw new AuthorizationPolicyException("A policy with this code already exists."); p.update(c.code(),c.name(),c.description(),c.resourceType(),c.action(),c.effect(),c.priority(),c.requiredPermission(),c.requiredRole(),c.ownerOnly(),c.minimumAal(),c.active()); return PolicyView.from(p);}
 @Transactional public void delete(UUID id){repository.delete(require(id));}
 private AuthorizationPolicy require(UUID id){return repository.findByTenantIdAndId(tenant(),id).orElseThrow(()->new AuthorizationPolicyException("Authorization policy was not found."));}
 static UUID tenant(){return UUID.fromString(RequestContextHolder.require().tenantId());}
}
