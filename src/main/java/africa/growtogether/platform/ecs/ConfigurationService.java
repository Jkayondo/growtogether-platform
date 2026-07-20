package africa.growtogether.platform.ecs;

import africa.growtogether.platform.common.web.RequestContextHolder;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static africa.growtogether.platform.ecs.ConfigurationDtos.*;

@Service
public class ConfigurationService {
 private final ConfigurationDefinitionRepository definitions;
 private final ConfigurationValueRepository values;
 private final ConfigurationVersionRepository versions;
 private final ConfigurationCryptoService crypto;
 public ConfigurationService(ConfigurationDefinitionRepository d,ConfigurationValueRepository v,ConfigurationVersionRepository h,ConfigurationCryptoService c){definitions=d;values=v;versions=h;crypto=c;}
 @Transactional public DefinitionView create(UpsertDefinition c){rejectSecretDefault(c);if(definitions.existsByCodeIgnoreCase(c.code()))throw new ConfigurationException("Configuration definition code already exists.");ConfigurationDefinition d=new ConfigurationDefinition(c.code(),c.name(),c.category(),c.description(),c.dataType(),c.defaultValue(),c.validationRules(),c.allowedScopes(),c.required(),c.secret());return DefinitionView.from(definitions.save(d));}
 @Transactional(readOnly=true) public List<DefinitionView> list(){return definitions.findAllByOrderByCategoryAscCodeAsc().stream().map(DefinitionView::from).toList();}
 @Transactional public DefinitionView update(UUID id,UpsertDefinition c){rejectSecretDefault(c);ConfigurationDefinition d=require(id);d.update(c.code(),c.name(),c.category(),c.description(),c.dataType(),c.defaultValue(),c.validationRules(),c.allowedScopes(),c.required(),c.secret(),c.active()==null||c.active());return DefinitionView.from(d);}
 @Transactional public ResolvedValue put(UUID id,PutValue c){ConfigurationDefinition d=require(id);String country=c.countryCode()==null?null:c.countryCode().toUpperCase(Locale.ROOT);String validated=ConfigurationValidator.validate(d,c.value());ConfigurationValue v=values.findExact(id,c.scope(),country,c.organizationId(),c.tenantId()).orElseGet(()->new ConfigurationValue(d,c.scope(),country,c.organizationId(),c.tenantId()));if(d.isSecret()){v.writeEncrypted(crypto.encrypt(validated),c.reason());}else{v.writePlain(validated,c.reason(),crypto.sha256(validated));}values.saveAndFlush(v);appendVersion(v,null);return view(d,v,canReadSecret());}
 @Transactional(readOnly=true) public ResolvedValue resolve(ResolveRequest r){ConfigurationDefinition d=definitions.findByCodeIgnoreCase(r.code()).orElseThrow(()->new ConfigurationException("Configuration definition was not found."));List<ConfigurationValue> cs=values.candidates(d.getId(),r.tenantId(),r.organizationId(),r.countryCode()==null?null:r.countryCode().toUpperCase(Locale.ROOT));Map<ConfigurationScope,Integer> rank=Map.of(ConfigurationScope.TENANT,4,ConfigurationScope.ORGANIZATION,3,ConfigurationScope.COUNTRY,2,ConfigurationScope.PLATFORM,1);ConfigurationValue best=cs.stream().max(Comparator.comparingInt(v->rank.get(v.getScope()))).orElse(null);if(best==null){String val=d.getDefaultValue();if(val==null&&d.isRequired())throw new ConfigurationException("Required configuration has no effective value: "+d.getCode());return new ResolvedValue(d.getCode(),d.getDataType(),d.isSecret()?maskOrDefault(d,val):val,null,d.getVersion(),d.isSecret());}return view(d,best,canReadSecret());}
 @Transactional(readOnly=true) public List<VersionView> history(UUID valueId){ConfigurationValue v=requireValue(valueId);return versions.findByConfigurationValueIdOrderByVersionNumberDesc(valueId).stream().map(x->VersionView.from(x,v.getDefinition().isSecret())).toList();}
 @Transactional public ResolvedValue rollback(UUID valueId,RollbackRequest request){ConfigurationValue current=requireValue(valueId);ConfigurationVersion target=versions.findByConfigurationValueIdAndVersionNumber(valueId,request.versionNumber()).orElseThrow(()->new ConfigurationException("Configuration version was not found."));String reason=(request.reason()==null||request.reason().isBlank())?"Rollback to version "+request.versionNumber():request.reason();current.restore(target.getStoredValue(),target.isEncrypted(),target.getEncryptionIv(),target.getEncryptionKeyId(),target.getValueHash(),reason);values.saveAndFlush(current);appendVersion(current,request.versionNumber());return view(current.getDefinition(),current,canReadSecret());}
 private void appendVersion(ConfigurationValue v,Long rollbackFrom){long next=versions.findByConfigurationValueIdOrderByVersionNumberDesc(v.getId()).stream().findFirst().map(x->x.getVersionNumber()+1).orElse(1L);versions.save(new ConfigurationVersion(v,next,actor(),correlation(),rollbackFrom));}
 private ResolvedValue view(ConfigurationDefinition d,ConfigurationValue v,boolean reveal){String value=v.getStoredValue();if(v.isEncrypted())value=reveal?crypto.decrypt(v.getStoredValue(),v.getEncryptionIv()):"***";return new ResolvedValue(d.getCode(),d.getDataType(),value,v.getScope(),d.getVersion(),d.isSecret());}
 private String maskOrDefault(ConfigurationDefinition d,String value){return d.isSecret()?"***":value;}
 private boolean canReadSecret(){Authentication a=SecurityContextHolder.getContext().getAuthentication();return a!=null&&a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("platform.configuration.secret.read"));}
 private String actor(){Authentication a=SecurityContextHolder.getContext().getAuthentication();return a==null||a.getName()==null?"system":a.getName();}
 private String correlation(){return RequestContextHolder.current().map(c->c.correlationId()).orElse(null);}
 private void rejectSecretDefault(UpsertDefinition c){if((c.secret()||c.dataType()==ConfigurationDataType.SECRET)&&c.defaultValue()!=null&&!c.defaultValue().isBlank())throw new ConfigurationException("Secret definitions cannot contain plaintext default values.");}
 private ConfigurationDefinition require(UUID id){return definitions.findById(id).orElseThrow(()->new ConfigurationException("Configuration definition was not found."));}
 private ConfigurationValue requireValue(UUID id){return values.findById(id).orElseThrow(()->new ConfigurationException("Configuration value was not found."));}
}
