import {
  activateCurriculum,
  activateCurriculumLearningArea,
  activateCurriculumVersion,
  activateSubjectCatalogueEntry,
  approveCurriculumVersion,
  archiveCurriculumClassGrade,
  changeCurriculumSubjectRequirement,
  createCurriculum,
  createCurriculumClassGrade,
  createCurriculumLearningArea,
  createCurriculumSubject,
  createCurriculumVersion,
  createSubjectCatalogue,
  deactivateCurriculumLearningArea,
  deactivateSubjectCatalogueEntry,
  getCurricula,
  getCurriculumClassGrades,
  getCurriculumLearningAreas,
  getCurriculumSubjects,
  getCurriculumVersions,
  getSubjectCatalogue
} from "./curriculumApi";

import type {
  ApproveCurriculumVersionRequest,
  ChangeCurriculumSubjectRequirementRequest,
  CreateCurriculumClassGradeRequest,
  CreateCurriculumLearningAreaRequest,
  CreateCurriculumRequest,
  CreateCurriculumSubjectRequest,
  CreateCurriculumVersionRequest,
  CreateSubjectCatalogueRequest,
  Curriculum,
  CurriculumClassGrade,
  CurriculumLearningArea,
  CurriculumSubject,
  CurriculumVersion,
  SubjectCatalogue
} from "../types/curriculum";


export async function loadCurricula()
  : Promise<Curriculum[]> {

  const response =
    await getCurricula();

  return response.data;
}


export async function saveCurriculum(
  request: CreateCurriculumRequest
): Promise<Curriculum> {

  const response =
    await createCurriculum(
      request
    );

  return response.data;
}


export async function enableCurriculum(
  curriculumCode: string
): Promise<Curriculum> {

  const response =
    await activateCurriculum(
      curriculumCode
    );

  return response.data;
}


export async function loadCurriculumVersions(
  curriculumId: string
): Promise<CurriculumVersion[]> {

  const response =
    await getCurriculumVersions(
      curriculumId
    );

  return response.data;
}


export async function saveCurriculumVersion(
  request: CreateCurriculumVersionRequest
): Promise<CurriculumVersion> {

  const response =
    await createCurriculumVersion(
      request
    );

  return response.data;
}


export async function approveVersion(
  request: ApproveCurriculumVersionRequest
): Promise<CurriculumVersion> {

  const response =
    await approveCurriculumVersion(
      request
    );

  return response.data;
}


export async function enableCurriculumVersion(
  curriculumId: string,
  versionCode: string
): Promise<CurriculumVersion> {

  const response =
    await activateCurriculumVersion(
      curriculumId,
      versionCode
    );

  return response.data;
}


export async function loadCurriculumClassGrades(
  curriculumVersionId: string
): Promise<CurriculumClassGrade[]> {

  return getCurriculumClassGrades(
    curriculumVersionId
  );
}


export async function saveCurriculumClassGrade(
  request: CreateCurriculumClassGradeRequest
): Promise<CurriculumClassGrade> {

  return createCurriculumClassGrade(
    request
  );
}


export async function archiveClassGradeMapping(
  curriculumVersionId: string,
  classGradeId: string
): Promise<CurriculumClassGrade> {

  return archiveCurriculumClassGrade(
    curriculumVersionId,
    classGradeId
  );
}


export async function loadCurriculumLearningAreas(
  curriculumVersionId: string
): Promise<CurriculumLearningArea[]> {

  const response =
    await getCurriculumLearningAreas(
      curriculumVersionId
    );

  return response.data;
}


export async function saveCurriculumLearningArea(
  request: CreateCurriculumLearningAreaRequest
): Promise<CurriculumLearningArea> {

  const response =
    await createCurriculumLearningArea(
      request
    );

  return response.data;
}


export async function enableCurriculumLearningArea(
  curriculumVersionId: string,
  learningAreaCode: string
): Promise<CurriculumLearningArea> {

  const response =
    await activateCurriculumLearningArea(
      curriculumVersionId,
      learningAreaCode
    );

  return response.data;
}


export async function disableCurriculumLearningArea(
  curriculumVersionId: string,
  learningAreaCode: string
): Promise<CurriculumLearningArea> {

  const response =
    await deactivateCurriculumLearningArea(
      curriculumVersionId,
      learningAreaCode
    );

  return response.data;
}


export async function loadSubjectCatalogue(
  curriculumVersionId: string
): Promise<SubjectCatalogue[]> {

  const response =
    await getSubjectCatalogue(
      curriculumVersionId
    );

  return response.data;
}


export async function saveSubjectCatalogueEntry(
  request: CreateSubjectCatalogueRequest
): Promise<SubjectCatalogue> {

  const response =
    await createSubjectCatalogue(
      request
    );

  return response.data;
}


export async function enableSubjectCatalogueEntry(
  curriculumVersionId: string,
  subjectCode: string
): Promise<SubjectCatalogue> {

  const response =
    await activateSubjectCatalogueEntry(
      curriculumVersionId,
      subjectCode
    );

  return response.data;
}


export async function disableSubjectCatalogueEntry(
  curriculumVersionId: string,
  subjectCode: string
): Promise<SubjectCatalogue> {

  const response =
    await deactivateSubjectCatalogueEntry(
      curriculumVersionId,
      subjectCode
    );

  return response.data;
}

export async function loadCurriculumSubjects(
  curriculumVersionId: string,
  classGradeId: string
): Promise<CurriculumSubject[]> {

  return getCurriculumSubjects(
    curriculumVersionId,
    classGradeId
  );
}


export async function saveCurriculumSubject(
  request: CreateCurriculumSubjectRequest
): Promise<CurriculumSubject> {

  return createCurriculumSubject(
    request
  );
}


export async function updateCurriculumSubjectRequirement(
  request: ChangeCurriculumSubjectRequirementRequest
): Promise<CurriculumSubject> {

  return changeCurriculumSubjectRequirement(
    request
  );
}
