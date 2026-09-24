export type LeadershipLearnerSummary = {
  activeLearnerRecords: number;
  activeEnrollments: number;
};

export type LeadershipTeacherSummary = {
  activeTeacherProfiles: number;
  activeTeachingAssignments: number;
};

export type LeadershipAttendanceSummary = {
  attendanceDate: string;
  sessionType: string;
  sessionCount: number;
  expectedStudentCount: number;
  recordedAttendanceCount: number;
  unrecordedCount: number;
  presentCount: number;
  absentCount: number;
  lateCount: number;
  excusedAbsenceCount: number;
  unexcusedAbsenceCount: number;
  medicalAbsenceCount: number;
  schoolActivityCount: number;
  remoteLearningCount: number;
  earlyDepartureCount: number;
  suspendedCount: number;
  notRequiredCount: number;
  unknownCount: number;
  registerStarted: boolean;
  fullyRecorded: boolean;
};


export type LeadershipCapabilityStatus =
  | "AVAILABLE"
  | "PENDING_AGGREGATION";

export interface LeadershipCapabilityState {
  code: string;
  status: LeadershipCapabilityStatus;
  source: string;
}

export interface LeadershipCoverageSummary {
  totalItems: number;
  notStarted: number;
  inProgress: number;
  completed: number;
  requiresRemediation: number;
  aheadOfSchedule: number;
}

export interface LeadershipParentEngagementSummary {
  totalNotifications: number;
  deliveredNotifications: number;
  viewedNotifications: number;
  acknowledgedNotifications: number;
}

export interface LeadershipOverviewData {
  tenantId: string;
  asOf: string;
  eventWindowEnd: string;
  activeVisitors: number;
  upcomingEvents: number;
  coverage: LeadershipCoverageSummary;
  parentEngagement: LeadershipParentEngagementSummary;
  learners: LeadershipLearnerSummary;
  teachers: LeadershipTeacherSummary;
  attendance: LeadershipAttendanceSummary;
  capabilities: LeadershipCapabilityState[];
}
