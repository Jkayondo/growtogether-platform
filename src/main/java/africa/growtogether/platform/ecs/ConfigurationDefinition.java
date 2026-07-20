package africa.growtogether.platform.ecs;
import jakarta.persistence.*; import java.time.Instant; import java.util.*; import org.hibernate.annotations.UuidGenerator;
@Entity @Table(name="ecs_configuration_definitions", uniqueConstraints=@UniqueConstraint(name="uk_ecs_definition_code",columnNames="code"))
public class ConfigurationDefinition {
 @Id @GeneratedValue @UuidGenerator private UUID id;
 @Column(nullable=false,length=120) private String code;
 @Column(nullable=false,length=160) private String name;
 @Column(nullable=false,length=80) private String category;
 @Column(length=1000) private String description;
 @Enumerated(EnumType.STRING) @Column(name="data_type",nullable=false,length=20) private ConfigurationDataType dataType;
 @Column(name="default_value",columnDefinition="text") private String defaultValue;
 @Column(name="validation_rules",columnDefinition="jsonb") private String validationRules="{}";
 @Column(name="allowed_scopes",columnDefinition="text[]",nullable=false) private String[] allowedScopes;
 @Column(nullable=false) private boolean required;
 @Column(name="secret_value",nullable=false) private boolean secret;
 @Column(nullable=false) private boolean active=true;
 @Version private long version;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 protected ConfigurationDefinition(){}
 public ConfigurationDefinition(String code,String name,String category,String description,ConfigurationDataType dataType,String defaultValue,String validationRules,Set<ConfigurationScope> allowedScopes,boolean required,boolean secret){update(code,name,category,description,dataType,defaultValue,validationRules,allowedScopes,required,secret,true);}
 public void update(String code,String name,String category,String description,ConfigurationDataType dataType,String defaultValue,String validationRules,Set<ConfigurationScope> scopes,boolean required,boolean secret,boolean active){this.code=normalize(code);this.name=require(name,"name");this.category=normalize(category);this.description=description;this.dataType=Objects.requireNonNull(dataType);this.defaultValue=defaultValue;this.validationRules=(validationRules==null||validationRules.isBlank())?"{}":validationRules;this.allowedScopes=scopes.stream().map(Enum::name).sorted().toArray(String[]::new);if(this.allowedScopes.length==0)throw new ConfigurationException("At least one scope is required.");this.required=required;this.secret=secret||dataType==ConfigurationDataType.SECRET;this.active=active;}
 public boolean allows(ConfigurationScope scope){return Arrays.asList(allowedScopes).contains(scope.name());}
 @PrePersist void create(){createdAt=updatedAt=Instant.now();}
 @PreUpdate void touch(){updatedAt=Instant.now();}
 static String normalize(String v){return require(v,"value").trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_.-]","_");}
 static String require(String v,String f){if(v==null||v.isBlank())throw new ConfigurationException(f+" is required.");return v.trim();}
 public UUID getId(){return id;} public String getCode(){return code;} public String getName(){return name;} public String getCategory(){return category;} public String getDescription(){return description;} public ConfigurationDataType getDataType(){return dataType;} public String getDefaultValue(){return defaultValue;} public String getValidationRules(){return validationRules;} public Set<ConfigurationScope> getAllowedScopes(){Set<ConfigurationScope>s=new LinkedHashSet<>();for(String x:allowedScopes)s.add(ConfigurationScope.valueOf(x));return s;} public boolean isRequired(){return required;} public boolean isSecret(){return secret;} public boolean isActive(){return active;} public long getVersion(){return version;}
}
