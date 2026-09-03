export interface SchoolSnapshot {

  totalLearners: number;

  visitorsToday: number;

  totalStaff: number;

}


export interface AttendanceSummary {

  present: number;

  absent: number;

  lateArrivals: number;

  boys: number;

  girls: number;

  attendanceRate: number;

}


export interface PaymentSummary {

  amountPaid: number;

  amountPending: number;

  collectionRate: number;

}


export interface NotificationItem {

  title: string;

  message: string;

  type: "warning" | "info" | "success";

}


export interface DashboardData {

  snapshot: SchoolSnapshot;

  attendance: AttendanceSummary;

  payment: PaymentSummary;

  notifications: NotificationItem[];

  activities: ActivityItem[];

}

export interface ActivityItem {

  title: string;

  description: string;

  time: string;

}