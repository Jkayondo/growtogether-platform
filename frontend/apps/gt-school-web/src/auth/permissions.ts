export const Permission = {

  VIEW_DASHBOARD:
    "VIEW_DASHBOARD",

  MANAGE_LEARNERS:
    "MANAGE_LEARNERS",

  STUDENT_READ:
    "school.student.read",

  STUDENT_CREATE:
    "school.student.create",

  STUDENT_MANAGE:
    "school.student.manage",

  ENROLLMENT_READ:
    "school.enrollment.read",

  ENROLLMENT_CREATE:
    "school.enrollment.create",

  ENROLLMENT_MANAGE:
    "school.enrollment.manage",

  MANAGE_ATTENDANCE:
    "MANAGE_ATTENDANCE",

  MANAGE_FEES:
    "MANAGE_FEES",

  VIEW_REPORTS:
    "VIEW_REPORTS",

  MANAGE_USERS:
    "MANAGE_USERS",

  MANAGE_VISITORS:
    "MANAGE_VISITORS",

  ACADEMIC_YEAR_READ:
    "school.academic.year.read",

  ACADEMIC_YEAR_CREATE:
    "school.academic.year.create",

  CAMPUS_READ:
    "school.academic.campus.read",

  CAMPUS_CREATE:
    "school.academic.campus.create",

  CAMPUS_MANAGE:
    "school.academic.campus.manage",

  CURRICULUM_READ:
    "school.academic.curriculum.read",

  CURRICULUM_CREATE:
    "school.academic.curriculum.create",

  CURRICULUM_MANAGE:
    "school.academic.curriculum.manage",

  CURRICULUM_VERSION_READ:
    "school.academic.curriculum.version.read",

  CURRICULUM_VERSION_CREATE:
    "school.academic.curriculum.version.create",

  CURRICULUM_VERSION_MANAGE:
    "school.academic.curriculum.version.manage",

  CURRICULUM_LEARNING_AREA_READ:
    "school.academic.curriculum.learning-area.read",

  CURRICULUM_LEARNING_AREA_CREATE:
    "school.academic.curriculum.learning-area.create",

  CURRICULUM_LEARNING_AREA_MANAGE:
    "school.academic.curriculum.learning-area.manage",

  CLASS_GRADE_READ:
    "school.academic.class-grade.read",

  CLASS_GRADE_CREATE:
    "school.academic.class-grade.create",

  CLASS_GRADE_MANAGE:
    "school.academic.class-grade.manage",

  CLASS_OFFERING_READ:
    "school.academic.class-offering.read",

  CLASS_OFFERING_CREATE:
    "school.academic.class-offering.create",

  CLASS_OFFERING_MANAGE:
    "school.academic.class-offering.manage",

  SUBJECT_READ:
    "school.academic.subject.read",

  SUBJECT_CREATE:
    "school.academic.subject.create",

  SUBJECT_MANAGE:
    "school.academic.subject.manage",

  WORKFORCE_MEMBER_READ:
    "enterprise.workforce.member.read",

  WORKFORCE_MEMBER_CREATE:
    "enterprise.workforce.member.create",

  TEACHER_PROFILE_READ:
    "school.academic.teacher-profile.read",

  TEACHER_PROFILE_CREATE:
    "school.academic.teacher-profile.create",

  TEACHER_PROFILE_MANAGE:
    "school.academic.teacher-profile.manage",

  TEACHING_ASSIGNMENT_READ:
    "school.academic.teaching-assignment.read",

  TEACHER_PROGRAMME_READ:
    "school.teacher.programme.read",

  TEACHER_COVERAGE_READ:
    "school.teacher.coverage.read",

  TEACHER_COVERAGE_UPDATE:
    "school.teacher.coverage.update",

  TEACHING_ASSIGNMENT_CREATE:
    "school.academic.teaching-assignment.create",

  TEACHING_ASSIGNMENT_MANAGE:
    "school.academic.teaching-assignment.manage",

  ASSESSMENT_READ:
    "school.academic.assessment.read",

  ASSESSMENT_CREATE:
    "school.academic.assessment.create",

  ASSESSMENT_MANAGE:
    "school.academic.assessment.manage",

  TIMETABLE_READ:
    "school.timetable.read",

  TIMETABLE_CREATE:
    "school.timetable.create",

  TIMETABLE_REVIEW:
    "school.timetable.review",

  TIMETABLE_APPROVE:
    "school.timetable.approve",

  TIMETABLE_PUBLISH:
    "school.timetable.publish",

  TIMETABLE_ACTIVATE:
    "school.timetable.activate",

  TIMETABLE_SUSPEND:
    "school.timetable.suspend",

} as const;


export type Permission =
  typeof Permission[keyof typeof Permission];
