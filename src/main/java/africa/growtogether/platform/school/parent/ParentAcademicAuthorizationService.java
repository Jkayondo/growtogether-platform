package africa.growtogether.platform.school.parent;


import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ParentAcademicAuthorizationService {


    private final EnterpriseIdentityContext identityContext;

    private final ParentAcademicAccessRepository repository;


    public ParentAcademicAuthorizationService(
            EnterpriseIdentityContext identityContext,
            ParentAcademicAccessRepository repository
    ) {

        this.identityContext = identityContext;
        this.repository = repository;
    }


    public void verifyParentAccess(
            UUID learnerId
    ) {


        UUID authenticatedUser =
                identityContext.requireUserId();


        boolean allowed =
                repository
                        .findByParentIdAndAccessStatus(
                                authenticatedUser,
                                ParentAcademicAccessStatus.ACTIVE
                        )
                        .stream()
                        .anyMatch(access ->
                                access.getLearnerId()
                                        .equals(learnerId)
                        );


        if (!allowed) {

            throw new SecurityException(
                    "Parent does not have access to this learner."
            );
        }
    }
}
