package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class ClassGradeService {


    private final ClassGradeRepository repository;


    public ClassGradeService(
            ClassGradeRepository repository
    ) {
        this.repository = repository;
    }


    public ClassGrade create(
            UUID tenantId,
            UUID educationLevelId,
            String classCode,
            String className,
            Integer sequenceNumber,
            Integer capacity
    ) {


        ClassGrade classGrade =
                new ClassGrade(
                        educationLevelId,
                        classCode,
                        className,
                        sequenceNumber,
                        capacity
                );


        classGrade.setTenantId(tenantId);


        return repository.save(classGrade);

    }



    @Transactional(readOnly = true)
    public List<ClassGrade> findByEducationLevel(
            UUID tenantId,
            UUID educationLevelId
    ) {

        return repository
                .findByTenantIdAndEducationLevelId(
                        tenantId,
                        educationLevelId
                );

    }



    @Transactional(readOnly = true)
    public ClassGrade findByCode(
            UUID tenantId,
            String classCode
    ) {

        return repository
                .findByTenantIdAndClassCode(
                        tenantId,
                        classCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Class grade not found."
                        )
                );

    }



    public ClassGrade activate(
            ClassGrade classGrade
    ) {

        classGrade.activate();

        return repository.save(classGrade);

    }



    public ClassGrade deactivate(
            ClassGrade classGrade
    ) {

        classGrade.deactivate();

        return repository.save(classGrade);

    }

}
