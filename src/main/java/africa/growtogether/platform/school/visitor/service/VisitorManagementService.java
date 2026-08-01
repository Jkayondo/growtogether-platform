package africa.growtogether.platform.school.visitor.service;

import africa.growtogether.platform.school.visitor.domain.SchoolVisitor;
import africa.growtogether.platform.school.visitor.repository.SchoolVisitorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class VisitorManagementService {


    private final SchoolVisitorRepository repository;


    public VisitorManagementService(
            SchoolVisitorRepository repository
    ) {
        this.repository = repository;
    }


    public SchoolVisitor registerVisitor(
            UUID tenantId,
            String firstName,
            String lastName,
            String phoneNumber,
            String email,
            String identificationType,
            String identificationReference,
            String visitorCategory
    ) {

      repository
        .findByTenantIdAndIdentificationReference(
                tenantId,
                identificationReference
        )
        .ifPresent(existing -> {
            throw new VisitorAlreadyExistsException(
                    "Visitor with this identification already exists"
            );
        });

        SchoolVisitor visitor = new SchoolVisitor(
                firstName,
                lastName,
                phoneNumber,
                email,
                identificationType,
                identificationReference,
                visitorCategory
        );

        return repository.save(visitor);
    }


    @Transactional(readOnly = true)
    public SchoolVisitor getVisitor(
            UUID tenantId,
            UUID visitorId
    ) {

        return repository.findByIdAndTenantId(
                visitorId,
                tenantId
        ).orElseThrow(
                () -> new IllegalArgumentException(
                        "Visitor not found"
                )
        );
    }


    @Transactional(readOnly = true)
    public List<SchoolVisitor> listVisitors(
            UUID tenantId
    ) {

        return repository.findAllByTenantId(
                tenantId
        );
    }


    @Transactional(readOnly = true)
    public SchoolVisitor findByIdentification(
            UUID tenantId,
            String identificationReference
    ) {

        return repository
                .findByTenantIdAndIdentificationReference(
                        tenantId,
                        identificationReference
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Visitor not found"
                        )
                );
    }
}
