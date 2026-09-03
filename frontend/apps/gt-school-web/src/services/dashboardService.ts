import { getDashboardFromApi } from "./dashboardApi";


import type {
  SchoolSnapshot,
  AttendanceSummary,
  PaymentSummary,
  DashboardData,
  NotificationItem,
  ActivityItem
} from "../types/dashboard";


export const getSchoolSnapshot = (): SchoolSnapshot => {

  return {
    totalLearners: 1250,
    visitorsToday: 36,
    totalStaff: 85
  };

};


export const getAttendanceSummary = (): AttendanceSummary => {

  return {
    present: 1185,
    absent: 65,
    lateArrivals: 12,
    boys: 600,
    girls: 585,
    attendanceRate: 94.8
  };

};


export const getPaymentSummary = (): PaymentSummary => {

  return {
    amountPaid: 1085000000,
    amountPending: 415000000,
    collectionRate: 72.4
  };

};


export const getDashboardData = (): DashboardData => {

  return {
    snapshot: getSchoolSnapshot(),

    attendance: getAttendanceSummary(),

    payment: getPaymentSummary(),

    notifications: getNotifications(),

    activities: getActivities()
  };

};

export const getNotifications = (): NotificationItem[] => {

  return [
    {
      title: "Outstanding Fees",
      message: "45 learners have pending balances",
      type: "warning",
    },
    {
      title: "Attendance Reminder",
      message: "P4 Blue attendance has not been submitted",
      type: "warning",
    },
    {
      title: "Parent Meeting",
      message: "Parents meeting scheduled tomorrow",
      type: "info",
    },
  ];

};

export const getActivities = (): ActivityItem[] => {

  return [
    {
      title: "Attendance completed",
      description: "P5A - 42 learners marked present",
      time: "10 minutes ago",
    },
    {
      title: "Payment received",
      description: "Sarah paid UGX 500,000",
      time: "25 minutes ago",
    },
    {
      title: "Visitor registered",
      description: "Parent meeting visitor checked in",
      time: "1 hour ago",
    },
  ];

};

export const fetchDashboardData = async (): Promise<DashboardData> => {

  return getDashboardFromApi();

};
