package africa.growtogether.platform.school.academic.year;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Service
public class AcademicYearService {


    private final AcademicYearRepository repository;


    public AcademicYearService(
            AcademicYearRepository repository
    ) {

        this.repository = repository;

    }


    @Transactional
    public AcademicYear create(
            UUID tenantId,
            CreateAcademicYearCommand command
    ) {


        validateDates(
                command.startDate(),
                command.endDate()
        );


        AcademicYear year =
                new AcademicYear(
                        tenantId,
                        command.academicYearCode(),
                        command.academicYearName(),
                        command.startDate(),
                        command.endDate()
                );


        return repository.save(year);

    }


    @Transactional(readOnly = true)
    public List<AcademicYear> findByTenant(
            UUID tenantId
    ) {

        return repository.findByTenantId(
                tenantId
        );

    }


    private void validateDates(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (endDate.isBefore(startDate)
                || endDate.equals(startDate)) {

            throw new IllegalArgumentException(
                    "Academic year end date must be after start date"
            );

        }

    }

}