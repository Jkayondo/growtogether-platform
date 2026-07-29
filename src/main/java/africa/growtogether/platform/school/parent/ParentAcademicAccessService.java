package africa.growtogether.platform.school.parent;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class ParentAcademicAccessService {


    private final ParentAcademicAccessRepository repository;


    public ParentAcademicAccessService(
            ParentAcademicAccessRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public ParentAcademicAccess grantAccess(
            UUID tenantId,
            UUID parentId,
            UUID learnerId
    ) {

        ParentAcademicAccess access =
                new ParentAcademicAccess(
                        tenantId,
                        parentId,
                        learnerId
                );


        return repository.save(access);
    }


    @Transactional
    public ParentAcademicAccess suspendAccess(
            ParentAcademicAccess access
    ) {

        access.suspend();

        return repository.save(access);
    }


    @Transactional
    public ParentAcademicAccess revokeAccess(
            ParentAcademicAccess access
    ) {

        access.revoke();

        return repository.save(access);
    }


    @Transactional(readOnly = true)
    public List<ParentAcademicAccess> getParentLearners(
            UUID parentId
    ) {

        return repository.findByParentIdAndAccessStatus(
                parentId,
                ParentAcademicAccessStatus.ACTIVE
        );
    }


    @Transactional(readOnly = true)
    public List<ParentAcademicAccess> getLearnerParents(
            UUID learnerId
    ) {

        return repository.findByLearnerId(
                learnerId
        );
    }


    @Transactional(readOnly = true)
    public List<ParentAcademicAccess> getSchoolAccessRecords(
            UUID tenantId
    ) {

        return repository.findByTenantId(
                tenantId
        );
    }
}
