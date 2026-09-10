import {
  activateClassGrade,
  createClassGrade,
  deactivateClassGrade,
  getClassGrades
} from "./classGradeApi";

import type {
  ClassGrade,
  CreateClassGradeRequest
} from "../types/classGrade";


export async function loadClassGrades(
  educationLevelId: string
): Promise<ClassGrade[]> {

  const response =
    await getClassGrades(
      educationLevelId
    );


  return response.data;

}


export async function saveClassGrade(
  request: CreateClassGradeRequest
): Promise<ClassGrade> {

  const response =
    await createClassGrade(
      request
    );


  return response.data;

}


export async function enableClassGrade(
  classCode: string
): Promise<ClassGrade> {

  const response =
    await activateClassGrade(
      classCode
    );


  return response.data;

}


export async function disableClassGrade(
  classCode: string
): Promise<ClassGrade> {

  const response =
    await deactivateClassGrade(
      classCode
    );


  return response.data;

}
