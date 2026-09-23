import {
  useCallback,
  useEffect,
  useState
} from "react";

import "./TeachingAssignments.css";

import {
  useAuth
} from "../../auth/authContext";

import {
  Permission
} from "../../auth/permissions";

import {
  loadAcademicYears
} from "../../services/academicYearService";

import {
  loadCampuses
} from "../../services/campusService";

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
  loadTeacherProfilesByStatus
} from "../../services/teacherProfileService";

import {
  cancelAssignment,
  enableTeachingAssignment,
  finishTeachingAssignment,
  loadTeachingAssignmentsByStatus,
  saveTeachingAssignment,
  submitTeachingAssignmentForApproval,
  suspendAssignment
} from "../../services/teachingAssignmentService";

import type {
  AcademicYear
} from "../../types/academicYear";

import type {
  Campus
} from "../../types/campus";

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
  TeacherProfile
} from "../../types/teacherProfile";

import type {
  TeachingAssignment
} from "../../types/teachingAssignment";


interface TeachingAssignmentForm {

  assignmentReference: string;

  teacherProfileId: string;

  academicYearId: string;

  campusId: string;

  educationLevelId: string;

  classGradeId: string;

  subjectId: string;

  assignmentType: string;

  weeklyPeriods: string;

  workloadPercentage: string;

  effectiveFrom: string;

  effectiveTo: string;

  roomReference: string;

}


const emptyForm:
  TeachingAssignmentForm = {

    assignmentReference: "",

    teacherProfileId: "",

    academicYearId: "",

    campusId: "",

    educationLevelId: "",

    classGradeId: "",

    subjectId: "",

    assignmentType:
      "PRIMARY_TEACHER",

    weeklyPeriods: "",

    workloadPercentage: "",

    effectiveFrom: "",

    effectiveTo: "",

    roomReference: ""

  };


const assignmentTypes = [
  "PRIMARY_TEACHER",
  "ASSISTANT_TEACHER",
  "SUBJECT_TEACHER",
  "RELIEF_TEACHER",
  "OTHER"
];


const assignmentStatuses = [
  "PLANNED",
  "PENDING_APPROVAL",
  "ACTIVE",
  "SUSPENDED",
  "COMPLETED",
  "CANCELLED"
];


export default function TeachingAssignments() {

  const {
    hasPermission
  } = useAuth();


  const canCreate =
    hasPermission(
      Permission.TEACHING_ASSIGNMENT_CREATE
    );


  const canManage =
    hasPermission(
      Permission.TEACHING_ASSIGNMENT_MANAGE
    );


  const [
    teachers,
    setTeachers
  ] =
    useState<TeacherProfile[]>([]);


  const [
    academicYears,
    setAcademicYears
  ] =
    useState<AcademicYear[]>([]);


  const [
    campuses,
    setCampuses
  ] =
    useState<Campus[]>([]);


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
    assignments,
    setAssignments
  ] =
    useState<TeachingAssignment[]>([]);


  const [
    form,
    setForm
  ] =
    useState<TeachingAssignmentForm>({
      ...emptyForm
    });


  const [
    loading,
    setLoading
  ] =
    useState(true);


  const [
    loadingGrades,
    setLoadingGrades
  ] =
    useState(false);


  const [
    saving,
    setSaving
  ] =
    useState(false);


  const [
    changingAssignment,
    setChangingAssignment
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


  const refreshAssignments =
    useCallback(
      async () => {

        const results =
          await Promise.all(
            assignmentStatuses.map(
              status =>
                loadTeachingAssignmentsByStatus(
                  status
                )
            )
          );


        const combined =
          results.flat();


        setAssignments(
          Array.from(
            new Map(
              combined.map(
                assignment => [
                  assignment.id,
                  assignment
                ]
              )
            ).values()
          )
        );

      },
      []
    );


  useEffect(
    () => {

      let mounted =
        true;


      async function initialise() {

        try {

          const [
            activeTeachers,
            years,
            campusData,
            levels,
            subjectData,
            statusResults
          ] =
            await Promise.all([
              loadTeacherProfilesByStatus(
                "ACTIVE"
              ),
              loadAcademicYears(),
              loadCampuses(),
              loadEducationLevels(),
              loadSubjects(),
              Promise.all(
                assignmentStatuses.map(
                  status =>
                    loadTeachingAssignmentsByStatus(
                      status
                    )
                )
              )
            ]);


          if (!mounted) {
            return;
          }


          setTeachers(
            activeTeachers
          );


          setAcademicYears(
            years
          );


          setCampuses(
            campusData
          );


          setEducationLevels(
            levels
          );


          setSubjects(
            subjectData
          );


          setAssignments(
            Array.from(
              new Map(
                statusResults
                  .flat()
                  .map(
                    assignment => [
                      assignment.id,
                      assignment
                    ]
                  )
              ).values()
            )
          );


          const currentYear =
            years.find(
              year =>
                year.currentYear
                && year.status === "ACTIVE"
            )
            ?? years.find(
              year =>
                year.status === "ACTIVE"
            );


          const firstCampus =
            campusData.find(
              campus =>
                campus.status === "ACTIVE"
            );


          const firstLevel =
            levels.find(
              level =>
                level.status === "ACTIVE"
            );


          setForm(
            current => ({
              ...current,

              teacherProfileId:
                activeTeachers[0]?.id
                ?? "",

              academicYearId:
                currentYear?.id
                ?? "",

              campusId:
                firstCampus?.id
                ?? "",

              educationLevelId:
                firstLevel?.id
                ?? "",

              subjectId:
                subjectData.find(
                  subject =>
                    subject.status === "ACTIVE"
                )?.id
                ?? ""
            })
          );

        }
        catch {

          if (mounted) {

            setError(
              "Unable to load Teaching Assignment dependencies."
            );

          }

        }
        finally {

          if (mounted) {

            setLoading(
              false
            );

          }

        }

      }


      void initialise();


      return () => {

        mounted =
          false;

      };

    },
    []
  );


  useEffect(
    () => {

      let mounted =
        true;


      async function refreshGrades() {

        if (
          !form.educationLevelId
        ) {

          setClassGrades([]);

          return;

        }


        setLoadingGrades(
          true
        );


        try {

          const grades =
            await loadClassGrades(
              form.educationLevelId
            );


          if (!mounted) {
            return;
          }


          const activeGrades =
            grades.filter(
              grade =>
                grade.status === "ACTIVE"
            );


          setClassGrades(
            activeGrades
          );


          setForm(
            current => ({
              ...current,

              classGradeId:
                activeGrades.some(
                  grade =>
                    grade.id ===
                    current.classGradeId
                )
                  ? current.classGradeId
                  : activeGrades[0]?.id
                    ?? ""
            })
          );

        }
        catch {

          if (mounted) {

            setClassGrades([]);

            setError(
              "Unable to load Class Grades."
            );

          }

        }
        finally {

          if (mounted) {

            setLoadingGrades(
              false
            );

          }

        }

      }


      void refreshGrades();


      return () => {

        mounted =
          false;

      };

    },
    [
      form.educationLevelId
    ]
  );


  function updateField(
    field: keyof TeachingAssignmentForm,
    value: string
  ) {

    setForm(
      current => ({
        ...current,
        [field]: value
      })
    );

  }


  async function submit(
    event: React.FormEvent
  ) {

    event.preventDefault();

    setMessage(null);
    setError(null);


    if (
      !form.assignmentReference.trim()
      || !form.teacherProfileId
      || !form.academicYearId
      || !form.campusId
      || !form.classGradeId
      || !form.subjectId
      || !form.weeklyPeriods.trim()
      || !form.effectiveFrom
    ) {

      setError(
        "Assignment reference, teacher, academic year, campus, class grade, subject, weekly periods and effective date are required."
      );

      return;

    }


    const weeklyPeriods =
      Number(
        form.weeklyPeriods
      );


    if (
      !Number.isInteger(
        weeklyPeriods
      )
      || weeklyPeriods <= 0
    ) {

      setError(
        "Weekly periods must be a positive whole number."
      );

      return;

    }


    let workloadPercentage:
      number | undefined;


    if (
      form.workloadPercentage.trim()
    ) {

      workloadPercentage =
        Number(
          form.workloadPercentage
        );


      if (
        !Number.isFinite(
          workloadPercentage
        )
        || workloadPercentage < 0
        || workloadPercentage > 100
      ) {

        setError(
          "Workload percentage must be between 0 and 100."
        );

        return;

      }

    }


    if (
      form.effectiveTo
      && form.effectiveTo
        < form.effectiveFrom
    ) {

      setError(
        "Effective-to date cannot be earlier than effective-from date."
      );

      return;

    }


    setSaving(
      true
    );


    try {

      await saveTeachingAssignment({

        assignmentReference:
          form.assignmentReference
            .trim()
            .toUpperCase(),

        teacherProfileId:
          form.teacherProfileId,

        academicYearId:
          form.academicYearId,

        campusId:
          form.campusId,

        classGradeId:
          form.classGradeId,

        subjectId:
          form.subjectId,

        assignmentType:
          form.assignmentType,

        weeklyPeriods,

        workloadPercentage,

        effectiveFrom:
          form.effectiveFrom,

        effectiveTo:
          form.effectiveTo
          || undefined,

        roomReference:
          form.roomReference.trim()
          || undefined

      });


      setForm(
        current => ({
          ...current,

          assignmentReference: "",

          weeklyPeriods: "",

          workloadPercentage: "",

          effectiveFrom: "",

          effectiveTo: "",

          roomReference: ""
        })
      );


      setMessage(
        "Teaching assignment created successfully."
      );


      await refreshAssignments();

    }
    catch {

      setError(
        "Unable to create Teaching Assignment."
      );

    }
    finally {

      setSaving(
        false
      );

    }

  }


  async function changeStatus(
    assignment:
      TeachingAssignment,
    action:
      "approval"
      | "activate"
      | "suspend"
      | "complete"
      | "cancel"
  ) {

    setMessage(null);
    setError(null);

    setChangingAssignment(
      assignment.id
    );


    try {

      if (
        action === "approval"
      ) {

        await submitTeachingAssignmentForApproval(
          assignment.id
        );

        setMessage(
          "Teaching assignment submitted for approval."
        );

      }
      else if (
        action === "activate"
      ) {

        await enableTeachingAssignment(
          assignment.id
        );

        setMessage(
          "Teaching assignment activated."
        );

      }
      else if (
        action === "suspend"
      ) {

        await suspendAssignment(
          assignment.id
        );

        setMessage(
          "Teaching assignment suspended."
        );

      }
      else if (
        action === "complete"
      ) {

        await finishTeachingAssignment(
          assignment.id
        );

        setMessage(
          "Teaching assignment completed."
        );

      }
      else {

        await cancelAssignment(
          assignment.id
        );

        setMessage(
          "Teaching assignment cancelled."
        );

      }


      await refreshAssignments();

    }
    catch {

      setError(
        "Unable to change Teaching Assignment status."
      );

    }
    finally {

      setChangingAssignment(
        null
      );

    }

  }


  function teacherLabel(
    teacherProfileId: string
  ) {

    const teacher =
      teachers.find(
        item =>
          item.id === teacherProfileId
      );


    return teacher
      ? teacher.teacherNumber
      : teacherProfileId;

  }


  function yearLabel(
    academicYearId: string
  ) {

    const year =
      academicYears.find(
        item =>
          item.id === academicYearId
      );


    return year
      ? year.academicYearName
      : academicYearId;

  }


  function campusLabel(
    campusId: string
  ) {

    const campus =
      campuses.find(
        item =>
          item.id === campusId
      );


    return campus
      ? campus.campusName
      : campusId;

  }


  function gradeLabel(
    classGradeId: string
  ) {

    const grade =
      classGrades.find(
        item =>
          item.id === classGradeId
      );


    return grade
      ? grade.className
      : classGradeId;

  }


  function subjectLabel(
    subjectId: string
  ) {

    const subject =
      subjects.find(
        item =>
          item.id === subjectId
      );


    return subject
      ? subject.subjectName
      : subjectId;

  }


  if (loading) {

    return (
      <div className="gt-teaching-page">
        <h1>Teaching Assignments</h1>
        <p>Loading Teaching Assignments...</p>
      </div>
    );

  }


  return (
    <div className="gt-teaching-page">

      <div className="gt-teaching-header">

        <div>
          <h1>Teaching Assignments</h1>

          <p>
            Assign verified Teacher Profiles
            to academic responsibilities.
          </p>
        </div>

      </div>


      {message && (
        <div className="gt-teaching-message">
          {message}
        </div>
      )}


      {error && (
        <div className="gt-teaching-error">
          {error}
        </div>
      )}


      {canCreate && (

        <form
          className="gt-card gt-teaching-form"
          onSubmit={submit}
        >

          <h2>
            Create Teaching Assignment
          </h2>


          <label>
            Assignment Reference
            <input
              value={
                form.assignmentReference
              }
              onChange={
                event =>
                  updateField(
                    "assignmentReference",
                    event.target.value
                  )
              }
              placeholder="e.g. TA-ENG-P5-001"
            />
          </label>


          <label>
            Teacher
            <select
              value={
                form.teacherProfileId
              }
              onChange={
                event =>
                  updateField(
                    "teacherProfileId",
                    event.target.value
                  )
              }
            >
              <option value="">
                Select teacher
              </option>

              {teachers.map(
                teacher => (
                  <option
                    key={teacher.id}
                    value={teacher.id}
                  >
                    {teacher.teacherNumber}
                    {" — "}
                    {teacher.teacherCategory}
                  </option>
                )
              )}
            </select>
          </label>


          <label>
            Academic Year
            <select
              value={
                form.academicYearId
              }
              onChange={
                event =>
                  updateField(
                    "academicYearId",
                    event.target.value
                  )
              }
            >
              <option value="">
                Select academic year
              </option>

              {academicYears
                .filter(
                  year =>
                    year.status === "ACTIVE"
                )
                .map(
                  year => (
                    <option
                      key={year.id}
                      value={year.id}
                    >
                      {year.academicYearName}
                    </option>
                  )
                )}
            </select>
          </label>


          <label>
            Campus
            <select
              value={
                form.campusId
              }
              onChange={
                event =>
                  updateField(
                    "campusId",
                    event.target.value
                  )
              }
            >
              <option value="">
                Select campus
              </option>

              {campuses
                .filter(
                  campus =>
                    campus.status === "ACTIVE"
                )
                .map(
                  campus => (
                    <option
                      key={campus.id}
                      value={campus.id}
                    >
                      {campus.campusName}
                    </option>
                  )
                )}
            </select>
          </label>


          <label>
            Education Level
            <select
              value={
                form.educationLevelId
              }
              onChange={
                event =>
                  updateField(
                    "educationLevelId",
                    event.target.value
                  )
              }
            >
              <option value="">
                Select education level
              </option>

              {educationLevels
                .filter(
                  level =>
                    level.status === "ACTIVE"
                )
                .map(
                  level => (
                    <option
                      key={level.id}
                      value={level.id}
                    >
                      {level.levelName}
                    </option>
                  )
                )}
            </select>
          </label>


          <label>
            Class Grade
            <select
              value={
                form.classGradeId
              }
              disabled={
                loadingGrades
                || !form.educationLevelId
              }
              onChange={
                event =>
                  updateField(
                    "classGradeId",
                    event.target.value
                  )
              }
            >
              <option value="">
                {loadingGrades
                  ? "Loading grades..."
                  : "Select class grade"}
              </option>

              {classGrades.map(
                grade => (
                  <option
                    key={grade.id}
                    value={grade.id}
                  >
                    {grade.className}
                  </option>
                )
              )}
            </select>
          </label>


          <label>
            Subject
            <select
              value={
                form.subjectId
              }
              onChange={
                event =>
                  updateField(
                    "subjectId",
                    event.target.value
                  )
              }
            >
              <option value="">
                Select subject
              </option>

              {subjects
                .filter(
                  subject =>
                    subject.status === "ACTIVE"
                )
                .map(
                  subject => (
                    <option
                      key={subject.id}
                      value={subject.id}
                    >
                      {subject.subjectName}
                    </option>
                  )
                )}
            </select>
          </label>


          <label>
            Assignment Type
            <select
              value={
                form.assignmentType
              }
              onChange={
                event =>
                  updateField(
                    "assignmentType",
                    event.target.value
                  )
              }
            >
              {assignmentTypes.map(
                type => (
                  <option
                    key={type}
                    value={type}
                  >
                    {type}
                  </option>
                )
              )}
            </select>
          </label>


          <label>
            Weekly Periods
            <input
              type="number"
              min="1"
              step="1"
              value={
                form.weeklyPeriods
              }
              onChange={
                event =>
                  updateField(
                    "weeklyPeriods",
                    event.target.value
                  )
              }
            />
          </label>


          <label>
            Workload %
            <input
              type="number"
              min="0"
              max="100"
              step="0.01"
              value={
                form.workloadPercentage
              }
              onChange={
                event =>
                  updateField(
                    "workloadPercentage",
                    event.target.value
                  )
              }
            />
          </label>


          <label>
            Effective From
            <input
              type="date"
              value={
                form.effectiveFrom
              }
              onChange={
                event =>
                  updateField(
                    "effectiveFrom",
                    event.target.value
                  )
              }
            />
          </label>


          <label>
            Effective To
            <input
              type="date"
              value={
                form.effectiveTo
              }
              onChange={
                event =>
                  updateField(
                    "effectiveTo",
                    event.target.value
                  )
              }
            />
          </label>


          <label>
            Room Reference
            <input
              value={
                form.roomReference
              }
              onChange={
                event =>
                  updateField(
                    "roomReference",
                    event.target.value
                  )
              }
              placeholder="Optional"
            />
          </label>


          <div className="gt-teaching-note">
            Academic Term and Stream are
            intentionally unavailable until
            their tenant-security review is
            completed.
          </div>


          <button
            type="submit"
            disabled={saving}
          >
            {saving
              ? "Creating..."
              : "Create Teaching Assignment"}
          </button>

        </form>

      )}


      <div className="gt-card gt-teaching-list">

        <h2>
          Existing Teaching Assignments
        </h2>


        {assignments.length === 0
          ? (
            <p>
              No Teaching Assignments found.
            </p>
          )
          : (

            <div className="gt-teaching-table-wrap">

              <table className="gt-teaching-table">

                <thead>
                  <tr>
                    <th>Reference</th>
                    <th>Teacher</th>
                    <th>Year</th>
                    <th>Campus</th>
                    <th>Class</th>
                    <th>Subject</th>
                    <th>Periods</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>

                <tbody>

                  {assignments.map(
                    assignment => {

                      const changing =
                        changingAssignment ===
                        assignment.id;


                      return (
                        <tr
                          key={
                            assignment.id
                          }
                        >
                          <td>
                            {
                              assignment
                                .assignmentReference
                            }
                          </td>

                          <td>
                            {
                              teacherLabel(
                                assignment
                                  .teacherProfileId
                              )
                            }
                          </td>

                          <td>
                            {
                              yearLabel(
                                assignment
                                  .academicYearId
                              )
                            }
                          </td>

                          <td>
                            {
                              campusLabel(
                                assignment
                                  .campusId
                              )
                            }
                          </td>

                          <td>
                            {
                              gradeLabel(
                                assignment
                                  .classGradeId
                              )
                            }
                          </td>

                          <td>
                            {
                              subjectLabel(
                                assignment
                                  .subjectId
                              )
                            }
                          </td>

                          <td>
                            {
                              assignment
                                .weeklyPeriods
                            }
                          </td>

                          <td>
                            {
                              assignment
                                .assignmentStatus
                            }
                          </td>

                          <td>

                            {canManage &&
                              assignment.assignmentStatus
                                === "PLANNED" && (
                                <>
                                  <button
                                    type="button"
                                    disabled={changing}
                                    onClick={
                                      () =>
                                        void changeStatus(
                                          assignment,
                                          "approval"
                                        )
                                    }
                                  >
                                    Request Approval
                                  </button>

                                  <button
                                    type="button"
                                    disabled={changing}
                                    onClick={
                                      () =>
                                        void changeStatus(
                                          assignment,
                                          "cancel"
                                        )
                                    }
                                  >
                                    Cancel
                                  </button>
                                </>
                              )}


                            {canManage &&
                              assignment.assignmentStatus
                                === "PENDING_APPROVAL" && (
                                <>
                                  <button
                                    type="button"
                                    disabled={changing}
                                    onClick={
                                      () =>
                                        void changeStatus(
                                          assignment,
                                          "activate"
                                        )
                                    }
                                  >
                                    Activate
                                  </button>

                                  <button
                                    type="button"
                                    disabled={changing}
                                    onClick={
                                      () =>
                                        void changeStatus(
                                          assignment,
                                          "cancel"
                                        )
                                    }
                                  >
                                    Cancel
                                  </button>
                                </>
                              )}


                            {canManage &&
                              assignment.assignmentStatus
                                === "ACTIVE" && (
                                <>
                                  <button
                                    type="button"
                                    disabled={changing}
                                    onClick={
                                      () =>
                                        void changeStatus(
                                          assignment,
                                          "suspend"
                                        )
                                    }
                                  >
                                    Suspend
                                  </button>

                                  <button
                                    type="button"
                                    disabled={changing}
                                    onClick={
                                      () =>
                                        void changeStatus(
                                          assignment,
                                          "complete"
                                        )
                                    }
                                  >
                                    Complete
                                  </button>

                                  <button
                                    type="button"
                                    disabled={changing}
                                    onClick={
                                      () =>
                                        void changeStatus(
                                          assignment,
                                          "cancel"
                                        )
                                    }
                                  >
                                    Cancel
                                  </button>
                                </>
                              )}


                            {canManage &&
                              assignment.assignmentStatus
                                === "SUSPENDED" && (
                                <>
                                  <button
                                    type="button"
                                    disabled={changing}
                                    onClick={
                                      () =>
                                        void changeStatus(
                                          assignment,
                                          "activate"
                                        )
                                    }
                                  >
                                    Reactivate
                                  </button>

                                  <button
                                    type="button"
                                    disabled={changing}
                                    onClick={
                                      () =>
                                        void changeStatus(
                                          assignment,
                                          "complete"
                                        )
                                    }
                                  >
                                    Complete
                                  </button>

                                  <button
                                    type="button"
                                    disabled={changing}
                                    onClick={
                                      () =>
                                        void changeStatus(
                                          assignment,
                                          "cancel"
                                        )
                                    }
                                  >
                                    Cancel
                                  </button>
                                </>
                              )}


                            {!canManage && (
                              <span>
                                Read only
                              </span>
                            )}

                          </td>

                        </tr>
                      );

                    }
                  )}

                </tbody>

              </table>

            </div>

          )}

      </div>

    </div>
  );

}
