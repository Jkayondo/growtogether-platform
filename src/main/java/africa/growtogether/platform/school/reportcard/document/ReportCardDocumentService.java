package africa.growtogether.platform.school.reportcard.document;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class ReportCardDocumentService {


    private final ReportCardDocumentRepository repository;


    public ReportCardDocumentService(
            ReportCardDocumentRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public ReportCardDocument create(
            UUID tenantId,
            UUID reportCardId,
            String documentReference
    ) {

        ReportCardDocument document =
                new ReportCardDocument(
                        tenantId,
                        reportCardId,
                        documentReference
                );


        return repository.save(document);
    }


    @Transactional
    public ReportCardDocument approve(
            ReportCardDocument document
    ) {

        document.approve();

        return repository.save(document);
    }


    @Transactional
    public ReportCardDocument release(
            ReportCardDocument document
    ) {

        document.release();

        return repository.save(document);
    }


    @Transactional(readOnly = true)
    public List<ReportCardDocument> getByReportCard(
            UUID reportCardId
    ) {

        return repository.findByReportCardId(
                reportCardId
        );
    }


    @Transactional(readOnly = true)
    public List<ReportCardDocument> getByTenant(
            UUID tenantId
    ) {

        return repository.findByTenantId(
                tenantId
        );
    }
}
