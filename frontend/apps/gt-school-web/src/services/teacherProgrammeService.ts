import {
  getMyTodayProgramme
} from "./teacherProgrammeApi";

import type {
  TeacherProgrammeToday
} from "../types/teacherProgramme";

export async function loadMyTodayProgramme()
  : Promise<TeacherProgrammeToday> {

  const programme =
    await getMyTodayProgramme();

  if (
    !programme ||
    typeof programme.date !== "string" ||
    programme.date.trim() === "" ||
    typeof programme.zone !== "string" ||
    programme.zone.trim() === "" ||
    !Array.isArray(programme.lessons) ||
    !Array.isArray(programme.calendarEvents)
  ) {
    throw new Error(
      "Invalid teacher programme response."
    );
  }

  return programme;
}
