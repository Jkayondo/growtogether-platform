import {
  useCallback,
  useEffect,
  useMemo,
  useState
} from "react";

import {
  useAuth
} from "../../auth/authContext";

import {
  Permission
} from "../../auth/permissions";

import {
  loadEducationLevels
} from "../../services/educationLevelService";

import {
  loadClassGrades
} from "../../services/classGradeService";

import {
  loadSubjects
} from "../../services/subjectService";

import {
  archiveClassGradeMapping,
  loadCurriculumClassGrades,
  loadCurriculumSubjects,
  saveCurriculumClassGrade,
  saveCurriculumSubject,
  updateCurriculumSubjectRequirement
} from "../../services/curriculumService";

import type {
  EducationLevel
} from "../../types/educationLevel";

import type {
  ClassGrade
} from "../../types/classGrade";

import type {
  Subject
} from "../../types/subject";

import type {
  CurriculumClassGrade,
  CurriculumSubject
} from "../../types/curriculum";


interface Props {
  curriculumVersionId: string;
}


const REQUIREMENTS = [
  "CORE",
  "COMPULSORY",
  "ELECTIVE",
  "OPTIONAL",
  "VOCATIONAL",
  "CO_CURRICULAR"
] as const;


export default function CurriculumMappings({
  curriculumVersionId
}: Props) {

  const {
    hasPermission
  } = useAuth();


  const canReadGrades =
    hasPermission(
      Permission.CLASS_GRADE_READ
    );


  const canReadMappings =
    hasPermission(
      Permission.CURRICULUM_VERSION_READ
    );


  const canCreateMappings =
    hasPermission(
      Permission.CURRICULUM_VERSION_CREATE
    );


  const canManageMappings =
    hasPermission(
      Permission.CURRICULUM_VERSION_MANAGE
    );


  const canReadSubjects =
    hasPermission(
      Permission.SUBJECT_READ
    );


  const canCreateSubjects =
    hasPermission(
      Permission.SUBJECT_CREATE
    );


  const canManageSubjects =
    hasPermission(
      Permission.SUBJECT_MANAGE
    );


  const [
    educationLevels,
    setEducationLevels
  ] =
    useState<EducationLevel[]>([]);


  const [
    classGrades,
    setClassGrades
  ] =
    useState<ClassGrade[]>([]);


  const [
    subjects,
    setSubjects
  ] =
    useState<Subject[]>([]);


  const [
    gradeMappings,
    setGradeMappings
  ] =
    useState<CurriculumClassGrade[]>([]);


  const [
    curriculumSubjects,
    setCurriculumSubjects
  ] =
    useState<CurriculumSubject[]>([]);


  const [
    selectedEducationLevelId,
    setSelectedEducationLevelId
  ] =
    useState("");


  const [
    selectedClassGradeId,
    setSelectedClassGradeId
  ] =
    useState("");


  const [
    selectedMappedClassGradeId,
    setSelectedMappedClassGradeId
  ] =
    useState("");


  const [
    selectedSubjectId,
    setSelectedSubjectId
  ] =
    useState("");


  const [
    sequenceNumber,
    setSequenceNumber
  ] =
    useState("1");


  const [
    loadingDependencies,
    setLoadingDependencies
  ] =
    useState(false);


  const [
    loadingMappings,
    setLoadingMappings
  ] =
    useState(false);


  const [
    loadingCurriculumSubjects,
    setLoadingCurriculumSubjects
  ] =
    useState(false);


  const [
    savingGradeMapping,
    setSavingGradeMapping
  ] =
    useState(false);


  const [
    savingSubjectMapping,
    setSavingSubjectMapping
  ] =
    useState(false);


  const [
    changingGradeMapping,
    setChangingGradeMapping
  ] =
    useState<string | null>(
      null
    );


  const [
    changingSubject,
    setChangingSubject
  ] =
    useState<string | null>(
      null
    );


  const [
    message,
    setMessage
  ] =
    useState<string | null>(
      null
    );


  const [
    error,
    setError
  ] =
    useState<string | null>(
      null
    );


  const activeEducationLevels =
    useMemo(
      () =>
        educationLevels.filter(
          level =>
            level.status === "ACTIVE"
        ),
      [
        educationLevels
      ]
    );


  const gradesForSelectedLevel =
    useMemo(
      () =>
        classGrades.filter(
          grade =>
            grade.educationLevelId
            === selectedEducationLevelId
        ),
      [
        classGrades,
        selectedEducationLevelId
      ]
    );


  const mappedGradeIds =
    useMemo(
      () =>
        new Set(
          gradeMappings.map(
            mapping =>
              mapping.classGradeId
          )
        ),
      [
        gradeMappings
      ]
    );


  const availableGrades =
    useMemo(
      () =>
        gradesForSelectedLevel.filter(
          grade =>
            !mappedGradeIds.has(
              grade.id
            )
        ),
      [
        gradesForSelectedLevel,
        mappedGradeIds
      ]
    );


  const mappedSubjectIds =
    useMemo(
      () =>
        new Set(
          curriculumSubjects.map(
            mapping =>
              mapping.subjectId
          )
        ),
      [
        curriculumSubjects
      ]
    );


  const availableSubjects =
    useMemo(
      () =>
        subjects.filter(
          subject =>
            subject.status === "ACTIVE"
            && !mappedSubjectIds.has(
              subject.id
            )
        ),
      [
        subjects,
        mappedSubjectIds
      ]
    );


  function classGradeName(
    classGradeId: string
  ) {

    const grade =
      classGrades.find(
        item =>
          item.id === classGradeId
      );


    return grade
      ? `${grade.classCode} — ${grade.className}`
      : classGradeId;

  }


  function subjectName(
    subjectId: string
  ) {

    const subject =
      subjects.find(
        item =>
          item.id === subjectId
      );


    return subject
      ? `${subject.subjectCode} — ${subject.subjectName}`
      : subjectId;

  }


  const refreshMappings =
    useCallback(
      async () => {

        if (
          !curriculumVersionId
          || !canReadMappings
        ) {

          setGradeMappings([]);
          setSelectedMappedClassGradeId("");

          return;
        }


        setLoadingMappings(
          true
        );


        try {

          const data =
            await loadCurriculumClassGrades(
              curriculumVersionId
            );


          setGradeMappings(
            data
          );


          setSelectedMappedClassGradeId(
            currentId => {

              const current =
                data.find(
                  mapping =>
                    mapping.classGradeId
                    === currentId
                );


              const next =
                current
                ?? data.find(
                  mapping =>
                    mapping.status
                    !== "ARCHIVED"
                )
                ?? data[0];


              return next?.classGradeId ?? "";
            }
          );


          const highestSequence =
            data.reduce(
              (
                highest,
                mapping
              ) =>
                Math.max(
                  highest,
                  mapping.sequenceNumber
                ),
              0
            );


          setSequenceNumber(
            String(
              highestSequence + 1
            )
          );

        }
        catch {

          setError(
            "Unable to load curriculum grade mappings."
          );

        }
        finally {

          setLoadingMappings(
            false
          );

        }

      },
      [
        curriculumVersionId,
        canReadMappings
      ]
    );


  const refreshCurriculumSubjects =
    useCallback(
      async (
        classGradeId: string
      ) => {

        if (
          !curriculumVersionId
          || !classGradeId
          || !canReadSubjects
        ) {

          setCurriculumSubjects([]);
          setSelectedSubjectId("");

          return;
        }


        setLoadingCurriculumSubjects(
          true
        );


        try {

          const data =
            await loadCurriculumSubjects(
              curriculumVersionId,
              classGradeId
            );


          setCurriculumSubjects(
            data
          );

        }
        catch {

          setError(
            "Unable to load curriculum subjects."
          );

        }
        finally {

          setLoadingCurriculumSubjects(
            false
          );

        }

      },
      [
        curriculumVersionId,
        canReadSubjects
      ]
    );


  useEffect(() => {

    async function loadDependencies() {

      if (
        !curriculumVersionId
      ) {

        setEducationLevels([]);
        setClassGrades([]);
        setSubjects([]);

        return;
      }


      setLoadingDependencies(
        true
      );

      setMessage(null);
      setError(null);


      try {

        const levels =
          canReadGrades
            ? await loadEducationLevels()
            : [];


        setEducationLevels(
          levels
        );


        const gradeGroups =
          canReadGrades
            ? await Promise.all(
                levels.map(
                  level =>
                    loadClassGrades(
                      level.id
                    )
                )
              )
            : [];


        setClassGrades(
          gradeGroups.flat()
        );


        const firstLevel =
          levels.find(
            level =>
              level.status === "ACTIVE"
          )
          ?? levels[0];


        setSelectedEducationLevelId(
          firstLevel?.id ?? ""
        );


        const subjectData =
          canReadSubjects
            ? await loadSubjects()
            : [];


        setSubjects(
          subjectData
        );

      }
      catch {

        setError(
          "Unable to load grade and subject dependencies."
        );

      }
      finally {

        setLoadingDependencies(
          false
        );

      }

    }


    void loadDependencies();
    void refreshMappings();

  }, [
    curriculumVersionId,
    canReadGrades,
    canReadSubjects,
    refreshMappings
  ]);


  useEffect(() => {

    if (
      selectedMappedClassGradeId
    ) {

      void refreshCurriculumSubjects(
        selectedMappedClassGradeId
      );

    }
    else {

      setCurriculumSubjects([]);

    }

  }, [
    selectedMappedClassGradeId,
    refreshCurriculumSubjects
  ]);


  useEffect(() => {

    const selectedStillAvailable =
      availableGrades.find(
        grade =>
          grade.id
          === selectedClassGradeId
      );


    if (!selectedStillAvailable) {

      setSelectedClassGradeId(
        availableGrades[0]?.id
        ?? ""
      );

    }

  }, [
    availableGrades,
    selectedClassGradeId
  ]);


  useEffect(() => {

    const selectedStillAvailable =
      availableSubjects.find(
        subject =>
          subject.id
          === selectedSubjectId
      );


    if (!selectedStillAvailable) {

      setSelectedSubjectId(
        availableSubjects[0]?.id
        ?? ""
      );

    }

  }, [
    availableSubjects,
    selectedSubjectId
  ]);


  async function submitGradeMapping(
    event: React.FormEvent
  ) {

    event.preventDefault();

    setMessage(null);
    setError(null);


    if (
      !curriculumVersionId
      || !selectedClassGradeId
    ) {

      setError(
        "Select a Class Grade to map."
      );

      return;
    }


    const parsedSequence =
      Number(
        sequenceNumber
      );


    if (
      !Number.isInteger(
        parsedSequence
      )
      || parsedSequence <= 0
    ) {

      setError(
        "Sequence number must be a positive whole number."
      );

      return;
    }


    setSavingGradeMapping(
      true
    );


    try {

      const created =
        await saveCurriculumClassGrade({

          curriculumVersionId,

          classGradeId:
            selectedClassGradeId,

          sequenceNumber:
            parsedSequence

        });


      setMessage(
        "Class Grade mapped to Curriculum Version successfully."
      );


      await refreshMappings();


      setSelectedMappedClassGradeId(
        created.classGradeId
      );

    }
    catch {

      setError(
        "Unable to map Class Grade. Check that the grade and sequence are not already mapped."
      );

    }
    finally {

      setSavingGradeMapping(
        false
      );

    }

  }


  async function archiveMapping(
    mapping: CurriculumClassGrade
  ) {

    setMessage(null);
    setError(null);

    setChangingGradeMapping(
      mapping.classGradeId
    );


    try {

      await archiveClassGradeMapping(
        curriculumVersionId,
        mapping.classGradeId
      );


      setMessage(
        "Curriculum Class Grade mapping archived successfully."
      );


      await refreshMappings();

    }
    catch {

      setError(
        "Unable to archive Curriculum Class Grade mapping."
      );

    }
    finally {

      setChangingGradeMapping(
        null
      );

    }

  }


  async function submitSubjectMapping(
    event: React.FormEvent
  ) {

    event.preventDefault();

    setMessage(null);
    setError(null);


    if (
      !curriculumVersionId
      || !selectedMappedClassGradeId
      || !selectedSubjectId
    ) {

      setError(
        "Select a mapped Class Grade and Subject."
      );

      return;
    }


    setSavingSubjectMapping(
      true
    );


    try {

      await saveCurriculumSubject({

        curriculumVersionId,

        classGradeId:
          selectedMappedClassGradeId,

        subjectId:
          selectedSubjectId

      });


      setMessage(
        "Subject mapped to Curriculum Grade successfully."
      );


      await refreshCurriculumSubjects(
        selectedMappedClassGradeId
      );

    }
    catch {

      setError(
        "Unable to map Subject to Curriculum Grade."
      );

    }
    finally {

      setSavingSubjectMapping(
        false
      );

    }

  }


  async function changeRequirement(
    mapping: CurriculumSubject,
    requirement: string
  ) {

    setMessage(null);
    setError(null);

    setChangingSubject(
      mapping.subjectId
    );


    try {

      await updateCurriculumSubjectRequirement({

        curriculumVersionId,

        classGradeId:
          mapping.classGradeId,

        subjectId:
          mapping.subjectId,

        requirement

      });


      setMessage(
        "Subject requirement updated successfully."
      );


      await refreshCurriculumSubjects(
        mapping.classGradeId
      );

    }
    catch {

      setError(
        "Unable to update Subject requirement."
      );

    }
    finally {

      setChangingSubject(
        null
      );

    }

  }


  if (!curriculumVersionId) {

    return null;

  }


  return (

    <>

      {
        message && (

          <div className="gt-card">
            {message}
          </div>

        )
      }


      {
        error && (

          <div className="gt-card">
            {error}
          </div>

        )
      }


      <div className="gt-card">

        <h3>
          Curriculum Class Grades
        </h3>


        {
          loadingDependencies
          || loadingMappings
            ? (
              <p>
                Loading curriculum grade structure...
              </p>
            )
            : !canReadMappings
              ? (
                <p>
                  Curriculum Version read access is required.
                </p>
              )
              : gradeMappings.length === 0
                ? (
                  <p>
                    No Class Grades are mapped to this Curriculum Version.
                  </p>
                )
                : (

                  <>

                    <select
                      className="gt-input"
                      value={
                        selectedMappedClassGradeId
                      }
                      onChange={
                        event =>
                          setSelectedMappedClassGradeId(
                            event.target.value
                          )
                      }
                    >

                      {
                        gradeMappings.map(
                          mapping => (

                            <option
                              key={mapping.id}
                              value={
                                mapping.classGradeId
                              }
                            >
                              {
                                `${mapping.sequenceNumber}. ${classGradeName(
                                  mapping.classGradeId
                                )} (${mapping.status ?? "ACTIVE"})`
                              }
                            </option>

                          )
                        )
                      }

                    </select>


                    <div
                      style={{
                        overflowX: "auto"
                      }}
                    >

                      <table>

                        <thead>
                          <tr>
                            <th>Sequence</th>
                            <th>Class Grade</th>
                            <th>Mandatory</th>
                            <th>Status</th>
                            {
                              canManageMappings
                              && (
                                <th>Action</th>
                              )
                            }
                          </tr>
                        </thead>


                        <tbody>

                          {
                            gradeMappings.map(
                              mapping => (

                                <tr
                                  key={mapping.id}
                                >

                                  <td>
                                    {mapping.sequenceNumber}
                                  </td>

                                  <td>
                                    {
                                      classGradeName(
                                        mapping.classGradeId
                                      )
                                    }
                                  </td>

                                  <td>
                                    {
                                      mapping.mandatoryStage
                                        ? "Yes"
                                        : "No"
                                    }
                                  </td>

                                  <td>
                                    {
                                      mapping.status
                                      ?? "ACTIVE"
                                    }
                                  </td>


                                  {
                                    canManageMappings
                                    && (

                                      <td>

                                        {
                                          mapping.status
                                          !== "ARCHIVED"
                                            ? (

                                              <button
                                                className="gt-button"
                                                type="button"
                                                disabled={
                                                  changingGradeMapping
                                                  === mapping.classGradeId
                                                }
                                                onClick={
                                                  () =>
                                                    void archiveMapping(
                                                      mapping
                                                    )
                                                }
                                              >
                                                {
                                                  changingGradeMapping
                                                  === mapping.classGradeId
                                                    ? "Archiving..."
                                                    : "Archive"
                                                }
                                              </button>

                                            )
                                            : (
                                              <span>
                                                Archived
                                              </span>
                                            )
                                        }

                                      </td>

                                    )
                                  }

                                </tr>

                              )
                            )
                          }

                        </tbody>

                      </table>

                    </div>

                  </>

                )
        }

      </div>


      {
        canCreateMappings
        && canReadGrades
        && (

          <form
            className="gt-card"
            onSubmit={
              submitGradeMapping
            }
          >

            <h3>
              Map Class Grade to Curriculum Version
            </h3>


            {
              activeEducationLevels.length === 0
                ? (
                  <p>
                    No active Education Levels are available.
                  </p>
                )
                : (

                  <select
                    className="gt-input"
                    value={
                      selectedEducationLevelId
                    }
                    onChange={
                      event =>
                        setSelectedEducationLevelId(
                          event.target.value
                        )
                    }
                  >

                    {
                      activeEducationLevels.map(
                        level => (

                          <option
                            key={level.id}
                            value={level.id}
                          >
                            {
                              `${level.levelCode} — ${level.levelName}`
                            }
                          </option>

                        )
                      )
                    }

                  </select>

                )
            }


            <select
              className="gt-input"
              value={
                selectedClassGradeId
              }
              disabled={
                !selectedEducationLevelId
                || availableGrades.length === 0
              }
              onChange={
                event =>
                  setSelectedClassGradeId(
                    event.target.value
                  )
              }
            >

              {
                availableGrades.length === 0
                  ? (
                    <option value="">
                      No unmapped Class Grades available
                    </option>
                  )
                  : availableGrades.map(
                      grade => (

                        <option
                          key={grade.id}
                          value={grade.id}
                        >
                          {
                            `${grade.classCode} — ${grade.className}`
                          }
                        </option>

                      )
                    )
              }

            </select>


            <input
              className="gt-input"
              type="number"
              min="1"
              step="1"
              placeholder="Curriculum sequence number"
              value={
                sequenceNumber
              }
              onChange={
                event =>
                  setSequenceNumber(
                    event.target.value
                  )
              }
            />


            <button
              className="gt-button"
              type="submit"
              disabled={
                savingGradeMapping
                || !selectedClassGradeId
              }
            >
              {
                savingGradeMapping
                  ? "Mapping..."
                  : "Map Class Grade"
              }
            </button>

          </form>

        )
      }


      <div className="gt-card">

        <h3>
          Curriculum Subjects by Class Grade
        </h3>


        {
          !selectedMappedClassGradeId
            ? (
              <p>
                Select a mapped Class Grade to manage its Subjects.
              </p>
            )
            : !canReadSubjects
              ? (
                <p>
                  Subject read access is required.
                </p>
              )
              : loadingCurriculumSubjects
                ? (
                  <p>
                    Loading Curriculum Subjects...
                  </p>
                )
                : curriculumSubjects.length === 0
                  ? (
                    <p>
                      No Subjects are mapped to this Class Grade.
                    </p>
                  )
                  : (

                    <div
                      style={{
                        overflowX: "auto"
                      }}
                    >

                      <table>

                        <thead>
                          <tr>
                            <th>Subject</th>
                            <th>Requirement</th>
                          </tr>
                        </thead>


                        <tbody>

                          {
                            curriculumSubjects.map(
                              mapping => (

                                <tr
                                  key={mapping.id}
                                >

                                  <td>
                                    {
                                      subjectName(
                                        mapping.subjectId
                                      )
                                    }
                                  </td>


                                  <td>

                                    {
                                      canManageSubjects
                                        ? (

                                          <select
                                            className="gt-input"
                                            value={
                                              mapping.subjectRequirement
                                            }
                                            disabled={
                                              changingSubject
                                              === mapping.subjectId
                                            }
                                            onChange={
                                              event =>
                                                void changeRequirement(
                                                  mapping,
                                                  event.target.value
                                                )
                                            }
                                          >

                                            {
                                              REQUIREMENTS.map(
                                                requirement => (

                                                  <option
                                                    key={requirement}
                                                    value={requirement}
                                                  >
                                                    {requirement}
                                                  </option>

                                                )
                                              )
                                            }

                                          </select>

                                        )
                                        : mapping.subjectRequirement
                                    }

                                  </td>

                                </tr>

                              )
                            )
                          }

                        </tbody>

                      </table>

                    </div>

                  )
        }

      </div>


      {
        canCreateSubjects
        && canReadSubjects
        && selectedMappedClassGradeId
        && (

          <form
            className="gt-card"
            onSubmit={
              submitSubjectMapping
            }
          >

            <h3>
              Map Subject to Curriculum Grade
            </h3>


            <p>
              {
                classGradeName(
                  selectedMappedClassGradeId
                )
              }
            </p>


            <select
              className="gt-input"
              value={
                selectedSubjectId
              }
              disabled={
                availableSubjects.length === 0
              }
              onChange={
                event =>
                  setSelectedSubjectId(
                    event.target.value
                  )
              }
            >

              {
                availableSubjects.length === 0
                  ? (
                    <option value="">
                      No unmapped active Subjects available
                    </option>
                  )
                  : availableSubjects.map(
                      subject => (

                        <option
                          key={subject.id}
                          value={subject.id}
                        >
                          {
                            `${subject.subjectCode} — ${subject.subjectName}`
                          }
                        </option>

                      )
                    )
              }

            </select>


            <button
              className="gt-button"
              type="submit"
              disabled={
                savingSubjectMapping
                || !selectedSubjectId
              }
            >
              {
                savingSubjectMapping
                  ? "Mapping..."
                  : "Map Subject"
              }
            </button>


            <p>
              New Subject mappings start as CORE. The requirement can then be changed using the table above.
            </p>

          </form>

        )
      }

    </>

  );

}
