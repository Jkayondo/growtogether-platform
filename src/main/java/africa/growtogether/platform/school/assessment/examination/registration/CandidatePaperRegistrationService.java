package africa.growtogether.platform.school.assessment.examination.registration;

import africa.growtogether.platform.school.assessment.examination.candidate.ExaminationCandidate;
import africa.growtogether.platform.school.assessment.examination.candidate.ExaminationCandidateRepository;
import africa.growtogether.platform.school.assessment.examination.paper.AssessmentPaper;
import africa.growtogether.platform.school.assessment.examination.paper.AssessmentPaperRepository;
import africa.growtogether.platform.school.assessment.examination.schedule.ExaminationSchedule;
import africa.growtogether.platform.school.assessment.examination.schedule.ExaminationScheduleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;


@Service
@Transactional
public class CandidatePaperRegistrationService {


    private static final Set<String>
            SUPPORTED_REGISTRATION_TYPES =
            Set.of(
                    "STANDARD",
                    "OPTIONAL",
                    "REPEAT",
                    "SUPPLEMENTARY",
                    "SPECIAL",
                    "EXEMPTED"
            );


    private final CandidatePaperRegistrationRepository repository;
    private final ExaminationCandidateRepository candidateRepository;
    private final AssessmentPaperRepository paperRepository;
    private final ExaminationScheduleRepository scheduleRepository;


    public CandidatePaperRegistrationService(
            CandidatePaperRegistrationRepository repository,
            ExaminationCandidateRepository candidateRepository,
            AssessmentPaperRepository paperRepository,
            ExaminationScheduleRepository scheduleRepository
    ) {

        this.repository =
                repository;

        this.candidateRepository =
                candidateRepository;

        this.paperRepository =
                paperRepository;

        this.scheduleRepository =
                scheduleRepository;
    }


    public CandidatePaperRegistration register(
            UUID tenantId,
            UUID registeredBy,
            CreateCandidatePaperRegistrationCommand command
    ) {

        requireId(
                tenantId,
                "tenantId"
        );

        requireId(
                registeredBy,
                "registeredBy"
        );

        if (command == null) {
            throw new IllegalArgumentException(
                    "Candidate paper registration command is required"
            );
        }


        UUID examinationCandidateId =
                requireId(
                        command.examinationCandidateId(),
                        "examinationCandidateId"
                );

        UUID assessmentPaperId =
                requireId(
                        command.assessmentPaperId(),
                        "assessmentPaperId"
                );


        String registrationType =
                normalizeRegistrationType(
                        command.registrationType()
                );


        ExaminationCandidate candidate =
                candidateRepository
                        .findByTenantIdAndId(
                                tenantId,
                                examinationCandidateId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Examination candidate not found for tenant"
                                        )
                        );


        AssessmentPaper paper =
                paperRepository
                        .findByTenantIdAndId(
                                tenantId,
                                assessmentPaperId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Assessment paper not found for tenant"
                                        )
                        );


        UUID examinationSessionId =
                candidate.getExaminationSessionId();


        if (
                paper.getExaminationSessionId() == null
                || !examinationSessionId.equals(
                        paper.getExaminationSessionId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Assessment paper does not belong to the candidate examination session"
            );
        }


        UUID examinationScheduleId =
                command.examinationScheduleId();


        if (examinationScheduleId != null) {

            ExaminationSchedule schedule =
                    scheduleRepository
                            .findByTenantIdAndId(
                                    tenantId,
                                    examinationScheduleId
                            )
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "Examination schedule not found for tenant"
                                            )
                            );


            if (!assessmentPaperId.equals(
                    schedule.getAssessmentPaperId()
            )) {

                throw new IllegalArgumentException(
                        "Examination schedule does not belong to the assessment paper"
                );
            }


            if (!examinationSessionId.equals(
                    schedule.getExaminationSessionId()
            )) {

                throw new IllegalArgumentException(
                        "Examination schedule does not belong to the candidate examination session"
                );
            }
        }


        if (
                repository
                        .existsByTenantIdAndExaminationCandidateIdAndAssessmentPaperId(
                                tenantId,
                                examinationCandidateId,
                                assessmentPaperId
                        )
        ) {

            throw new IllegalArgumentException(
                    "Candidate paper registration already exists"
            );
        }


        CandidatePaperRegistration registration =
                new CandidatePaperRegistration(
                        examinationCandidateId,
                        assessmentPaperId,
                        examinationScheduleId,
                        registrationType,
                        registeredBy
                );


        registration.setTenantId(
                tenantId
        );


        return repository.save(
                registration
        );
    }


    @Transactional(readOnly = true)
    public CandidatePaperRegistration get(
            UUID tenantId,
            UUID examinationCandidateId,
            UUID assessmentPaperId
    ) {

        requireId(
                tenantId,
                "tenantId"
        );

        requireId(
                examinationCandidateId,
                "examinationCandidateId"
        );

        requireId(
                assessmentPaperId,
                "assessmentPaperId"
        );


        return repository
                .findByTenantIdAndExaminationCandidateIdAndAssessmentPaperId(
                        tenantId,
                        examinationCandidateId,
                        assessmentPaperId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Candidate paper registration not found"
                                )
                );
    }


    public CandidatePaperRegistration verify(
            UUID tenantId,
            UUID examinationCandidateId,
            UUID assessmentPaperId
    ) {

        CandidatePaperRegistration registration =
                get(
                        tenantId,
                        examinationCandidateId,
                        assessmentPaperId
                );


        registration.verify();


        return registration;
    }


    private String normalizeRegistrationType(
            String value
    ) {

        String normalized =
                value == null
                || value.isBlank()
                        ? "STANDARD"
                        : value.trim().toUpperCase();


        if (!SUPPORTED_REGISTRATION_TYPES.contains(
                normalized
        )) {

            throw new IllegalArgumentException(
                    "Unsupported candidate paper registration type"
            );
        }


        return normalized;
    }


    private UUID requireId(
            UUID value,
            String field
    ) {

        if (value == null) {

            throw new IllegalArgumentException(
                    field + " is required"
            );
        }


        return value;
    }

}
