import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import AppRoutes from "./AppRoutes";
import { Permission } from "../auth/permissions";

const auth = vi.hoisted(() => ({
  isAuthenticated: true,
  hasPermission: vi.fn<(permission: string) => boolean>(),
}));

vi.mock("../auth/authContext", () => ({
  useAuth: () => auth,
}));

vi.mock("../layouts/MainLayout", async () => {
  const { Outlet } = await import("react-router-dom");
  return { default: () => <Outlet /> };
});

vi.mock("../features/administration/Dashboard", () => ({
  default: () => <div data-testid="Dashboard">Dashboard</div>,
}));

vi.mock("../features/auth/Login", () => ({
  default: () => <div data-testid="Login">Login</div>,
}));

vi.mock("../features/visitor/VisitorDashboard", () => ({
  default: () => <div data-testid="VisitorDashboard">VisitorDashboard</div>,
}));

vi.mock("../features/community/Learners", () => ({
  default: () => <div data-testid="Learners">Learners</div>,
}));

vi.mock("../features/community/LearnerProfile", () => ({
  default: () => <div data-testid="LearnerProfile">LearnerProfile</div>,
}));

vi.mock("../features/community/StudentEnrollments", () => ({
  default: () => <div data-testid="StudentEnrollments">StudentEnrollments</div>,
}));

vi.mock("../features/academic/AcademicYears", () => ({
  default: () => <div data-testid="AcademicYears">AcademicYears</div>,
}));

vi.mock("../features/academic/Campuses", () => ({
  default: () => <div data-testid="Campuses">Campuses</div>,
}));

vi.mock("../features/academic/Curricula", () => ({
  default: () => <div data-testid="Curricula">Curricula</div>,
}));

vi.mock("../features/academic/ClassGrades", () => ({
  default: () => <div data-testid="ClassGrades">ClassGrades</div>,
}));

vi.mock("../features/academic/ClassOfferings", () => ({
  default: () => <div data-testid="ClassOfferings">ClassOfferings</div>,
}));

vi.mock("../features/academic/SubjectOfferings", () => ({
  default: () => <div data-testid="SubjectOfferings">SubjectOfferings</div>,
}));

vi.mock("../features/academic/TeacherProfiles", () => ({
  default: () => <div data-testid="TeacherProfiles">TeacherProfiles</div>,
}));

vi.mock("../features/academic/TeachingAssignments", () => ({
  default: () => <div data-testid="TeachingAssignments">TeachingAssignments</div>,
}));

vi.mock("../features/academic/Timetables", () => ({
  default: () => <div data-testid="Timetables">Timetables</div>,
}));

vi.mock("../features/academic/AssessmentPlans", () => ({
  default: () => <div data-testid="AssessmentPlans">AssessmentPlans</div>,
}));

vi.mock("../features/academic/ExaminationCandidates", () => ({
  default: () => <div data-testid="ExaminationCandidates">ExaminationCandidates</div>,
}));

vi.mock("../features/academic/ExaminationSessions", () => ({
  default: () => <div data-testid="ExaminationSessions">ExaminationSessions</div>,
}));

vi.mock("../features/academic/CandidatePaperRegistrations", () => ({
  default: () => <div data-testid="CandidatePaperRegistrations">CandidatePaperRegistrations</div>,
}));

vi.mock("../features/academic/MarkSheets", () => ({
  default: () => <div data-testid="MarkSheets">MarkSheets</div>,
}));

vi.mock("../features/connect/ConnectDashboard", () => ({
  default: () => <div data-testid="ConnectDashboard">ConnectDashboard</div>,
}));

vi.mock("../features/teacher/TeacherWorkspace", () => ({
  default: () => <div data-testid="TeacherWorkspace">TeacherWorkspace</div>,
}));

afterEach(cleanup);

beforeEach(() => {
  auth.isAuthenticated = true;
  auth.hasPermission.mockReset();
  auth.hasPermission.mockReturnValue(false);
});

function openWorkspace() {
  return render(
    <MemoryRouter initialEntries={["/teacher/workspace"]}>
      <AppRoutes />
    </MemoryRouter>
  );
}

describe("Teacher Workspace route access", () => {
  it("allows an authenticated user with assignment-read permission", async () => {
    auth.hasPermission.mockImplementation(
      permission => permission === Permission.TEACHING_ASSIGNMENT_READ
    );

    openWorkspace();

    expect(await screen.findByTestId("TeacherWorkspace")).toBeTruthy();
    expect(auth.hasPermission).toHaveBeenCalledWith(
      Permission.TEACHING_ASSIGNMENT_READ
    );
    expect(screen.queryByTestId("Login")).toBeNull();
  });

  it("redirects a user without permission to the dashboard", async () => {
    openWorkspace();

    expect(await screen.findByTestId("Dashboard")).toBeTruthy();
    expect(screen.queryByTestId("TeacherWorkspace")).toBeNull();
  });

  it("redirects a signed-out user to login before checking permission", async () => {
    auth.isAuthenticated = false;

    openWorkspace();

    expect(await screen.findByTestId("Login")).toBeTruthy();
    expect(screen.queryByTestId("TeacherWorkspace")).toBeNull();
    expect(auth.hasPermission).not.toHaveBeenCalled();
  });
});
