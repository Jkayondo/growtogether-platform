package africa.growtogether.platform.eiam.authorization;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;

@Entity
@Table(name="eiam_authorization_policy")
public class AuthorizationPolicy extends AuditedTenantEntity {
 @Column(nullable=false,length=100) private String code;
 @Column(nullable=false,length=150) private String name;
 @Column(length=500) private String description;
 @Column(name="resource_type",nullable=false,length=120) private String resourceType;
 @Column(nullable=false,length=120) private String action;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=10) private PolicyEffect effect;
 @Column(nullable=false) private int priority;
 @Column(name="required_permission",length=180) private String requiredPermission;
 @Column(name="required_role",length=100) private String requiredRole;
 @Column(name="owner_only",nullable=false) private boolean ownerOnly;
 @Column(name="minimum_aal",nullable=false) private int minimumAal;
 @Column(nullable=false) private boolean active;
 protected AuthorizationPolicy() {}
 public AuthorizationPolicy(String code,String name,String description,String resourceType,String action,PolicyEffect effect,int priority,String requiredPermission,String requiredRole,boolean ownerOnly,int minimumAal,boolean active){update(code,name,description,resourceType,action,effect,priority,requiredPermission,requiredRole,ownerOnly,minimumAal,active);}
 public void update(String code,String name,String description,String resourceType,String action,PolicyEffect effect,int priority,String requiredPermission,String requiredRole,boolean ownerOnly,int minimumAal,boolean active){
  this.code=normCode(code); this.name=req(name); this.description=blank(description); this.resourceType=req(resourceType).toUpperCase(); this.action=req(action).toLowerCase(); this.effect=java.util.Objects.requireNonNull(effect); this.priority=priority; this.requiredPermission=blank(requiredPermission); this.requiredRole=blank(requiredRole)==null?null:blank(requiredRole).toUpperCase(); this.ownerOnly=ownerOnly; this.minimumAal=Math.max(1,minimumAal); this.active=active;
 }
 public boolean matches(String resourceType,String action){return active&&this.resourceType.equalsIgnoreCase(resourceType)&&this.action.equalsIgnoreCase(action);}
 public String getCode(){return code;} public String getName(){return name;} public String getDescription(){return description;} public String getResourceType(){return resourceType;} public String getAction(){return action;} public PolicyEffect getEffect(){return effect;} public int getPriority(){return priority;} public String getRequiredPermission(){return requiredPermission;} public String getRequiredRole(){return requiredRole;} public boolean isOwnerOnly(){return ownerOnly;} public int getMinimumAal(){return minimumAal;} public boolean isActive(){return active;}
 private static String normCode(String v){return req(v).toUpperCase().replace(' ','_');} private static String req(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("Required policy value is missing.");return v.trim();} private static String blank(String v){return v==null||v.isBlank()?null:v.trim();}
}
