package africa.growtogether.platform.eip;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="eip_routes", uniqueConstraints=@UniqueConstraint(name="uk_eip_route_tenant_code",columnNames={"tenant_id","route_code"}))
public class IntegrationRoute extends AuditedTenantEntity {
 @Column(name="route_code",nullable=false,length=100) private String routeCode;
 @Column(name="event_pattern",nullable=false,length=180) private String eventPattern;
 @Column(name="destination",nullable=false,length=200) private String destination;
 @Enumerated(EnumType.STRING) @Column(name="protocol",nullable=false,length=30) private IntegrationProtocol protocol;
 @Column(name="priority",nullable=false) private int priority;
 @Column(name="enabled",nullable=false) private boolean enabled=true;
 protected IntegrationRoute(){}
 public IntegrationRoute(UUID tenantId,String code,String pattern,String destination,IntegrationProtocol protocol,int priority){setTenantId(tenantId);routeCode=req(code).toUpperCase();eventPattern=req(pattern);this.destination=req(destination);this.protocol=protocol;this.priority=priority;}
 private static String req(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("value is required");return v.trim();}
 public boolean matches(String eventType){if(!enabled)return false;String regex=eventPattern.replace(".","\\.").replace("*",".*");return eventType.matches(regex);}
 public String routeCode(){return routeCode;} public String destination(){return destination;} public IntegrationProtocol protocol(){return protocol;} public int priority(){return priority;}
}
