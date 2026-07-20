package africa.growtogether.platform.eip;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="eip_gateway_routes",uniqueConstraints=@UniqueConstraint(name="uk_eip_gateway_route_code",columnNames={"tenant_id","route_code"}))
public class GatewayRoute extends AuditedTenantEntity {
 @Column(name="route_code",nullable=false,length=100) private String routeCode;
 @Column(name="path_pattern",nullable=false,length=250) private String pathPattern;
 @Column(name="http_method",nullable=false,length=12) private String httpMethod;
 @Column(name="upstream_uri",nullable=false,length=500) private String upstreamUri;
 @Column(name="required_authority",length=180) private String requiredAuthority;
 @Column(name="rate_limit_per_minute",nullable=false) private int rateLimitPerMinute;
 @Column(name="timeout_millis",nullable=false) private int timeoutMillis;
 @Column(name="active",nullable=false) private boolean active=true;
 protected GatewayRoute(){}
 public GatewayRoute(UUID tenantId,String code,String path,String method,String uri,String authority,int rate,int timeout){setTenantId(tenantId);routeCode=req(code);pathPattern=req(path);httpMethod=req(method).toUpperCase();upstreamUri=req(uri);requiredAuthority=authority;rateLimitPerMinute=rate<1?60:rate;timeoutMillis=timeout<1?5000:timeout;}
 private static String req(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("Required value missing");return v.trim();}
 public UUID id(){return getId();} public String routeCode(){return routeCode;} public String pathPattern(){return pathPattern;} public String httpMethod(){return httpMethod;} public String upstreamUri(){return upstreamUri;} public String requiredAuthority(){return requiredAuthority;} public int rateLimitPerMinute(){return rateLimitPerMinute;} public int timeoutMillis(){return timeoutMillis;} public boolean active(){return active;}
}
