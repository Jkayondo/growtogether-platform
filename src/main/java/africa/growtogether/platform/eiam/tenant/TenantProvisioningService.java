package africa.growtogether.platform.eiam.tenant;
import africa.growtogether.platform.common.security.PasswordService;
import africa.growtogether.platform.eiam.permission.*;
import africa.growtogether.platform.eiam.role.*;
import africa.growtogether.platform.eiam.user.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class TenantProvisioningService {
 private static final List<String> ADMIN_PERMISSIONS=List.of(
  "eiam.users.create","eiam.users.read","eiam.users.update",
  "eiam.users.activate","eiam.users.suspend","eiam.users.deactivate",
  "eiam.roles.create","eiam.roles.read","eiam.roles.update","eiam.roles.delete",
  "eiam.user-roles.assign","eiam.user-roles.read",
  "eiam.permissions.create","eiam.permissions.read",
  "eiam.permissions.update","eiam.permissions.delete",
  "eiam.role-permissions.assign","eiam.role-permissions.read",
  "platform.tenants.read","platform.tenants.manage"
 );
 private static final List<String> EIP_PERMISSIONS=List.of(
  "integration.admin.read",
  "integration.analytics.read",
  "integration.certification.manage",
  "integration.certification.read",
  "integration.connector.manage",
  "integration.connector.read",
  "integration.dispute.manage",
  "integration.event.publish",
  "integration.event.read",
  "integration.gateway.manage",
  "integration.gateway.read",
  "integration.payment.create",
  "integration.payment.execute",
  "integration.payment.manage",
  "integration.payment.read",
  "integration.payment.reverse",
  "integration.reconciliation.manage",
  "integration.replay",
  "integration.route.manage",
  "integration.runtime.manage",
  "integration.settlement.manage",
  "integration.transformation.manage",
  "integration.transformation.read",
  "integration.webhook.manage",
  "integration.webhook.read"
 );
 private static final Set<String> INTEGRATION_ADMIN_PERMISSIONS=Set.of(
  "integration.admin.read",
  "integration.analytics.read",
  "integration.certification.manage",
  "integration.certification.read",
  "integration.connector.manage",
  "integration.connector.read",
  "integration.event.publish",
  "integration.event.read",
  "integration.gateway.manage",
  "integration.gateway.read",
  "integration.replay",
  "integration.route.manage",
  "integration.runtime.manage",
  "integration.transformation.manage",
  "integration.transformation.read",
  "integration.webhook.manage",
  "integration.webhook.read"
 );
 private static final List<String> ENS_PERMISSIONS=List.of(
  "notification.send",
  "notification.read",
  "notification.queue.manage",
  "notification.dispatch.manage",
  "notification.route.read",
  "notification.route.manage"
 );
 private static final Set<String> INTEGRATION_ADMIN_NOTIFICATION_PERMISSIONS=Set.of(
  "notification.dispatch.manage",
  "notification.route.read",
  "notification.route.manage"
 );
 private final OrganizationRepository organizations; private final TenantRepository tenants; private final UserAccountRepository users; private final RoleRepository roles; private final PermissionRepository permissions; private final UserRoleRepository userRoles; private final RolePermissionRepository rolePermissions; private final PasswordService passwords;
 public TenantProvisioningService(OrganizationRepository organizations,TenantRepository tenants,UserAccountRepository users,RoleRepository roles,PermissionRepository permissions,UserRoleRepository userRoles,RolePermissionRepository rolePermissions,PasswordService passwords){this.organizations=organizations;this.tenants=tenants;this.users=users;this.roles=roles;this.permissions=permissions;this.userRoles=userRoles;this.rolePermissions=rolePermissions;this.passwords=passwords;}
 @Transactional public TenantView provision(ProvisionTenantCommand c){
  if(organizations.existsByCodeIgnoreCase(c.organizationCode()))throw new TenantLifecycleException("Organization code already exists.");
  if(tenants.existsByCodeIgnoreCase(c.tenantCode()))throw new TenantLifecycleException("Tenant code already exists.");
  Organization organization=organizations.save(new Organization(c.organizationCode(),c.organizationName()));
  Tenant tenant=tenants.save(new Tenant(organization.getId(),c.tenantCode(),c.tenantName())); UUID tenantId=tenant.getId();
  UserAccount admin=new UserAccount(c.administratorUsername(),c.administratorEmail(),c.administratorDisplayName(),passwords.hash(c.administratorPassword())); admin.setTenantId(tenantId); admin.activate(); users.save(admin);
  Role role=new Role("TENANT_ADMIN","Tenant Administrator","Bootstrap administrator with tenant-wide EIAM authority.",true); role.setTenantId(tenantId); roles.save(role);
  Role integrationRole=new Role("INTEGRATION_ADMIN","Integration Administrator","Specialist administrator for governed enterprise integration infrastructure.",true); integrationRole.setTenantId(tenantId); roles.save(integrationRole);

  List<Permission> seeded=seedPermissions(tenantId,ADMIN_PERMISSIONS,"Bootstrap permission seeded during tenant provisioning.");
  List<Permission> eipSeeded=seedPermissions(tenantId,EIP_PERMISSIONS,"Enterprise integration permission seeded during tenant provisioning.");
  List<Permission> ensSeeded=seedPermissions(tenantId,ENS_PERMISSIONS,"Enterprise notification permission seeded during tenant provisioning.");

  UserRole ur=new UserRole(admin.getId(),role.getId());ur.setTenantId(tenantId);userRoles.save(ur);

  assignPermissions(tenantId,role.getId(),seeded);
  assignPermissions(
   tenantId,
   integrationRole.getId(),
   eipSeeded.stream()
    .filter(p->INTEGRATION_ADMIN_PERMISSIONS.contains(p.getCode()))
    .toList()
  );

  assignPermissions(
   tenantId,
   integrationRole.getId(),
   ensSeeded.stream()
    .filter(p->INTEGRATION_ADMIN_NOTIFICATION_PERMISSIONS.contains(p.getCode()))
    .toList()
  );

  tenant.activate();
  return new TenantView(organization.getId(),tenantId,organization.getCode(),tenant.getCode(),tenant.getName(),tenant.getStatus(),admin.getId(),role.getId());
 }
 @Transactional(readOnly=true) public Tenant get(UUID id){return tenants.findById(id).orElseThrow(()->new TenantLifecycleException("Tenant not found."));}
 @Transactional public Tenant changeStatus(UUID id,TenantStatus target){Tenant t=get(id);switch(target){case ACTIVE->t.activate();case SUSPENDED->t.suspend();case DEACTIVATED->t.deactivate();case PROVISIONING->throw new TenantLifecycleException("A tenant cannot return to provisioning.");}return t;}
 private List<Permission> seedPermissions(UUID tenantId,List<String> codes,String description){
  List<Permission> seeded=new ArrayList<>();
  for(String code:codes){
   Permission p=new Permission(code,title(code),module(code),description,true);
   p.setTenantId(tenantId);
   seeded.add(permissions.save(p));
  }
  return seeded;
 }
 private void assignPermissions(UUID tenantId,UUID roleId,List<Permission> assigned){
  for(Permission p:assigned){
   RolePermission rp=new RolePermission(roleId,p.getId());
   rp.setTenantId(tenantId);
   rolePermissions.save(rp);
  }
 }
 private static String module(String code){int i=code.indexOf('.');return (i<0?"PLATFORM":code.substring(0,i)).toUpperCase();}
 private static String title(String code){String leaf=code.substring(code.lastIndexOf('.')+1).replace('-',' ');return Character.toUpperCase(leaf.charAt(0))+leaf.substring(1);}
}
