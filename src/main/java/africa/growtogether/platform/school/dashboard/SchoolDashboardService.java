package africa.growtogether.platform.school.dashboard;


import africa.growtogether.platform.school.academic.learner360.Learner360ProfileRepository;
import africa.growtogether.platform.school.attendance.StudentAttendanceRepository;
import africa.growtogether.platform.school.visitor.repository.VisitorCheckInRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional(readOnly = true)
public class SchoolDashboardService {


    private final Learner360ProfileRepository learners;

    private final StudentAttendanceRepository attendance;

    private final VisitorCheckInRepository visitorCheckIns;


    public SchoolDashboardService(
            Learner360ProfileRepository learners,
            StudentAttendanceRepository attendance,
            VisitorCheckInRepository visitorCheckIns
    ) {

        this.learners = learners;
        this.attendance = attendance;
        this.visitorCheckIns = visitorCheckIns;

    }


    public SchoolDashboardResponse getDashboard(
            UUID tenantId
    ) {


        long totalLearners =
                learners.countByTenantId(
                        tenantId
                );


        long present =
                attendance.countByTenantIdAndAttendanceStatus(
                        tenantId,
                        "PRESENT"
                );


        long absent =
                attendance.countByTenantIdAndAttendanceStatus(
                        tenantId,
                        "ABSENT"
                );


        long late =
                attendance.countByTenantIdAndAttendanceStatus(
                        tenantId,
                        "LATE"
                );


        long totalAttendance =
                present + absent + late;


        double attendanceRate =
                totalAttendance == 0
                        ? 0
                        : ((double) present / totalAttendance) * 100;


        long visitorsToday =
                visitorCheckIns
                        .findAllByTenantId(
                                tenantId
                        )
                        .size();


        return new SchoolDashboardResponse(

                new SchoolDashboardResponse.SchoolSnapshot(
                        totalLearners,
                        visitorsToday,
                        0
                ),


                new SchoolDashboardResponse.AttendanceSummary(
                        present,
                        absent,
                        late,
                        0,
                        0,
                        attendanceRate
                ),


                new SchoolDashboardResponse.PaymentSummary(
                        0,
                        0,
                        0
                ),


                List.of(),

                List.of()

        );

    }

}
