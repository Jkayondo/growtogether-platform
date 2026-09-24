import {
  activateClassOffering,
  createClassOffering,
  deactivateClassOffering,
  getClassOfferingsByAcademicYear
} from "./classOfferingApi";

import type {
  ClassOffering,
  CreateClassOfferingRequest
} from "../types/classOffering";


export async function loadClassOfferings(
  academicYearId: string
): Promise<ClassOffering[]> {

  const response =
    await getClassOfferingsByAcademicYear(
      academicYearId
    );


  return response.data;

}


export async function saveClassOffering(
  request: CreateClassOfferingRequest
): Promise<ClassOffering> {

  const response =
    await createClassOffering(
      request
    );


  return response.data;

}


export async function enableClassOffering(
  offeringCode: string
): Promise<ClassOffering> {

  const response =
    await activateClassOffering(
      offeringCode
    );


  return response.data;

}


export async function disableClassOffering(
  offeringCode: string
): Promise<ClassOffering> {

  const response =
    await deactivateClassOffering(
      offeringCode
    );


  return response.data;

}
