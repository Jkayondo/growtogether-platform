package africa.growtogether.platform.school.student.identity;

import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;


@Service
public class DefaultLearnerIdentityGenerator
        implements LearnerIdentityGenerator {


    private final AtomicLong permanentSequence =
            new AtomicLong(1);


    private final AtomicLong studentSequence =
            new AtomicLong(1);



    @Override
    public String generatePermanentLearnerNumber() {

        long number =
                permanentSequence.getAndIncrement();


        return String.format(
                "GT-LRN-%09d",
                number
        );
    }



    @Override
    public String generateStudentNumber(
            UUID tenantId,
            String schoolCode,
            int admissionYear
    ) {

        long number =
                studentSequence.getAndIncrement();


        return String.format(
                "%s-%d-%03d",
                schoolCode,
                admissionYear,
                number
        );
    }

}
