package africa.growtogether.platform.school.parent.consent.validation;


import africa.growtogether.platform.school.parent.consent.ParentEngagementConsentChannel;
import africa.growtogether.platform.school.parent.consent.ParentEngagementConsentPreference;
import africa.growtogether.platform.school.parent.consent.ParentEngagementConsentPreferenceRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class ParentEngagementConsentValidationService {


    private final ParentEngagementConsentPreferenceRepository repository;


    public ParentEngagementConsentValidationService(
            ParentEngagementConsentPreferenceRepository repository
    ) {

        this.repository = repository;
    }


    public ParentEngagementConsentValidationResult validate(
            UUID parentId,
            ParentEngagementConsentChannel channel
    ) {


        List<ParentEngagementConsentPreference> preferences =
                repository.findByParentId(parentId);


        if (preferences.isEmpty()) {

            return ParentEngagementConsentValidationResult
                    .BLOCKED_NO_CONSENT;
        }


        return preferences.stream()
                .filter(
                        preference ->
                                preference.getChannel() == channel
                )
                .findFirst()
                .map(
                        preference ->
                                preference.isGranted()
                                        ?
                                        ParentEngagementConsentValidationResult.ALLOWED
                                        :
                                        ParentEngagementConsentValidationResult.BLOCKED_REVOKED
                )
                .orElse(
                        ParentEngagementConsentValidationResult.BLOCKED_CHANNEL
                );
    }
}
