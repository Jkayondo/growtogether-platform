package africa.growtogether.platform.school.parent.security;


import org.springframework.stereotype.Service;


@Service
public class ParentEngagementAuthorizationService {


    public ParentEngagementPermission authorize(
            ParentEngagementAccessRole role
    ) {

        return new ParentEngagementPermission(
                role
        );
    }
}
