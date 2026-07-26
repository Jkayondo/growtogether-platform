package africa.growtogether.platform.school.attendance;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


public interface StudentAttendanceRepository
        extends JpaRepository<StudentAttendance, UUID> {


    List<StudentAttendance> findByStudentId(
            UUID studentId
    );


    List<StudentAttendance> findByStudentIdAndAttendanceStatus(
            UUID studentId,
            String attendanceStatus
    );

}
