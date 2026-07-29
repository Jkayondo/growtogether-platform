package africa.growtogether.platform.school.parent;


import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;


@Service
public class ParentAcademicDashboardService {


    private final ParentAcademicViewService viewService;


    public ParentAcademicDashboardService(
            ParentAcademicViewService viewService
    ) {

        this.viewService = viewService;
    }


    public ParentAcademicDashboard loadDashboard(
            UUID parentId
    ) {


        ParentAcademicView view =
                viewService.getParentAcademicView(
                        parentId
                );


        List<ParentAcademicDashboard.LearnerDashboardItem> learners =
                view.learners()
                        .stream()
                        .map(item ->
                                new ParentAcademicDashboard.LearnerDashboardItem(
                                        item.learnerId(),
                                        item.reportCardIds().size()
                                )
                        )
                        .toList();


        return new ParentAcademicDashboard(
                parentId,
                learners.size(),
                learners
        );
    }
}
