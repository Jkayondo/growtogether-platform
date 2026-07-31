package africa.growtogether.platform.school.subject;


import africa.growtogether.platform.school.subject.dto.CreateSubjectConfigurationRequest;
import africa.growtogether.platform.school.subject.dto.SubjectConfigurationResponse;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class SubjectConfigurationService {


    private final SubjectConfigurationRepository repository;


    public SubjectConfigurationService(
            SubjectConfigurationRepository repository
    ) {

        this.repository = repository;
    }


    public SubjectConfigurationResponse create(
            UUID tenantId,
            CreateSubjectConfigurationRequest request
    ) {


        SubjectConfiguration subject =
                new SubjectConfiguration(
                        tenantId,
                        request.academicGradeId(),
                        request.subjectName(),
                        request.subjectCode(),
                        request.mandatory()
                );


        SubjectConfiguration saved =
                repository.save(subject);


        return new SubjectConfigurationResponse(
                saved.getId(),
                saved.getAcademicGradeId(),
                saved.getSubjectName(),
                saved.getSubjectCode(),
                saved.isMandatory()
        );
    }


    public List<SubjectConfigurationResponse> findByGrade(
            UUID academicGradeId
    ) {

        return repository.findByAcademicGradeId(
                academicGradeId
        )
        .stream()
        .map(subject ->
                new SubjectConfigurationResponse(
                        subject.getId(),
                        subject.getAcademicGradeId(),
                        subject.getSubjectName(),
                        subject.getSubjectCode(),
                        subject.isMandatory()
                )
        )
        .toList();
    }
}
