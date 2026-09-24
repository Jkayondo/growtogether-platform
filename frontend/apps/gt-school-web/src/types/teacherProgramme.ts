export interface TeacherProgrammeLessonDetail {
  timetableId: string;
  bellPeriodId: string;
  periodCode: string;
  periodName: string;
  sequenceNumber: number;
  startTime: string;
  endTime: string;
  classGradeId: string;
  classCode: string;
  className: string;
  streamId: string | null;
  streamCode: string | null;
  streamName: string | null;
  subjectOfferingId: string;
  subjectId: string;
  subjectCode: string;
  subjectName: string;
  activityName: string | null;
}

export interface TeacherProgrammeCalendarEvent {
  id: string;
  eventCode: string;
  eventName: string;
  eventType: string;
  startAt: string;
  endAt: string | null;
  eventStatus: string;
}

export interface TeacherProgrammeToday {
  date: string;
  zone: string;
  lessons: TeacherProgrammeLessonDetail[];
  calendarEvents: TeacherProgrammeCalendarEvent[];
}
