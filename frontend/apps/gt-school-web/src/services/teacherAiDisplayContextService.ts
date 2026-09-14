import {
  loadTeacherAiAssignmentContexts
} from "./teacherAiContextService";

import {
  loadSubjects
} from "./subjectService";

import {
  loadEducationLevels
} from "./educationLevelService";

import {
  loadClassGrades
} from "./classGradeService";

import type {
  ClassGrade
} from "../types/classGrade";

import type {
  Subject
} from "../types/subject";

import type {
  TeacherAiDisplayContextResult
} from "../types/teacherAi";

export async function loadTeacherAiDisplayContexts()
  : Promise<TeacherAiDisplayContextResult> {
  const contexts =
    await loadTeacherAiAssignmentContexts();

  if (contexts.length === 0) {
    return {
      contexts: [],
      notices: []
    };
  }

  const notices: string[] = [];

  const [
    subjectResult,
    levelResult
  ] =
    await Promise.allSettled([
      loadSubjects(),
      loadEducationLevels()
    ]);

  const subjectsById =
    new Map<string, Subject>();

  if (subjectResult.status === "fulfilled") {
    for (const subject of subjectResult.value) {
      subjectsById.set(
        subject.id,
        subject
      );
    }

    if (
      contexts.some(
        context =>
          !subjectsById.has(
            context.subjectId
          )
      )
    ) {
      notices.push(
        "Some subject names are unavailable."
      );
    }
  } else {
    notices.push(
      "Subject names could not be loaded."
    );
  }

  const classesById =
    new Map<string, ClassGrade>();

  if (levelResult.status === "fulfilled") {
    const requiredClassIds =
      new Set(
        contexts.map(
          context =>
            context.classGradeId
        )
      );

    const levelIds =
      [
        ...new Set(
          levelResult.value.map(
            level => level.id
          )
        )
      ];

    let classLookupFailed = false;

    for (
      let offset = 0;
      offset < levelIds.length;
      offset += 4
    ) {
      const batch =
        await Promise.allSettled(
          levelIds
            .slice(
              offset,
              offset + 4
            )
            .map(
              levelId =>
                loadClassGrades(
                  levelId
                )
            )
        );

      for (const result of batch) {
        if (
          result.status === "rejected"
        ) {
          classLookupFailed = true;
          continue;
        }

        for (const grade of result.value) {
          if (
            requiredClassIds.has(
              grade.id
            )
          ) {
            classesById.set(
              grade.id,
              grade
            );
          }
        }
      }
    }

    if (
      classLookupFailed ||
      [...requiredClassIds].some(
        id =>
          !classesById.has(id)
      )
    ) {
      notices.push(
        "Some class names are unavailable."
      );
    }
  } else {
    notices.push(
      "Class names could not be loaded."
    );
  }

  return {
    contexts:
      contexts.map(
        context => ({
          ...context,

          className:
            classesById.get(
              context.classGradeId
            )?.className
            ?? context.classGradeId,

          subjectName:
            subjectsById.get(
              context.subjectId
            )?.subjectName
            ?? context.subjectId
        })
      ),

    notices
  };
}
