package africa.growtogether.platform.eip;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="eip_circuits",uniqueConstraints=@UniqueConstraint(name="uk_eip_circuit_tenant_destination",columnNames={"tenant_id","destination"}))
public class IntegrationCircuit extends AuditedTenantEntity {
 @Column(name="destination",nullable=false,length=200) private String destination;
 @Enumerated(EnumType.STRING) @Column(name="circuit_state",nullable=false,length=20) private CircuitState circuitState=CircuitState.CLOSED;
 @Column(name="failure_count",nullable=false) private int failureCount;
 @Column(name="opened_at") private Instant openedAt;
 @Column(name="next_probe_at") private Instant nextProbeAt;
 protected IntegrationCircuit(){}
 public IntegrationCircuit(UUID tenantId,String destination){setTenantId(tenantId);this.destination=destination;}
 public void recordSuccess(){failureCount=0;circuitState=CircuitState.CLOSED;openedAt=null;nextProbeAt=null;}
 public void recordFailure(int threshold,Instant probeAt){failureCount++;if(failureCount>=threshold){circuitState=CircuitState.OPEN;openedAt=Instant.now();nextProbeAt=probeAt;}}
 public boolean permits(Instant now){if(circuitState==CircuitState.CLOSED)return true;if(circuitState==CircuitState.OPEN&&nextProbeAt!=null&&!now.isBefore(nextProbeAt)){circuitState=CircuitState.HALF_OPEN;return true;}return circuitState==CircuitState.HALF_OPEN;}
}
