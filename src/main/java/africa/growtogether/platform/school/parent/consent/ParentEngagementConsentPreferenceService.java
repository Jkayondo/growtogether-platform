package africa.growtogether.platform.school.parent.consent;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class ParentEngagementConsentPreferenceService {


    private final ParentEngagementConsentPreferenceRepository repository;


    public ParentEngagementConsentPreferenceService(
            ParentEngagementConsentPreferenceRepository repository
    ) {

        this.repository = repository;
    }


    public ParentEngagementConsentPreference create(
            ParentEngagementConsentPreference preference
    ) {

        return repository.save(preference);
    }


    public List<ParentEngagementConsentPreference> findByParent(
            UUID parentId
    ) {

        return repository.findByParentId(parentId);
    }


    public void grant(
            ParentEngagementConsentPreference preference
    ) {

        preference.grantConsent();

        repository.save(preference);
    }


    public void revoke(
            ParentEngagementConsentPreference preference
    ) {

        preference.revokeConsent();

        repository.save(preference);
    }
}
