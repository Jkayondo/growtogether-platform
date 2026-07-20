package africa.growtogether.platform.eap;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;

@Entity
@Table(name="eap_metric_definitions", uniqueConstraints=@UniqueConstraint(name="uq_eap_metric_code",columnNames={"tenant_id","metric_code"}))
public class MetricDefinition extends AuditedTenantEntity {
 @Column(name="metric_code",nullable=false,length=120) private String metricCode;
 @Column(name="metric_name",nullable=false,length=180) private String metricName;
 @Column(name="event_type",nullable=false,length=160) private String eventType;
 @Column(name="value_path",length=300) private String valuePath;
 @Column(name="dimension_paths_json",columnDefinition="text") private String dimensionPathsJson;
 @Enumerated(EnumType.STRING) @Column(name="metric_type",nullable=false,length=30) private AnalyticsEnums.MetricType metricType;
 @Enumerated(EnumType.STRING) @Column(name="aggregation_period",nullable=false,length=30) private AnalyticsEnums.AggregationPeriod aggregationPeriod;
 @Column(name="active",nullable=false) private boolean active=true;
 protected MetricDefinition(){}
 public MetricDefinition(java.util.UUID tenantId,String code,String name,String eventType,AnalyticsEnums.MetricType type,AnalyticsEnums.AggregationPeriod period,String valuePath,String dimensions){setTenantId(tenantId);this.metricCode=norm(code);this.metricName=req(name);this.eventType=req(eventType);this.metricType=type==null?AnalyticsEnums.MetricType.COUNTER:type;this.aggregationPeriod=period==null?AnalyticsEnums.AggregationPeriod.DAY:period;this.valuePath=valuePath;this.dimensionPathsJson=dimensions;}
 private static String req(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("value is required");return v.trim();} private static String norm(String v){return req(v).toUpperCase(java.util.Locale.ROOT).replace(' ','_');}
 public String metricCode(){return metricCode;} public String eventType(){return eventType;} public AnalyticsEnums.MetricType metricType(){return metricType;} public AnalyticsEnums.AggregationPeriod aggregationPeriod(){return aggregationPeriod;} public boolean active(){return active;}
}
