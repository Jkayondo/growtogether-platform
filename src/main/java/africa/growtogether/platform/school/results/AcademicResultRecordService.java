package africa.growtogether.platform.school.results;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class AcademicResultRecordService {


    private final AcademicResultRecordRepository repository;



    public AcademicResultRecordService(

            AcademicResultRecordRepository repository

    ) {

        this.repository = repository;

    }



    public AcademicResultRecord save(

            UUID tenantId,

            AcademicResultRecord record

    ) {


        return repository.save(record);

    }



    @Transactional(readOnly = true)
    public List<AcademicResultRecord> findStudentHistory(

            UUID tenantId,

            UUID studentId

    ) {


        return repository
                .findByTenantIdAndStudentIdOrderByCreatedAtDesc(

                        tenantId,

                        studentId

                );

    }



    @Transactional(readOnly = true)
    public List<AcademicResultRecord> findStudentYearResults(

            UUID tenantId,

            UUID studentId,

            UUID academicYearId

    ) {


        return repository
                .findByTenantIdAndStudentIdAndAcademicYearIdOrderByCreatedAtDesc(

                        tenantId,

                        studentId,

                        academicYearId

                );

    }



    @Transactional(readOnly = true)
    public List<AcademicResultRecord> findByScheme(

            UUID tenantId,

            UUID gradingSchemeId

    ) {


        return repository
                .findByTenantIdAndGradingSchemeIdOrderByCreatedAtDesc(

                        tenantId,

                        gradingSchemeId

                );

    }


}
