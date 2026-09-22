import {
  getCurrentSchoolProfile
} from "./schoolProfileApi";

import type {
  SchoolProfile
} from "../types/schoolProfile";


export async function loadCurrentSchoolProfile()
  : Promise<SchoolProfile> {

  return getCurrentSchoolProfile();

}
