import {
  createAcademicYear,
  getAcademicYears
} from "./academicYearApi";

import type {
  AcademicYear,
  CreateAcademicYearRequest
} from "../types/academicYear";


export async function loadAcademicYears()
  : Promise<AcademicYear[]> {

  const response =
    await getAcademicYears();


  return response.data;

}


export async function saveAcademicYear(
  request: CreateAcademicYearRequest
): Promise<AcademicYear> {

  const response =
    await createAcademicYear(
      request
    );


  return response.data;

}
