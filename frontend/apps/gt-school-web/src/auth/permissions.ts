export const Permission = {

  VIEW_DASHBOARD:
    "VIEW_DASHBOARD",

  MANAGE_LEARNERS:
    "MANAGE_LEARNERS",

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

  TEACHING_ASSIGNMENT_READ:
    "school.academic.teaching-assignment.read",

  TEACHER_PROGRAMME_READ:
    "school.teacher.programme.read",

} as const;


export type Permission =
  typeof Permission[keyof typeof Permission];
