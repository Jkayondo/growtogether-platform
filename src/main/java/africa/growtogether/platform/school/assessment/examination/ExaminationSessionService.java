package africa.growtogether.platform.school.assessment.examination;

import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class ExaminationSessionService {

    private static final Set<String> EXAMINATION_TYPES = Set.of(
            "INTERNAL",
            "END_OF_TERM",
            "MID_TERM",
            "MOCK",
            "NATIONAL",
            "INTERNATIONAL",
            "EXTERNAL",
            "SUPPLEMENTARY",
            "SPECIAL",
            "ENTRANCE",
            "PLACEMENT",
            "OTHER"
    );

    private final ExaminationSessionRepository repository;
    private final AcademicYearRepository academicYears;
    private final AcademicTermRepository academicTerms;
    private final CampusRepository campuses;


    public ExaminationSessionService(
            ExaminationSessionRepository repository,
            AcademicYearRepository academicYears,
            AcademicTermRepository academicTerms,
            CampusRepository campuses
    ) {
        this.repository = repository;
        this.academicYears = academicYears;
        this.academicTerms = academicTerms;
        this.campuses = campuses;
    }


    public ExaminationSession create(
            UUID tenantId,
            CreateExaminationSessionCommand command
    ) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(command, "command must not be null");

        validate(command);

        if (repository.existsByTenantIdAndSessionCode(
                tenantId,
                command.sessionCode()
        )) {
            throw new IllegalArgumentException(
                    "Examination session code already exists."
            );
        }

        academicYears.findByTenantIdAndId(
                tenantId,
                command.academicYearId()
        ).orElseThrow(
                () -> new IllegalArgumentException(
                        "Academic year not found."
                )
        );

        if (command.academicTermId() != null) {
            academicTerms.findByTenantIdAndId(
                    tenantId,
                    command.academicTermId()
            ).orElseThrow(
                    () -> new IllegalArgumentException(
                            "Academic term not found."
                    )
            );
        }

        campuses.findByTenantIdAndId(
                tenantId,
                command.campusId()
        ).orElseThrow(
                () -> new IllegalArgumentException(
                        "Campus not found."
                )
        );

        ExaminationSession session = new ExaminationSession(
                command.sessionCode(),
                command.sessionName(),
                command.description(),
                command.academicYearId(),
                command.academicTermId(),
                command.campusId(),
                command.examinationType(),
                command.startDate(),
                command.endDate(),
                command.registrationOpenDate(),
                command.registrationCloseDate(),
                command.externalAuthority(),
                command.externalSessionReference(),
                command.workflowInstanceId()
        );

        session.setTenantId(tenantId);

        return repository.save(session);
    }


    @Transactional(readOnly = true)
    public ExaminationSession get(
            UUID tenantId,
            UUID id
    ) {
        return repository.findByTenantIdAndId(
                tenantId,
                id
        ).orElseThrow(
                () -> new IllegalArgumentException(
                        "Examination session not found."
                )
        );
    }


    @Transactional(readOnly = true)
    public ExaminationSession getByCode(
            UUID tenantId,
            String sessionCode
    ) {
        return repository.findByTenantIdAndSessionCode(
                tenantId,
                sessionCode
        ).orElseThrow(
                () -> new IllegalArgumentException(
                        "Examination session not found."
                )
        );
    }


    public ExaminationSession approve(
            UUID tenantId,
            UUID id,
            UUID approvedBy
    ) {
        ExaminationSession session = get(tenantId, id);
        session.approve(approvedBy);
        return session;
    }


    public ExaminationSession openRegistration(
            UUID tenantId,
            UUID id
    ) {
        ExaminationSession session = get(tenantId, id);
        session.openRegistration();
        return session;
    }


    public ExaminationSession activate(
            UUID tenantId,
            UUID id
    ) {
        ExaminationSession session = get(tenantId, id);
        session.activate();
        return session;
    }


    public ExaminationSession complete(
            UUID tenantId,
            UUID id
    ) {
        ExaminationSession session = get(tenantId, id);
        session.complete();
        return session;
    }


    private void validate(
            CreateExaminationSessionCommand command
    ) {
        if (!EXAMINATION_TYPES.contains(command.examinationType())) {
            throw new IllegalArgumentException(
                    "Unsupported examination type."
            );
        }

        LocalDate start = command.startDate();
        LocalDate end = command.endDate();

        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException(
                    "Examination session end date cannot be before start date."
            );
        }

        LocalDate registrationOpen =
                command.registrationOpenDate();

        LocalDate registrationClose =
                command.registrationCloseDate();

        if (registrationOpen != null
                && registrationClose != null
                && registrationClose.isBefore(registrationOpen)) {
            throw new IllegalArgumentException(
                    "Registration close date cannot be before registration open date."
            );
        }
    }
}
