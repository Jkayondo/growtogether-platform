import {
  activateTeacherProfile,
  createTeacherProfile,
  deactivateTeacherProfile,
  getTeacherProfileByNumber,
  getTeacherProfilesByStatus
} from "./teacherProfileApi";

import type {
  CreateTeacherProfileRequest,
  TeacherProfile
} from "../types/teacherProfile";


export async function loadTeacherProfilesByStatus(
  teachingStatus: string
): Promise<TeacherProfile[]> {

  const response =
    await getTeacherProfilesByStatus(
      teachingStatus
    );

  return response.data;

}


export async function loadTeacherProfileByNumber(
  teacherNumber: string
): Promise<TeacherProfile> {

  const response =
    await getTeacherProfileByNumber(
      teacherNumber
    );

  return response.data;

}


export async function saveTeacherProfile(
  request: CreateTeacherProfileRequest
): Promise<TeacherProfile> {

  const response =
    await createTeacherProfile(
      request
    );

  return response.data;

}


export async function enableTeacherProfile(
  teacherNumber: string
): Promise<TeacherProfile> {

  const response =
    await activateTeacherProfile(
      teacherNumber
    );

  return response.data;

}


export async function disableTeacherProfile(
  teacherNumber: string
): Promise<TeacherProfile> {

  const response =
    await deactivateTeacherProfile(
      teacherNumber
    );

  return response.data;

}
