package africa.growtogether.platform.school.timetable.generation;

import africa.growtogether.platform.eaif.AiEnums;
import africa.growtogether.platform.eaif.AiFoundationService;
import africa.growtogether.platform.eaif.AiRequest;

import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;
import africa.growtogether.platform.school.academic.year.AcademicYear;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;

import africa.growtogether.platform.school.timetable.bell.BellSchedule;
import africa.growtogether.platform.school.timetable.bell.BellScheduleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.time.LocalDate;

import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class TimetableGenerationService {

    private static final String AI_SOURCE_SERVICE =
            "GT_SCHOOL_TIMETABLE";

    private static final String AI_USE_CASE =
            "TIMETABLE_GENERATION";

    private final TimetableGenerationRequestRepository repository;

    private final AcademicYearRepository academicYears;

    private final AcademicTermRepository academicTerms;

    private final CampusRepository campuses;

    private final BellScheduleRepository bellSchedules;

    private final AiFoundationService aiFoundation;

    public TimetableGenerationService(
            TimetableGenerationRequestRepository repository,
            AcademicYearRepository academicYears,
            AcademicTermRepository academicTerms,
            CampusRepository campuses,
            BellScheduleRepository bellSchedules,
            AiFoundationService aiFoundation
    ) {

        this.repository = repository;
        this.academicYears = academicYears;
        this.academicTerms = academicTerms;
        this.campuses = campuses;
        this.bellSchedules = bellSchedules;
        this.aiFoundation = aiFoundation;
    }

    @Transactional
    public TimetableGenerationRequest create(
            UUID tenantId,
            CreateTimetableGenerationRequestCommand command
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (command == null) {
            throw new IllegalArgumentException(
                    "command must not be null"
            );
        }

        String generationCode =
                requireText(
                        command.generationCode(),
                        "generationCode"
                ).toUpperCase(
                        Locale.ROOT
                );

        if (
                repository.existsByTenantIdAndGenerationCode(
                        tenantId,
                        generationCode
                )
        ) {
            throw new IllegalArgumentException(
                    "Timetable generation code already exists for tenant"
            );
        }

        AcademicYear academicYear =
                academicYears
                        .findByTenantIdAndId(
                                tenantId,
                                command.academicYearId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Academic year not found for tenant"
                                )
                        );

        validateEffectiveDatesAgainstAcademicYear(
                academicYear,
                command.effectiveFrom(),
                command.effectiveTo()
        );

        AcademicTerm term = null;

        if (command.academicTermId() != null) {

            term =
                    academicTerms
                            .findByTenantIdAndId(
                                    tenantId,
                                    command.academicTermId()
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Academic term not found for tenant"
                                    )
                            );

            if (
                    term.getAcademicYear() == null
                    || !command.academicYearId().equals(
                            term.getAcademicYear().getId()
                    )
            ) {
                throw new IllegalArgumentException(
                        "Academic term does not belong to academic year"
                );
            }

            validateEffectiveDatesAgainstTerm(
                    term,
                    command.effectiveFrom(),
                    command.effectiveTo()
            );
        }

        campuses
                .findByTenantIdAndId(
                        tenantId,
                        command.campusId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Campus not found for tenant"
                        )
                );

        BellSchedule bellSchedule =
                bellSchedules
                        .findByTenantIdAndId(
                                tenantId,
                                command.bellScheduleId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Bell schedule not found for tenant"
                                )
                        );

        if (
                !command.campusId().equals(
                        bellSchedule.getCampusId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Bell schedule does not belong to campus"
            );
        }

        validateEffectiveDatesAgainstBellSchedule(
                bellSchedule,
                command.effectiveFrom(),
                command.effectiveTo()
        );

        TimetableGenerationRequest request =
                new TimetableGenerationRequest(
                        generationCode,
                        command.academicYearId(),
                        command.academicTermId(),
                        command.campusId(),
                        command.bellScheduleId(),
                        command.timetableType(),
                        command.effectiveFrom(),
                        command.effectiveTo(),
                        command.generationMode(),
                        command.modelCode(),
                        command.objectives(),
                        command.requestedBy()
                );

        request.setTenantId(
                tenantId
        );

        /*
         * Persist first so the generation request has a stable ID.
         * The enclosing transaction still provides atomicity with
         * the EAIF submission below.
         */
        request =
                repository.save(
                        request
                );

        if (
                "AI_ASSISTED".equals(
                        request.getGenerationMode()
                )
        ) {

            AiRequest aiRequest =
                    aiFoundation.submit(
                            tenantId,
                            AI_SOURCE_SERVICE,
                            AI_USE_CASE,
                            request.getModelCode(),
                            hashInput(
                                    tenantId,
                                    request
                            ),
                            AiEnums.RiskLevel.MEDIUM,
                            request.getId().toString()
                    );

            request.linkEaifRequest(
                    aiRequest.getId()
            );
        }

        request.markReady();

        return repository.save(
                request
        );
    }

    @Transactional(readOnly = true)
    public TimetableGenerationRequest get(
            UUID tenantId,
            UUID requestId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (requestId == null) {
            throw new IllegalArgumentException(
                    "requestId must not be null"
            );
        }

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        requestId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Timetable generation request not found"
                        )
                );
    }

    private void validateEffectiveDatesAgainstAcademicYear(
            AcademicYear academicYear,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        if (
                academicYear.getStartDate() != null
                && effectiveFrom != null
                && effectiveFrom.isBefore(
                        academicYear.getStartDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Generation effectiveFrom is before academic year"
            );
        }

        if (
                academicYear.getEndDate() != null
                && effectiveFrom != null
                && effectiveFrom.isAfter(
                        academicYear.getEndDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Generation effectiveFrom is after academic year"
            );
        }

        if (
                academicYear.getEndDate() != null
                && effectiveTo != null
                && effectiveTo.isAfter(
                        academicYear.getEndDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Generation effectiveTo is after academic year"
            );
        }
    }


    private void validateEffectiveDatesAgainstTerm(
            AcademicTerm term,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        if (
                effectiveFrom != null
                && effectiveFrom.isBefore(
                        term.getStartDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Generation effectiveFrom is before academic term"
            );
        }

        if (
                effectiveFrom != null
                && effectiveFrom.isAfter(
                        term.getEndDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Generation effectiveFrom is after academic term"
            );
        }

        if (
                effectiveTo != null
                && effectiveTo.isAfter(
                        term.getEndDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Generation effectiveTo is after academic term"
            );
        }
    }

    private void validateEffectiveDatesAgainstBellSchedule(
            BellSchedule schedule,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        if (
                effectiveFrom != null
                && effectiveFrom.isBefore(
                        schedule.getEffectiveFrom()
                )
        ) {
            throw new IllegalArgumentException(
                    "Generation effectiveFrom is before bell schedule"
            );
        }

        if (
                schedule.getEffectiveTo() != null
                && effectiveFrom != null
                && effectiveFrom.isAfter(
                        schedule.getEffectiveTo()
                )
        ) {
            throw new IllegalArgumentException(
                    "Generation effectiveFrom is after bell schedule"
            );
        }

        if (
                schedule.getEffectiveTo() != null
                && effectiveTo != null
                && effectiveTo.isAfter(
                        schedule.getEffectiveTo()
                )
        ) {
            throw new IllegalArgumentException(
                    "Generation effectiveTo is after bell schedule"
            );
        }
    }

    private String hashInput(
            UUID tenantId,
            TimetableGenerationRequest request
    ) {

        String value =
                tenantId
                        + "|"
                        + request.getGenerationCode()
                        + "|"
                        + request.getAcademicYearId()
                        + "|"
                        + nullable(
                                request.getAcademicTermId()
                        )
                        + "|"
                        + request.getCampusId()
                        + "|"
                        + request.getBellScheduleId()
                        + "|"
                        + request.getTimetableType()
                        + "|"
                        + request.getEffectiveFrom()
                        + "|"
                        + nullable(
                                request.getEffectiveTo()
                        )
                        + "|"
                        + request.getGenerationMode()
                        + "|"
                        + nullable(
                                request.getModelCode()
                        )
                        + "|"
                        + nullable(
                                request.getObjectives()
                        );

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            return HexFormat
                    .of()
                    .formatHex(
                            digest.digest(
                                    value.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            )
                    );

        } catch (NoSuchAlgorithmException ex) {

            throw new IllegalStateException(
                    "SHA-256 is not available",
                    ex
            );
        }
    }

    private static String nullable(
            Object value
    ) {

        return value == null
                ? ""
                : value.toString();
    }

    private static String requireText(
            String value,
            String field
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value.trim();
    }
}
