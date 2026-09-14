import {
  beforeEach,
  describe,
  expect,
  it,
  vi
} from "vitest";

const api = vi.hoisted(
  () => ({
    contexts: vi.fn(),
    subjects: vi.fn(),
    levels: vi.fn(),
    classes: vi.fn()
  })
);

vi.mock(
  "./teacherAiContextService",
  () => ({
    loadTeacherAiAssignmentContexts:
      api.contexts
  })
);

vi.mock(
  "./subjectService",
  () => ({
    loadSubjects:
      api.subjects
  })
);

vi.mock(
  "./educationLevelService",
  () => ({
    loadEducationLevels:
      api.levels
  })
);

vi.mock(
  "./classGradeService",
  () => ({
    loadClassGrades:
      api.classes
  })
);

import {
  loadTeacherAiDisplayContexts
} from "./teacherAiDisplayContextService";

function context(
  assignmentId = "assignment-1",
  classGradeId = "class-1",
  subjectId = "subject-1"
) {
  return {
    assignmentId,
    teacherProfileId: "teacher-1",
    classGradeId,
    subjectId,
    weeklyPeriods: 5,
    assignmentStatus: "ACTIVE"
  };
}

describe(
  "teacherAiDisplayContextService",
  () => {
    beforeEach(() => {
      api.contexts.mockReset();
      api.subjects.mockReset();
      api.levels.mockReset();
      api.classes.mockReset();

      api.contexts.mockResolvedValue([
        context()
      ]);

      api.subjects.mockResolvedValue([
        {
          id: "subject-1",
          subjectName: "Mathematics"
        }
      ]);

      api.levels.mockResolvedValue([
        {
          id: "level-1"
        }
      ]);

      api.classes.mockResolvedValue([
        {
          id: "class-1",
          className: "Primary One"
        }
      ]);
    });

    it(
      "skips supporting lookups when there are no active assignments",
      async () => {
        api.contexts.mockResolvedValue(
          []
        );

        await expect(
          loadTeacherAiDisplayContexts()
        ).resolves.toEqual({
          contexts: [],
          notices: []
        });

        expect(api.subjects)
          .not.toHaveBeenCalled();

        expect(api.levels)
          .not.toHaveBeenCalled();

        expect(api.classes)
          .not.toHaveBeenCalled();
      }
    );

    it(
      "resolves teacher-friendly class and subject names",
      async () => {
        await expect(
          loadTeacherAiDisplayContexts()
        ).resolves.toEqual({
          contexts: [
            {
              ...context(),
              className:
                "Primary One",
              subjectName:
                "Mathematics"
            }
          ],
          notices: []
        });

        expect(api.classes)
          .toHaveBeenCalledWith(
            "level-1"
          );
      }
    );

    it(
      "retains assignment context when one class lookup fails",
      async () => {
        api.contexts.mockResolvedValue([
          context(
            "assignment-1",
            "class-1",
            "subject-1"
          ),
          context(
            "assignment-2",
            "class-2",
            "subject-1"
          )
        ]);

        api.levels.mockResolvedValue([
          {
            id: "level-1"
          },
          {
            id: "level-2"
          }
        ]);

        api.classes.mockImplementation(
          async (
            levelId: string
          ) => {
            if (
              levelId === "level-2"
            ) {
              throw new Error(
                "API Error 403"
              );
            }

            return [
              {
                id: "class-1",
                className:
                  "Primary One"
              }
            ];
          }
        );

        const result =
          await loadTeacherAiDisplayContexts();

        expect(
          result.contexts[0]
        ).toMatchObject({
          className:
            "Primary One"
        });

        expect(
          result.contexts[1]
        ).toMatchObject({
          className:
            "class-2"
        });

        expect(
          result.notices
        ).toContain(
          "Some class names are unavailable."
        );
      }
    );

    it(
      "falls back to subject reference ids when subject lookup fails",
      async () => {
        api.subjects.mockRejectedValue(
          new Error(
            "API Error 403"
          )
        );

        const result =
          await loadTeacherAiDisplayContexts();

        expect(
          result.contexts[0]
            .subjectName
        ).toBe(
          "subject-1"
        );

        expect(
          result.notices
        ).toContain(
          "Subject names could not be loaded."
        );
      }
    );

    it(
      "falls back to class reference ids when education levels cannot load",
      async () => {
        api.levels.mockRejectedValue(
          new Error(
            "API Error 403"
          )
        );

        const result =
          await loadTeacherAiDisplayContexts();

        expect(
          result.contexts[0]
            .className
        ).toBe(
          "class-1"
        );

        expect(
          result.notices
        ).toContain(
          "Class names could not be loaded."
        );

        expect(api.classes)
          .not.toHaveBeenCalled();
      }
    );
  }
);
