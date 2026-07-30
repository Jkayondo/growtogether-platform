package africa.growtogether.platform.school.parent.security;


public class ParentEngagementPermission {


    private final ParentEngagementAccessRole role;


    public ParentEngagementPermission(
            ParentEngagementAccessRole role
    ) {

        this.role = role;
    }


    public boolean canViewSchoolAnalytics() {

        return role ==
                ParentEngagementAccessRole.SCHOOL_ADMIN;
    }


    public boolean canViewParentHistory() {

        return role ==
                ParentEngagementAccessRole.PARENT;
    }


    public boolean canViewClassAnalytics() {

        return role ==
                ParentEngagementAccessRole.TEACHER;
    }


    public ParentEngagementAccessRole getRole() {
        return role;
    }
}
