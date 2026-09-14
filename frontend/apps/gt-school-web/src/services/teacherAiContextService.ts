import {
  loadMyActiveTeachingAssignments
} from "./teachingAssignmentService";

import type {
  TeachingAssignment
} from "../types/teachingAssignment";

import type {
  TeacherAiAssignmentContext
} from "../types/teacherAi";

export function toTeacherAiAssignmentContext(
  assignment: TeachingAssignment
): TeacherAiAssignmentContext {
  return {
    assignmentId: assignment.id,
    teacherProfileId: assignment.teacherProfileId,
    classGradeId: assignment.classGradeId,
    subjectId: assignment.subjectId,
    weeklyPeriods: assignment.weeklyPeriods,
    assignmentStatus: assignment.assignmentStatus
  };
}

export async function loadTeacherAiAssignmentContexts()
  : Promise<TeacherAiAssignmentContext[]> {
  const assignments =
    await loadMyActiveTeachingAssignments();

  return assignments.map(
    toTeacherAiAssignmentContext
  );
}

export async function requireTeacherAiAssignmentContext(
  assignmentId: string
): Promise<TeacherAiAssignmentContext> {
  const id = assignmentId.trim();

  if (!id) {
    throw new Error(
      "A teaching assignment is required."
    );
  }

  const contexts =
    await loadTeacherAiAssignmentContexts();

  const context =
    contexts.find(
      item => item.assignmentId === id
    );

  if (!context) {
    throw new Error(
      "Active teaching assignment not found."
    );
  }

  return context;
}
