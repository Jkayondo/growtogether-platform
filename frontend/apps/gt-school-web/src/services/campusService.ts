import {
  activateCampus,
  createCampus,
  deactivateCampus,
  getCampuses
} from "./campusApi";

import {
  loadCurrentSchoolProfile
} from "./schoolProfileService";

import type {
  Campus,
  CreateCampusRequest
} from "../types/campus";


async function currentSchoolProfileId()
  : Promise<string> {

  const schoolProfile =
    await loadCurrentSchoolProfile();


  if (!schoolProfile.id) {

    throw new Error(
      "The current School Profile is unavailable."
    );

  }


  return schoolProfile.id;

}


export async function loadCampuses()
  : Promise<Campus[]> {

  const schoolProfileId =
    await currentSchoolProfileId();


  const response =
    await getCampuses(
      schoolProfileId
    );


  return response.data;

}


export async function saveCampus(
  request: CreateCampusRequest
): Promise<Campus> {

  const schoolProfileId =
    await currentSchoolProfileId();


  const response =
    await createCampus(
      schoolProfileId,
      request
    );


  return response.data;

}


export async function enableCampus(
  campusCode: string
): Promise<Campus> {

  const response =
    await activateCampus(
      campusCode
    );


  return response.data;

}


export async function disableCampus(
  campusCode: string
): Promise<Campus> {

  const response =
    await deactivateCampus(
      campusCode
    );


  return response.data;

}
