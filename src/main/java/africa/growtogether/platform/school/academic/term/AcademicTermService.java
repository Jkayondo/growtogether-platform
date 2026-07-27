package africa.growtogether.platform.school.academic.term;


import africa.growtogether.platform.school.academic.year.AcademicYear;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Service
public class AcademicTermService {


    private final AcademicTermRepository repository;


    public AcademicTermService(
            AcademicTermRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public AcademicTerm create(
            UUID tenantId,
            AcademicYear academicYear,
            String termCode,
            String termName,
            LocalDate startDate,
            LocalDate endDate,
            Integer sequenceNumber
    ) {


        validateDates(
                startDate,
                endDate
        );


        validateSequence(
                sequenceNumber
        );


        AcademicTerm term =
                new AcademicTerm(
                        academicYear,
                        termCode,
                        termName,
                        startDate,
                        endDate,
                        sequenceNumber
                );


        term.setTenantId(tenantId);


        return repository.save(term);
    }


    @Transactional(readOnly = true)
    public List<AcademicTerm> findByAcademicYear(
            UUID academicYearId
    ) {

        return repository.findByAcademicYearId(
                academicYearId
        );
    }


    @Transactional(readOnly = true)
    public List<AcademicTerm> findActiveTerms(
            UUID academicYearId
    ) {

        return repository.findByAcademicYearIdAndStatus(
                academicYearId,
                "ACTIVE"
        );
    }


    private void validateDates(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (endDate.isBefore(startDate)
                || endDate.equals(startDate)) {

            throw new IllegalArgumentException(
                    "Academic term end date must be after start date"
            );
        }
    }


    private void validateSequence(
            Integer sequenceNumber
    ) {

        if (sequenceNumber == null
                || sequenceNumber <= 0) {

            throw new IllegalArgumentException(
                    "Academic term sequence number must be greater than zero"
            );
        }
    }

}
