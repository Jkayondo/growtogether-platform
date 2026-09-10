import { getSubjects } from "./subjectApi";
import type { Subject } from "../types/subject";

export async function loadSubjects(): Promise<Subject[]> {
  const response = await getSubjects();
  return response.data;
}
