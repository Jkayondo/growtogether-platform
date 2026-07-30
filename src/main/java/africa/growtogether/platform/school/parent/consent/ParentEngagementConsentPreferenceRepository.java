package africa.growtogether.platform.school.parent.consent;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ParentEngagementConsentPreferenceRepository
        extends JpaRepository<ParentEngagementConsentPreference, UUID> {


    List<ParentEngagementConsentPreference>
    findByParentId(UUID parentId);


    List<ParentEngagementConsentPreference>
    findByParentIdAndStatus(
            UUID parentId,
            ParentEngagementConsentStatus status
    );
}
