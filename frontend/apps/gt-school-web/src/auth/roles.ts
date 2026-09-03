export const Role = {

  SCHOOL_ADMIN:
    "SCHOOL_ADMIN",

  TEACHER:
    "TEACHER",

  FINANCE_OFFICER:
    "FINANCE_OFFICER",

  PARENT:
    "PARENT",

  LEARNER:
    "LEARNER"

} as const;


export type Role =
  typeof Role[keyof typeof Role];
