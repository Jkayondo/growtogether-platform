import apiClient from "./apiClient";

import type {
  TeacherProgrammeToday
} from "../types/teacherProgramme";

const TODAY_PROGRAMME_ENDPOINT =
  "/api/v1/school/teacher/programme/today";

export async function getMyTodayProgramme()
  : Promise<TeacherProgrammeToday> {

  return apiClient.get<TeacherProgrammeToday>(
    TODAY_PROGRAMME_ENDPOINT
  );
}
