package africa.growtogether.platform.school.assessment.moderation;


import africa.growtogether.platform.school.assessment.marking.MarkSheet;
import africa.growtogether.platform.school.assessment.marking.MarkSheetService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@Transactional
public class MarkModerationService {


    private final MarkSheetService markSheetService;


    public MarkModerationService(
            MarkSheetService markSheetService
    ) {

        this.markSheetService =
                markSheetService;
    }


    public MarkSheet submitForModeration(
            UUID tenantId,
            UUID markSheetId,
            UUID actorId
    ) {

        return markSheetService.submit(
                tenantId,
                markSheetId,
                actorId
        );
    }


    public MarkSheet startModeration(
            UUID tenantId,
            UUID markSheetId
    ) {

        return markSheetService.startModeration(
                tenantId,
                markSheetId
        );
    }


    public MarkSheet approveModeration(
            UUID tenantId,
            UUID markSheetId
    ) {

        return markSheetService.approveModeration(
                tenantId,
                markSheetId
        );
    }


    public MarkSheet returnForCorrection(
            UUID tenantId,
            UUID markSheetId
    ) {

        return markSheetService.returnForCorrection(
                tenantId,
                markSheetId
        );
    }
}
