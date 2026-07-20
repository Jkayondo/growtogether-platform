package africa.growtogether.platform.ens.integration;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext; import africa.growtogether.platform.ecs.ConfigurationDtos.ResolveRequest; import africa.growtogether.platform.ecs.ConfigurationService; import org.springframework.stereotype.Component;
@Component public class EnsConfigurationGateway { private final ConfigurationService configuration; private final EnterpriseIdentityContext identity; public EnsConfigurationGateway(ConfigurationService c,EnterpriseIdentityContext i){configuration=c;identity=i;}
 private String value(String code,String fallback){try{var r=configuration.resolve(new ResolveRequest(code,null,null,identity.tenantId()));return r.value()==null?fallback:r.value();}catch(RuntimeException e){return fallback;}}
 public int maxAttempts(){try{return Integer.parseInt(value("ENS_MAX_RETRY_ATTEMPTS","5"));}catch(Exception e){return 5;}}
 public long retryBackoffSeconds(int attempt){long base;try{base=Long.parseLong(value("ENS_RETRY_BASE_SECONDS","30"));}catch(Exception e){base=30;} return Math.min(base*(1L<<Math.min(Math.max(attempt-1,0),10)),86400);}
}
