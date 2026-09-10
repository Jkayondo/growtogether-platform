package africa.growtogether.platform.school.results;


import africa.growtogether.platform.school.results.publication.ResultPublication;
import africa.growtogether.platform.school.results.publication.ResultPublicationRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class AcademicResultPublicationIntegrationTest {


    @Autowired
    private ResultPublicationRepository repository;



    @Test
    void resultPublicationLifecycleCanBeControlled(){


        ResultPublication publication =
                new ResultPublication(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "TERM_RESULT"
                );


        publication.publish(
                UUID.randomUUID()
        );


        assertEquals(
                "PUBLISHED",
                publication.getPublicationStatus()
        );

    }


}
