package africa.growtogether.platform.eap;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
@Entity @Table(name="eap_dashboard_definitions",uniqueConstraints=@UniqueConstraint(name="uq_eap_dashboard_code",columnNames={"tenant_id","dashboard_code"}))
public class DashboardDefinition extends AuditedTenantEntity {
 @Column(name="dashboard_code",nullable=false,length=120) private String dashboardCode; @Column(name="dashboard_name",nullable=false,length=180) private String dashboardName; @Column(name="widget_configuration_json",nullable=false,columnDefinition="text") private String widgetConfigurationJson; @Column(name="active",nullable=false) private boolean active=true;
 protected DashboardDefinition(){} public DashboardDefinition(java.util.UUID tenantId,String code,String name,String widgets){setTenantId(tenantId);dashboardCode=req(code).toUpperCase(java.util.Locale.ROOT);dashboardName=req(name);widgetConfigurationJson=req(widgets);} private static String req(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("value is required");return v.trim();}
 public String dashboardCode(){return dashboardCode;} public String widgetConfigurationJson(){return widgetConfigurationJson;}
}
