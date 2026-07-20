package africa.growtogether.platform.eiam.tenant;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="eiam_organization")
public class Organization {
 @Id private UUID id; @Column(nullable=false,unique=true,length=80) private String code; @Column(nullable=false,length=200) private String name;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 protected Organization() {}
 public Organization(String code,String name){this.id=UUID.randomUUID();this.code=normalize(code);this.name=name.trim();this.createdAt=Instant.now();}
 public UUID getId(){return id;} public String getCode(){return code;} public String getName(){return name;} public Instant getCreatedAt(){return createdAt;}
 private static String normalize(String value){return value.trim().toUpperCase().replace(' ','_');}
}
