package africa.growtogether.platform.eaif;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

interface AiProviderRepository
        extends JpaRepository<AiProvider, UUID> {

    Optional<AiProvider> findByTenantIdAndCode(
            UUID tenantId,
            String code
    );
}


interface AiModelRepository
        extends JpaRepository<AiModel, UUID> {

    Optional<AiModel> findByTenantIdAndCode(
            UUID tenantId,
            String code
    );
}


interface PromptTemplateRepository
        extends JpaRepository<PromptTemplate, UUID> {

    List<PromptTemplate> findByTenantIdAndCodeOrderByTemplateVersionDesc(
            UUID tenantId,
            String code
    );
}
