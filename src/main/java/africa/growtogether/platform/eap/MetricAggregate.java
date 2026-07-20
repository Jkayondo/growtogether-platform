package africa.growtogether.platform.eap;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="eap_metric_aggregates",uniqueConstraints=@UniqueConstraint(name="uq_eap_aggregate_bucket",columnNames={"tenant_id","metric_code","bucket_start","dimension_hash"}),indexes=@Index(name="ix_eap_aggregate_query",columnList="tenant_id,metric_code,bucket_start"))
public class MetricAggregate extends AuditedTenantEntity {
 @Column(name="metric_code",nullable=false,length=120) private String metricCode;
 @Column(name="bucket_start",nullable=false) private Instant bucketStart;
 @Column(name="bucket_end",nullable=false) private Instant bucketEnd;
 @Column(name="dimension_hash",nullable=false,length=64) private String dimensionHash;
 @Column(name="dimensions_json",columnDefinition="text") private String dimensionsJson;
 @Column(name="sample_count",nullable=false) private long sampleCount;
 @Column(name="sum_value",nullable=false,precision=30,scale=8) private BigDecimal sumValue=BigDecimal.ZERO;
 @Column(name="minimum_value",precision=30,scale=8) private BigDecimal minimumValue;
 @Column(name="maximum_value",precision=30,scale=8) private BigDecimal maximumValue;
 protected MetricAggregate(){}
 public MetricAggregate(java.util.UUID tenantId,String code,Instant start,Instant end,String dimensionHash,String dimensionsJson){setTenantId(tenantId);this.metricCode=code;this.bucketStart=start;this.bucketEnd=end;this.dimensionHash=dimensionHash;this.dimensionsJson=dimensionsJson;}
 public void add(BigDecimal value){BigDecimal v=value==null?BigDecimal.ONE:value;sampleCount++;sumValue=sumValue.add(v);minimumValue=minimumValue==null||v.compareTo(minimumValue)<0?v:minimumValue;maximumValue=maximumValue==null||v.compareTo(maximumValue)>0?v:maximumValue;}
 public String metricCode(){return metricCode;} public Instant bucketStart(){return bucketStart;} public long sampleCount(){return sampleCount;} public BigDecimal sumValue(){return sumValue;} public BigDecimal average(){return sampleCount==0?BigDecimal.ZERO:sumValue.divide(BigDecimal.valueOf(sampleCount),8,java.math.RoundingMode.HALF_UP);}
}
