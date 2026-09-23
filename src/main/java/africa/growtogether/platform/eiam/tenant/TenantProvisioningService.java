package africa.growtogether.platform.eiam.tenant;

import africa.growtogether.platform.eaif.AiEnums;
import africa.growtogether.platform.eaif.governance.policy.AiGovernancePolicy;
import africa.growtogether.platform.eaif.governance.policy.AiGovernancePolicyRepository;

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

 private static final List<String> CONFIGURATION_PERMISSIONS=List.of(
  "platform.configuration.definition.manage",
  "platform.configuration.manage",
  "platform.configuration.read",
  "platform.configuration.history.read",
  "platform.configuration.rollback",
  "platform.configuration.secret.read"
 );

 /*
  * Secret configuration read remains a separate high-privilege
  * authority. TENANT_ADMIN can govern configuration without
  * automatically receiving decrypted-secret visibility.
  */
 private static final Set<String> TENANT_ADMIN_CONFIGURATION_PERMISSIONS=Set.of(
  "platform.configuration.manage",
  "platform.configuration.read",
  "platform.configuration.history.read",
  "platform.configuration.rollback"
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
 private record TeacherPermissionDefinition(
  String code,
  String name,
  String module,
  String description,
  boolean systemPermission
 ) {}

 private static final List<TeacherPermissionDefinition> TEACHER_BASELINE_PERMISSIONS=List.of(
  new TeacherPermissionDefinition(
   "school.academic.curriculum.read",
   "Read Curriculum",
   "SCHOOL_ACADEMIC",
   "Allows viewing curricula",
   false
  ),
  new TeacherPermissionDefinition(
   "school.academic.class-grade.read",
   "Read Class Grades",
   "SCHOOL_ACADEMIC",
   "Allows viewing academic class grades",
   false
  ),
  new TeacherPermissionDefinition(
   "school.academic.subject.read",
   "Read Subjects",
   "SCHOOL_ACADEMIC",
   "Allows viewing academic subjects",
   false
  ),
  new TeacherPermissionDefinition(
   "school.academic.teaching-assignment.read",
   "Read Teaching Assignments",
   "SCHOOL_ACADEMIC",
   "Allows viewing teacher academic assignments",
   false
  ),
  new TeacherPermissionDefinition(
   "school.teacher.programme.read",
   "Read Teacher Programme",
   "SCHOOL_TEACHER",
   "Allows an authenticated teacher to view their own authorised Today's Programme, including applicable teaching lessons and teacher-visible school calendar events within their authenticated tenant.",
   false
  ),
  new TeacherPermissionDefinition(
   "school.teacher.coverage.read",
   "Read Teacher Coverage",
   "SCHOOL_TEACHER",
   "Allows an authenticated teacher to view curriculum coverage belonging to their own authorised teaching assignments within their authenticated tenant.",
   false
  ),
  new TeacherPermissionDefinition(
   "school.teacher.coverage.update",
   "Update Teacher Coverage",
   "SCHOOL_TEACHER",
   "Allows an authenticated teacher to update curriculum coverage status only for records belonging to their own authorised teaching assignments within their authenticated tenant.",
   false
  ),
  new TeacherPermissionDefinition(
   "ai.request.create",
   "AI Request Create",
   "EAIF",
   "Create governed enterprise AI requests.",
   true
  ),
  new TeacherPermissionDefinition(
   "ai.request.read",
   "AI Request Read",
   "EAIF",
   "Read governed enterprise AI request state and results.",
   true
  ),
  new TeacherPermissionDefinition(
   "ai.runtime.execute",
   "AI Runtime Execute",
   "EAIF",
   "Execute an authorised governed enterprise AI request.",
   true
  )
 );

 private record LeadershipPermissionDefinition(
  String code,
  String name,
  String module,
  String description,
  boolean systemPermission
 ) {}

 private static final List<LeadershipPermissionDefinition>
  LEADERSHIP_BASELINE_PERMISSIONS=List.of(
   new LeadershipPermissionDefinition(
    "school.leadership.overview.read",
    "Read School Leadership Overview",
    "SCHOOL_LEADERSHIP",
    "Allows an authorised school leadership user to read the tenant-scoped GT School Leadership overview and its permitted aggregate indicators.",
    false
   )
  );

 private record AiAdminPermissionDefinition(
  String code,
  String name,
  String description
 ) {}

 private static final List<AiAdminPermissionDefinition> AI_ADMIN_BASELINE_PERMISSIONS=List.of(
  new AiAdminPermissionDefinition(
   "ai.provider.manage",
   "AI Provider Manage",
   "Manage governed enterprise AI provider registrations."
  ),
  new AiAdminPermissionDefinition(
   "ai.model.manage",
   "AI Model Manage",
   "Manage governed enterprise AI model catalogue entries."
  ),
  new AiAdminPermissionDefinition(
   "ai.prompt.manage",
   "AI Prompt Manage",
   "Manage governed enterprise AI prompt templates and controls."
  ),
  new AiAdminPermissionDefinition(
   "ai.governance.read",
   "AI Governance Read",
   "Read governed enterprise AI governance policy and control state."
  ),
  new AiAdminPermissionDefinition(
   "ai.audit.read",
   "AI Audit Read",
   "Read governed enterprise AI audit records."
  ),
  new AiAdminPermissionDefinition(
   "ai.evidence.read",
   "AI Evidence Read",
   "Read governed enterprise AI execution evidence."
  ),
  new AiAdminPermissionDefinition(
   "ai.request.approval",
   "AI Request Approval",
   "Approve governed enterprise AI requests requiring human authorization."
  )
 );
 private final AiGovernancePolicyRepository aiGovernancePolicies;

 private final OrganizationRepository organizations; private final TenantRepository tenants; private final UserAccountRepository users; private final RoleRepository roles; private final PermissionRepository permissions; private final UserRoleRepository userRoles; private final RolePermissionRepository rolePermissions; private final PasswordService passwords;
 public TenantProvisioningService(OrganizationRepository organizations,TenantRepository tenants,UserAccountRepository users,RoleRepository roles,PermissionRepository permissions,UserRoleRepository userRoles,RolePermissionRepository rolePermissions,PasswordService passwords,
   AiGovernancePolicyRepository aiGovernancePolicies){
  this.aiGovernancePolicies=aiGovernancePolicies;
this.organizations=organizations;this.tenants=tenants;this.users=users;this.roles=roles;this.permissions=permissions;this.userRoles=userRoles;this.rolePermissions=rolePermissions;this.passwords=passwords;}
 @Transactional public TenantView provision(ProvisionTenantCommand c){
  if(organizations.existsByCodeIgnoreCase(c.organizationCode()))throw new TenantLifecycleException("Organization code already exists.");
  if(tenants.existsByCodeIgnoreCase(c.tenantCode()))throw new TenantLifecycleException("Tenant code already exists.");
  Organization organization=organizations.save(new Organization(c.organizationCode(),c.organizationName()));
  Tenant tenant=tenants.save(new Tenant(organization.getId(),c.tenantCode(),c.tenantName())); UUID tenantId=tenant.getId();
  UserAccount admin=new UserAccount(c.administratorUsername(),c.administratorEmail(),c.administratorDisplayName(),passwords.hash(c.administratorPassword())); admin.setTenantId(tenantId); admin.activate(); users.save(admin);
  Role role=new Role("TENANT_ADMIN","Tenant Administrator","Bootstrap administrator with tenant-wide EIAM authority.",true); role.setTenantId(tenantId); roles.save(role);
  Role integrationRole=new Role("INTEGRATION_ADMIN","Integration Administrator","Specialist administrator for governed enterprise integration infrastructure.",true); integrationRole.setTenantId(tenantId); roles.save(integrationRole);
  Role teacherRole=new Role(
   "TEACHER",
   "Teacher",
   "GT School teacher role for governed teacher-facing capabilities.",
   false
  );
  teacherRole.setTenantId(tenantId);
  roles.save(teacherRole);

  Role aiAdminRole=new Role(
   "AI_ADMIN",
   "AI Administrator",
   "Specialist administrator for governed enterprise AI administration.",
   true
  );
  aiAdminRole.setTenantId(tenantId);
  roles.save(aiAdminRole);

  List<Permission> seeded=seedPermissions(tenantId,ADMIN_PERMISSIONS,"Bootstrap permission seeded during tenant provisioning.");
  List<Permission> configurationSeeded=seedPermissions(tenantId,CONFIGURATION_PERMISSIONS,"Enterprise configuration permission seeded during tenant provisioning.");
  List<Permission> eipSeeded=seedPermissions(tenantId,EIP_PERMISSIONS,"Enterprise integration permission seeded during tenant provisioning.");
  List<Permission> ensSeeded=seedPermissions(tenantId,ENS_PERMISSIONS,"Enterprise notification permission seeded during tenant provisioning.");
  List<Permission> teacherBaselineSeeded=
   seedTeacherBaselinePermissions(tenantId);

  seedLeadershipBaselinePermissions(tenantId);

  List<Permission> aiAdminSeeded=
   seedAiAdminPermissions(tenantId);

  UserRole ur=new UserRole(admin.getId(),role.getId());ur.setTenantId(tenantId);userRoles.save(ur);

  assignPermissions(tenantId,role.getId(),seeded);
  assignPermissions(
   tenantId,
   role.getId(),
   configurationSeeded.stream()
    .filter(
     p->TENANT_ADMIN_CONFIGURATION_PERMISSIONS.contains(
      p.getCode()
     )
    )
    .toList()
  );
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

  assignPermissions(
   tenantId,
   teacherRole.getId(),
   teacherBaselineSeeded
  );

  List<Permission> aiAdminGranted=
   new ArrayList<>(aiAdminSeeded);

  teacherBaselineSeeded.stream()
   .filter(
    p->"ai.request.read".equals(p.getCode())
   )
   .findFirst()
   .ifPresent(aiAdminGranted::add);

  if(aiAdminGranted.size()!=8){
   throw new IllegalStateException(
    "AI_ADMIN provisioning requires seven administration permissions plus ai.request.read."
   );
  }

  assignPermissions(
   tenantId,
   aiAdminRole.getId(),
   aiAdminGranted
  );

  seedDefaultAiPolicy(tenantId);
  tenant.activate();
  return new TenantView(organization.getId(),tenantId,organization.getCode(),tenant.getCode(),tenant.getName(),tenant.getStatus(),admin.getId(),role.getId());
 }
 @Transactional(readOnly=true) public Tenant get(UUID id){return tenants.findById(id).orElseThrow(()->new TenantLifecycleException("Tenant not found."));}
 @Transactional public Tenant changeStatus(UUID id,TenantStatus target){Tenant t=get(id);switch(target){case ACTIVE->t.activate();case SUSPENDED->t.suspend();case DEACTIVATED->t.deactivate();case PROVISIONING->throw new TenantLifecycleException("A tenant cannot return to provisioning.");}return t;}
 private void seedDefaultAiPolicy(UUID tenantId){
  if(aiGovernancePolicies
   .findByTenantIdAndPolicyCode(
    tenantId,
    "DEFAULT_AI_POLICY"
   )
   .isPresent()){
   return;
  }

  aiGovernancePolicies.save(
   new AiGovernancePolicy(
    tenantId,
    "DEFAULT_AI_POLICY",
    "Default AI Governance Policy",
    AiEnums.RiskLevel.HIGH,
    true
   )
  );
 }

 private List<Permission> seedAiAdminPermissions(UUID tenantId){
  List<Permission> seeded=new ArrayList<>();

  for(AiAdminPermissionDefinition definition:AI_ADMIN_BASELINE_PERMISSIONS){
   Permission p=new Permission(
    definition.code(),
    definition.name(),
    "EAIF",
    definition.description(),
    true
   );
   p.setTenantId(tenantId);
   seeded.add(permissions.save(p));
  }

  return seeded;
 }

 private List<Permission> seedTeacherBaselinePermissions(UUID tenantId){
  List<Permission> seeded=new ArrayList<>();
  for(TeacherPermissionDefinition definition:TEACHER_BASELINE_PERMISSIONS){
   Permission p=new Permission(
    definition.code(),
    definition.name(),
    definition.module(),
    definition.description(),
    definition.systemPermission()
   );
   p.setTenantId(tenantId);
   seeded.add(permissions.save(p));
  }
  return seeded;
 }
 private List<Permission> seedLeadershipBaselinePermissions(
  UUID tenantId
 ){
  List<Permission> seeded=new ArrayList<>();

  for(
   LeadershipPermissionDefinition definition:
    LEADERSHIP_BASELINE_PERMISSIONS
  ){
   Permission p=new Permission(
    definition.code(),
    definition.name(),
    definition.module(),
    definition.description(),
    definition.systemPermission()
   );
   p.setTenantId(tenantId);
   seeded.add(permissions.save(p));
  }

  return seeded;
 }

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
