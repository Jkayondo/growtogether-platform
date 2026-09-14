import {
  beforeEach,
  describe,
  expect,
  it,
  vi
} from "vitest";

const api = vi.hoisted(() => ({
  assignments: vi.fn()
}));

vi.mock(
  "./teachingAssignmentService",
  () => ({
    loadMyActiveTeachingAssignments:
      api.assignments
  })
);

import {
  loadTeacherAiAssignmentContexts,
  requireTeacherAiAssignmentContext,
  toTeacherAiAssignmentContext
} from "./teacherAiContextService";

function assignment(
  id = "assignment-1"
) {
  return {
    id,
    tenantId: "tenant-1",
    teacherProfileId: "teacher-1",
    ewfAssignmentId: null,
    academicYearId: "year-1",
    campusId: "campus-1",
    classGradeId: "class-1",
    subjectId: "subject-1",
    assignmentReference: "REF-1",
    assignmentType: "PRIMARY",
    weeklyPeriods: 5,
    workloadPercentage: 50,
    effectiveFrom: "2026-01-01",
    effectiveTo: null,
    roomReference: null,
    assignmentStatus: "ACTIVE",
    approvalStatus: "APPROVED",
    approvedBy: null,
    approvedAt: null,
    notes: null,
    status: "ACTIVE",
    createdAt: "2026-01-01T00:00:00Z",
    createdBy: "user-1",
    updatedAt: "2026-01-01T00:00:00Z",
    updatedBy: "user-1",
    version: 1
  };
}

describe(
  "teacherAiContextService",
  () => {
    beforeEach(() => {
      api.assignments.mockReset();
    });

    it(
      "maps an active teaching assignment into governed AI context",
      () => {
        expect(
          toTeacherAiAssignmentContext(
            assignment() as never
          )
        ).toEqual({
          assignmentId: "assignment-1",
          teacherProfileId: "teacher-1",
          classGradeId: "class-1",
          subjectId: "subject-1",
          weeklyPeriods: 5,
          assignmentStatus: "ACTIVE"
        });
      }
    );

    it(
      "loads all active assignment contexts",
      async () => {
        api.assignments.mockResolvedValue([
          assignment("assignment-1"),
          {
            ...assignment("assignment-2"),
            teacherProfileId: "teacher-2",
            classGradeId: "class-2",
            subjectId: "subject-2"
          }
        ]);

        const result =
          await loadTeacherAiAssignmentContexts();

        expect(result).toHaveLength(2);

        expect(result[1]).toMatchObject({
          assignmentId: "assignment-2",
          teacherProfileId: "teacher-2",
          classGradeId: "class-2",
          subjectId: "subject-2"
        });
      }
    );

    it(
      "selects a real active assignment by id",
      async () => {
        api.assignments.mockResolvedValue([
          assignment("assignment-1"),
          assignment("assignment-2")
        ]);

        await expect(
          requireTeacherAiAssignmentContext(
            "assignment-2"
          )
        ).resolves.toMatchObject({
          assignmentId: "assignment-2",
          teacherProfileId: "teacher-1"
        });
      }
    );

    it(
      "rejects missing or unavailable assignment context",
      async () => {
        api.assignments.mockResolvedValue([
          assignment()
        ]);

        await expect(
          requireTeacherAiAssignmentContext(" ")
        ).rejects.toThrow(
          "A teaching assignment is required."
        );

        await expect(
          requireTeacherAiAssignmentContext(
            "missing"
          )
        ).rejects.toThrow(
          "Active teaching assignment not found."
        );
      }
    );
  }
);
