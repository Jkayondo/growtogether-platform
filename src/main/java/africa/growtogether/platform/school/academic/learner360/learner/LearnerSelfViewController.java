package africa.growtogether.platform.school.academic.learner360.learner;

import africa.growtogether.platform.school.learner.security.AuthenticatedLearnerResolver;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * L05C_AUTHENTICATED_LEARNER_SELF_RESOLUTION
 *
 * Tenant and learner identity are resolved server-side.
 * The caller cannot nominate another learner identifier.
 */
@RestController
@RequestMapping("/api/school/learner/intelligence")
public class LearnerSelfViewController {

    private final LearnerSelfViewService service;
    private final AuthenticatedLearnerResolver learners;

    public LearnerSelfViewController(
            LearnerSelfViewService service,
            AuthenticatedLearnerResolver learners
    ) {
        this.service = service;
        this.learners = learners;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public LearnerSelfView getLearnerView() {

        var learner =
                learners.requireCurrentLearner();

        return service.getLearnerView(
                learner.tenantId(),
                learner.student().getId()
        );
    }
}
