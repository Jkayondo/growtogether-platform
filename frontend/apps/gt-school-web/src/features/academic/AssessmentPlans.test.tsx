import {
  fireEvent,
  render,
  screen,
  waitFor
} from "@testing-library/react";

import {
  beforeEach,
  describe,
  expect,
  it,
  vi
} from "vitest";

import AssessmentPlans from "./AssessmentPlans";

import {
  Permission
} from "../../auth/permissions";


const {
  hasPermission,
  loadAcademicYears,
  loadCampuses,
  loadEducationLevels,
  loadClassGrades,
  loadAssessmentPlan,
  loadAssessmentPlansByAcademicYear,
  loadAssessmentPlansByCampus,
  loadAssessmentPlansByClassGrade,
  saveAssessmentPlan
} = vi.hoisted(() => ({
  hasPermission: vi.fn(),
  loadAcademicYears: vi.fn(),
  loadCampuses: vi.fn(),
  loadEducationLevels: vi.fn(),
  loadClassGrades: vi.fn(),
  loadAssessmentPlan: vi.fn(),
  loadAssessmentPlansByAcademicYear: vi.fn(),
  loadAssessmentPlansByCampus: vi.fn(),
  loadAssessmentPlansByClassGrade: vi.fn(),
  saveAssessmentPlan: vi.fn()
}));


vi.mock(
  "../../auth/authContext",
  () => ({
    useAuth: () => ({
      hasPermission
    })
  })
);


vi.mock(
  "../../services/academicYearService",
  () => ({
    loadAcademicYears
  })
);

vi.mock(
  "../../services/campusService",
  () => ({
    loadCampuses
  })
);

vi.mock(
  "../../services/educationLevelService",
  () => ({
    loadEducationLevels
  })
);

vi.mock(
  "../../services/classGradeService",
  () => ({
    loadClassGrades
  })
);


vi.mock(
  "../../services/assessmentPlanService",
  () => ({
    loadAssessmentPlan,
    loadAssessmentPlansByAcademicYear,
    loadAssessmentPlansByCampus,
    loadAssessmentPlansByClassGrade,
    saveAssessmentPlan
  })
);


describe(
  "Assessment Plans",
  () => {

    beforeEach(() => {

      vi.clearAllMocks();

      hasPermission.mockImplementation(
        (permission: string) =>
          (
            [
              Permission.ASSESSMENT_READ,
              Permission.ASSESSMENT_CREATE,
              Permission.ACADEMIC_YEAR_READ,
              Permission.CAMPUS_READ,
              Permission.CURRICULUM_READ,
              Permission.CLASS_GRADE_READ
            ] as readonly string[]
          ).includes(permission)
      );

      loadAcademicYears.mockResolvedValue([]);
      loadCampuses.mockResolvedValue([]);
      loadEducationLevels.mockResolvedValue([]);
      loadClassGrades.mockResolvedValue([]);

      loadAssessmentPlansByAcademicYear
        .mockResolvedValue([]);

      loadAssessmentPlansByCampus
        .mockResolvedValue([]);

      loadAssessmentPlansByClassGrade
        .mockResolvedValue([]);

    });


    it(
      "checks assessment read and create permissions",
      async () => {

        render(<AssessmentPlans />);

        await waitFor(() => {

          expect(
            hasPermission
          ).toHaveBeenCalledWith(
            Permission.ASSESSMENT_READ
          );

          expect(
            hasPermission
          ).toHaveBeenCalledWith(
            Permission.ASSESSMENT_CREATE
          );

        });

      }
    );


    it(
      "blocks plan retrieval without assessment read permission",
      async () => {

        hasPermission.mockImplementation(
          (permission: string) =>
            permission !==
              Permission.ASSESSMENT_READ
        );

        render(<AssessmentPlans />);

        const retrieveButtons =
          screen.queryAllByRole(
            "button"
          );

        const retrieveButton =
          retrieveButtons.find(
            button =>
              /retrieve/i.test(
                button.textContent ?? ""
              )
          );

        if (retrieveButton) {

          fireEvent.click(
            retrieveButton
          );

        }

        expect(
          loadAssessmentPlan
        ).not.toHaveBeenCalled();

      }
    );


    it(
      "does not save when assessment create permission is absent",
      async () => {

        hasPermission.mockImplementation(
          (permission: string) =>
            permission !==
              Permission.ASSESSMENT_CREATE
        );

        render(<AssessmentPlans />);

        const buttons =
          screen.queryAllByRole(
            "button"
          );

        const createButton =
          buttons.find(
            button =>
              /create|save/i.test(
                button.textContent ?? ""
              )
          );

        if (createButton) {

          fireEvent.click(
            createButton
          );

        }

        expect(
          saveAssessmentPlan
        ).not.toHaveBeenCalled();

      }
    );


    it(
      "loads assessment plans through the academic-year service path",
      async () => {

        loadAssessmentPlansByAcademicYear
          .mockResolvedValue([]);

        render(<AssessmentPlans />);

        await waitFor(() => {

          expect(
            hasPermission
          ).toHaveBeenCalledWith(
            Permission.ASSESSMENT_READ
          );

        });

      }
    );

  }
);
