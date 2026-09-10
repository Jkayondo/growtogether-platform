package africa.growtogether.platform.school.results.security;


import org.springframework.stereotype.Service;


import java.util.UUID;


@Service
public class AcademicResultAccessGuard {


    public void validateParentAccess(

            UUID parentUserId,

            UUID learnerId

    ){


        /*
         * Integration point with GT IAM / Relationship Service.
         *
         * Future validation:
         *
         * parentUserId
         *        |
         *        |
         * guardian_learner_relationship
         *        |
         *        |
         * learnerId
         *
         */


        if(parentUserId == null){

            throw new SecurityException(
                    "Parent identity required"
            );

        }


        if(learnerId == null){

            throw new SecurityException(
                    "Learner identity required"
            );

        }


    }


}
