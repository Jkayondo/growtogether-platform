import type {
  Permission as PermissionType
} from "./permissions";

import {
  Permission
} from "./permissions";

import {
  Role
} from "./roles";


export const rolePermissions: Record<string, PermissionType[]> = {


  [Role.SCHOOL_ADMIN]: [

    Permission.VIEW_DASHBOARD,

    Permission.MANAGE_LEARNERS,

    Permission.MANAGE_ATTENDANCE,

    Permission.MANAGE_FEES,

    Permission.VIEW_REPORTS,

    Permission.MANAGE_USERS

  ],



  [Role.TEACHER]: [

    Permission.VIEW_DASHBOARD,

    Permission.MANAGE_LEARNERS,

    Permission.MANAGE_ATTENDANCE

  ],



  [Role.FINANCE_OFFICER]: [

    Permission.VIEW_DASHBOARD,

    Permission.MANAGE_FEES,

    Permission.VIEW_REPORTS

  ],



  [Role.PARENT]: [

    Permission.VIEW_DASHBOARD

  ],



  [Role.LEARNER]: [

    Permission.VIEW_DASHBOARD

  ]

};
