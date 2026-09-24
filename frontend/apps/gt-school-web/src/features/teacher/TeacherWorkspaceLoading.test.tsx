import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import TeacherWorkspace from "./TeacherWorkspace";

const api = vi.hoisted(() => ({
  assignments: vi.fn(),
  programme: vi.fn(),
  subjects: vi.fn(),
  levels: vi.fn(),
  grades: vi.fn(),
}));

vi.mock("../../services/teachingAssignmentService", () => ({
  loadMyActiveTeachingAssignments: api.assignments,
}));
vi.mock("../../services/teacherProgrammeService", () => ({
  loadMyTodayProgramme: api.programme,
}));
vi.mock("../../services/subjectService", () => ({
  loadSubjects: api.subjects,
}));
vi.mock("../../services/educationLevelService", () => ({
  loadEducationLevels: api.levels,
}));
vi.mock("../../services/classGradeService", () => ({
  loadClassGrades: api.grades,
}));

vi.mock("../../auth/authContext", () => ({
  useAuth: () => ({
    hasPermission: () => true
  })
}));

vi.mock("../../services/teacherCoverageService", () => ({
  loadMyTeacherCoverage: async () => ({
    teacherProfileId: "teacher-regression",
    teachingAssignmentId: null,
    summary: {
      total: 0,
      notStarted: 0,
      inProgress: 0,
      completed: 0,
      requiresRemediation: 0,
      aheadOfSchedule: 0
    },
    items: []
  }),

  setMyTeacherCoverageInProgress:
    async () => undefined,

  completeMyCoverage:
    async () => undefined,

  setMyTeacherCoverageRequiresRemediation:
    async () => undefined,

  setMyTeacherCoverageAheadOfSchedule:
    async () => undefined
}));

function assignment(id: string, classGradeId: string) {
  return {
    id,
    classGradeId,
    subjectId: "subject-1",
    weeklyPeriods: 5,
    assignmentStatus: "ACTIVE",
  };
}

async function finishLoading() {
  await waitFor(() => {
    expect(screen.queryByText("Loading assignments...")).toBeNull();
  });
}

beforeEach(() => {
  for (const mock of Object.values(api)) mock.mockReset();
  api.assignments.mockResolvedValue([]);
  api.programme.mockResolvedValue({
    date: "2026-09-19",
    zone: "Africa/Kampala",
    lessons: [],
    calendarEvents: [],
  });
  api.subjects.mockResolvedValue([
    { id: "subject-1", subjectName: "Mathematics" },
  ]);
  api.levels.mockResolvedValue([{ id: "level-1" }, { id: "level-2" }]);
  api.grades.mockImplementation(async (id: string) => {
    if (id === "level-1") {
      return [
        { id: "class-1", className: "Primary One" },
        { id: "unassigned", className: "Unassigned Class" },
      ];
    }
    return [{ id: "class-2", className: "Senior One" }];
  });
});

afterEach(cleanup);

describe("Teacher Workspace loading", () => {
  it("shows an empty state without supporting lookups", async () => {
    render(<TeacherWorkspace />);
    await finishLoading();

    expect(screen.getByText("No active teaching assignments found.")).toBeTruthy();
    expect(api.assignments).toHaveBeenCalledWith();
    expect(api.subjects).not.toHaveBeenCalled();
    expect(api.levels).not.toHaveBeenCalled();
    expect(api.grades).not.toHaveBeenCalled();
    expect(screen.queryByRole("alert")).toBeNull();
  });

  it("resolves multiple classes using education-level IDs", async () => {
    api.assignments.mockResolvedValue([
      assignment("a1", "class-1"),
      assignment("a2", "class-2"),
    ]);
    api.levels.mockResolvedValue([
      { id: "level-1" }, { id: "level-2" }, { id: "level-1" },
    ]);

    render(<TeacherWorkspace />);
    await finishLoading();

    expect(screen.getByText("Primary One")).toBeTruthy();
    expect(screen.getByText("Senior One")).toBeTruthy();
    expect(api.grades).toHaveBeenCalledTimes(2);
    expect(api.grades).toHaveBeenCalledWith("level-1");
    expect(api.grades).toHaveBeenCalledWith("level-2");
    expect(screen.queryByText("Unassigned Class")).toBeNull();
    expect(screen.queryByRole("alert")).toBeNull();
  });

  it("retains assignments and resolved names when one class lookup fails", async () => {
    api.assignments.mockResolvedValue([
      assignment("a1", "class-1"),
      assignment("a2", "class-2"),
    ]);
    api.grades.mockImplementation(async (id: string) => {
      if (id === "level-2") throw new Error("API Error 403");
      return [{ id: "class-1", className: "Primary One" }];
    });

    render(<TeacherWorkspace />);
    await finishLoading();

    expect(screen.getByText("Primary One")).toBeTruthy();
    expect(screen.getByText("class-2")).toBeTruthy();
    expect(screen.getByRole("alert").textContent)
      .toContain("Some class names are unavailable.");
    expect(screen.queryByText("No active teaching assignments found.")).toBeNull();
  });

  it("retains assignments when subjects and education levels cannot load", async () => {
    api.assignments.mockResolvedValue([assignment("a1", "class-1")]);
    api.subjects.mockRejectedValue(new Error("API Error 403"));
    api.levels.mockRejectedValue(new Error("API Error 403"));

    render(<TeacherWorkspace />);
    await finishLoading();

    expect(screen.getByText("class-1")).toBeTruthy();
    expect(screen.getByText(/Subject:\s*subject-1/)).toBeTruthy();
    expect(screen.getByRole("alert").textContent)
      .toContain("Subject names could not be loaded.");
    expect(screen.getByRole("alert").textContent)
      .toContain("Class names could not be loaded.");
    expect(api.grades).not.toHaveBeenCalled();
  });

  it("warns when a referenced class is missing from successful lookups", async () => {
    api.assignments.mockResolvedValue([assignment("a1", "missing-class")]);

    render(<TeacherWorkspace />);
    await finishLoading();

    expect(screen.getByText("missing-class")).toBeTruthy();
    expect(screen.getByRole("alert").textContent)
      .toContain("Some class names are unavailable.");
  });

  it.each([
    ["API Error 403", "Access denied. Check your assignment-read permission and active teacher profile linkage."],
    ["API Error 401", "Your session has expired. Please sign in again."],
    ["Network failure", "Teaching assignments could not be loaded. Please reload to try again."],
  ])("handles assignment failure: %s", async (failure, message) => {
    api.assignments.mockRejectedValue(new Error(failure));

    render(<TeacherWorkspace />);
    await finishLoading();

    expect(screen.getByRole("alert").textContent).toBe(message);
    expect(screen.queryByText("No active teaching assignments found.")).toBeNull();
    expect(api.subjects).not.toHaveBeenCalled();
    expect(api.levels).not.toHaveBeenCalled();
    expect(api.grades).not.toHaveBeenCalled();
  });
});
