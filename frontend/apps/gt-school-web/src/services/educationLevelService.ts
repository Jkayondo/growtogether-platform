import {
  getEducationLevels
} from "./educationLevelApi";

import type {
  EducationLevel
} from "../types/educationLevel";


export async function loadEducationLevels()
  : Promise<EducationLevel[]> {

  const response =
    await getEducationLevels();


  return response.data;

}
