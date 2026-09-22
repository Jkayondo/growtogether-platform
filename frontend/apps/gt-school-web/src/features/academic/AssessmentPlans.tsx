import {
  useCallback,
  useEffect,
  useState
} from "react";

import GTSection
  from "../../components/common/GTSection";

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
  loadAssessmentPlan,
  loadAssessmentPlansByAcademicYear,
  loadAssessmentPlansByCampus,
  loadAssessmentPlansByClassGrade,
  saveAssessmentPlan
} from "../../services/assessmentPlanService";

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
  AssessmentPlan
} from "../../types/assessmentPlan";


interface AssessmentPlanForm {
  planCode: string;
  planName: string;
  description: string;

  academicYearId: string;
  campusId: string;

  educationLevelId: string;
  classGradeId: string;

  effectiveFrom: string;
  effectiveTo: string;
}


const emptyForm: AssessmentPlanForm = {
  planCode: "",
  planName: "",
  description: "",

  academicYearId: "",
  campusId: "",

  educationLevelId: "",
  classGradeId: "",

  effectiveFrom: "",
  effectiveTo: ""
};


export default function AssessmentPlans() {

  const {
    hasPermission
  } = useAuth();


  const canCreate =
    hasPermission(
      Permission.ASSESSMENT_CREATE
    );

  const canRead =
    hasPermission(
      Permission.ASSESSMENT_READ
    );


  const canReadAcademicYears =
    hasPermission(
      Permission.ACADEMIC_YEAR_READ
    );

  const canReadCampuses =
    hasPermission(
      Permission.CAMPUS_READ
    );

  const canReadCurriculum =
    hasPermission(
      Permission.CURRICULUM_READ
    );

  const canReadClassGrades =
    hasPermission(
      Permission.CLASS_GRADE_READ
    );


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
    form,
    setForm
  ] =
    useState<AssessmentPlanForm>(
      emptyForm
    );

  const [
    loadingDependencies,
    setLoadingDependencies
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
    assessmentPlanId,
    setAssessmentPlanId
  ] =
    useState("");

  const [
    viewing,
    setViewing
  ] =
    useState(false);

  const [
    viewedPlan,
    setViewedPlan
  ] =
    useState<AssessmentPlan | null>(
      null
    );


  const [
    academicYearFilterId,
    setAcademicYearFilterId
  ] =
    useState("");

  const [
    academicYearPlans,
    setAcademicYearPlans
  ] =
    useState<AssessmentPlan[]>([]);

  const [
    loadingAcademicYearPlans,
    setLoadingAcademicYearPlans
  ] =
    useState(false);


  const [
    campusFilterId,
    setCampusFilterId
  ] =
    useState("");

  const [
    campusPlans,
    setCampusPlans
  ] =
    useState<AssessmentPlan[]>([]);

  const [
    loadingCampusPlans,
    setLoadingCampusPlans
  ] =
    useState(false);


  const [
    classGradeEducationLevelId,
    setClassGradeEducationLevelId
  ] =
    useState("");

  const [
    classGradeFilterId,
    setClassGradeFilterId
  ] =
    useState("");

  const [
    classGradeFilterOptions,
    setClassGradeFilterOptions
  ] =
    useState<ClassGrade[]>([]);

  const [
    loadingClassGradeFilterOptions,
    setLoadingClassGradeFilterOptions
  ] =
    useState(false);

  const [
    classGradePlans,
    setClassGradePlans
  ] =
    useState<AssessmentPlan[]>([]);

  const [
    loadingClassGradePlans,
    setLoadingClassGradePlans
  ] =
    useState(false);

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


  const refreshClassGrades =
    useCallback(
      async (
        educationLevelId: string
      ) => {

        if (!educationLevelId) {

          setClassGrades([]);

          return;

        }


        setLoadingGrades(true);
        setError(null);


        try {

          const grades =
            await loadClassGrades(
              educationLevelId
            );


          setClassGrades(
            grades
          );

        }
        catch {

          setClassGrades([]);

          setError(
            "Unable to load class grades."
          );

        }
        finally {

          setLoadingGrades(false);

        }

      },
      []
    );


  useEffect(() => {

    async function loadDependencies() {

      if (
        !canReadAcademicYears
        || !canReadCampuses
        || !canReadCurriculum
        || !canReadClassGrades
      ) {

        setError(
          "Academic Year, Campus, Curriculum and Class Grade read access are required to create an Assessment Plan."
        );

        setLoadingDependencies(false);

        return;

      }


      try {

        const [
          years,
          campusRecords,
          levels
        ] =
          await Promise.all([
            loadAcademicYears(),
            loadCampuses(),
            loadEducationLevels()
          ]);


        setAcademicYears(
          years
        );

        setCampuses(
          campusRecords
        );

        setEducationLevels(
          levels
        );

      }
      catch {

        setError(
          "Unable to load Assessment Plan reference data."
        );

      }
      finally {

        setLoadingDependencies(false);

      }

    }


    void loadDependencies();

  }, [
    canReadAcademicYears,
    canReadCampuses,
    canReadCurriculum,
    canReadClassGrades
  ]);


  function updateField(
    field: keyof AssessmentPlanForm,
    value: string
  ) {

    setForm(
      current => ({
        ...current,
        [field]: value
      })
    );

  }


  async function handleEducationLevelChange(
    educationLevelId: string
  ) {

    setForm(
      current => ({
        ...current,
        educationLevelId,
        classGradeId: ""
      })
    );


    await refreshClassGrades(
      educationLevelId
    );

  }


  async function handleClassGradeEducationLevelChange(
    educationLevelId: string
  ) {

    setClassGradeEducationLevelId(
      educationLevelId
    );

    setClassGradeFilterId("");
    setClassGradeFilterOptions([]);
    setClassGradePlans([]);
    setError(null);


    if (!educationLevelId) {
      return;
    }


    setLoadingClassGradeFilterOptions(true);


    try {

      const grades =
        await loadClassGrades(
          educationLevelId
        );


      setClassGradeFilterOptions(
        grades
      );

    }
    catch {

      setError(
        "Unable to load Class Grades."
      );

    }
    finally {

      setLoadingClassGradeFilterOptions(false);

    }

  }


  async function handleLoadClassGradePlans() {

    setMessage(null);
    setError(null);
    setClassGradePlans([]);


    if (!canRead) {

      setError(
        "You do not have permission to view Assessment Plans."
      );

      return;

    }


    if (!classGradeFilterId) {

      setError(
        "Class Grade is required."
      );

      return;

    }


    setLoadingClassGradePlans(true);


    try {

      const plans =
        await loadAssessmentPlansByClassGrade(
          classGradeFilterId
        );


      setClassGradePlans(
        plans
      );

    }
    catch {

      setError(
        "Unable to load Assessment Plans for Class Grade."
      );

    }
    finally {

      setLoadingClassGradePlans(false);

    }

  }


  async function handleLoadCampusPlans() {

    setMessage(null);
    setError(null);
    setCampusPlans([]);


    if (!canRead) {

      setError(
        "You do not have permission to view Assessment Plans."
      );

      return;

    }


    if (!campusFilterId) {

      setError(
        "Campus is required."
      );

      return;

    }


    setLoadingCampusPlans(true);


    try {

      const plans =
        await loadAssessmentPlansByCampus(
          campusFilterId
        );


      setCampusPlans(
        plans
      );

    }
    catch {

      setError(
        "Unable to load Assessment Plans for Campus."
      );

    }
    finally {

      setLoadingCampusPlans(false);

    }

  }


  async function handleLoadAcademicYearPlans() {

    setMessage(null);
    setError(null);
    setAcademicYearPlans([]);


    if (!canRead) {

      setError(
        "You do not have permission to view Assessment Plans."
      );

      return;

    }


    if (!academicYearFilterId) {

      setError(
        "Academic Year is required."
      );

      return;

    }


    setLoadingAcademicYearPlans(true);


    try {

      const plans =
        await loadAssessmentPlansByAcademicYear(
          academicYearFilterId
        );


      setAcademicYearPlans(
        plans
      );

    }
    catch {

      setError(
        "Unable to load Assessment Plans for Academic Year."
      );

    }
    finally {

      setLoadingAcademicYearPlans(false);

    }

  }


  async function handleViewAssessmentPlan() {

    setMessage(null);
    setError(null);
    setViewedPlan(null);


    if (!canRead) {

      setError(
        "You do not have permission to view Assessment Plans."
      );

      return;

    }


    if (!assessmentPlanId.trim()) {

      setError(
        "Assessment Plan ID is required."
      );

      return;

    }


    setViewing(true);


    try {

      const plan =
        await loadAssessmentPlan(
          assessmentPlanId.trim()
        );


      setViewedPlan(
        plan
      );

    }
    catch {

      setError(
        "Unable to load Assessment Plan."
      );

    }
    finally {

      setViewing(false);

    }

  }


  async function handleSubmit(
    event: React.FormEvent<HTMLFormElement>
  ) {

    event.preventDefault();

    setMessage(null);
    setError(null);


    if (!canCreate) {

      setError(
        "You do not have permission to create Assessment Plans."
      );

      return;

    }


    if (
      !form.planCode.trim()
      || !form.planName.trim()
      || !form.academicYearId
      || !form.campusId
      || !form.classGradeId
      || !form.effectiveFrom
    ) {

      setError(
        "Plan code, plan name, Academic Year, Campus, Class Grade and effective-from date are required."
      );

      return;

    }


    if (
      form.effectiveTo
      && form.effectiveTo
        < form.effectiveFrom
    ) {

      setError(
        "Effective-to date cannot be before effective-from date."
      );

      return;

    }


    setSaving(true);


    try {

      const created =
        await saveAssessmentPlan({
          planCode:
            form.planCode.trim(),

          planName:
            form.planName.trim(),

          description:
            form.description.trim()
              || undefined,

          academicYearId:
            form.academicYearId,

          campusId:
            form.campusId,

          classGradeId:
            form.classGradeId,

          effectiveFrom:
            form.effectiveFrom,

          effectiveTo:
            form.effectiveTo
              || undefined
        });


      setMessage(
        `Assessment Plan "${created.planName}" created successfully.`
      );


      setForm(
        emptyForm
      );

      setClassGrades([]);

    }
    catch {

      setError(
        "Unable to create Assessment Plan."
      );

    }
    finally {

      setSaving(false);

    }

  }


  return (
    <GTSection
      title="Assessment Plans"
    >

      <div>

        <h2>
          View Assessment Plan
        </h2>

        <p>
          Retrieve an Assessment Plan using its unique ID.
        </p>

        <div>
          <label>
            Assessment Plan ID

            <input
              type="text"
              value={assessmentPlanId}
              onChange={event =>
                setAssessmentPlanId(
                  event.target.value
                )
              }
              placeholder="Assessment Plan UUID"
              disabled={!canRead || viewing}
            />
          </label>

          <button
            type="button"
            onClick={() =>
              void handleViewAssessmentPlan()
            }
            disabled={!canRead || viewing}
          >
            {
              viewing
                ? "Loading Assessment Plan..."
                : "View Assessment Plan"
            }
          </button>
        </div>


        {viewedPlan && (

          <div>
            <h3>
              Assessment Plan Details
            </h3>

            <p>
              <strong>Plan Code:</strong>{" "}
              {viewedPlan.planCode}
            </p>

            <p>
              <strong>Plan Name:</strong>{" "}
              {viewedPlan.planName}
            </p>

            <p>
              <strong>Status:</strong>{" "}
              {viewedPlan.planStatus}
            </p>

            <p>
              <strong>Effective From:</strong>{" "}
              {viewedPlan.effectiveFrom}
            </p>

            {viewedPlan.effectiveTo && (
              <p>
                <strong>Effective To:</strong>{" "}
                {viewedPlan.effectiveTo}
              </p>
            )}

            {viewedPlan.description && (
              <p>
                <strong>Description:</strong>{" "}
                {viewedPlan.description}
              </p>
            )}
          </div>

        )}


        <hr />


        <h2>
          Assessment Plans by Academic Year
        </h2>

        <p>
          View all Assessment Plans configured for an Academic Year.
        </p>

        <div>
          <label>
            Academic Year

            <select
              value={academicYearFilterId}
              onChange={event =>
                setAcademicYearFilterId(
                  event.target.value
                )
              }
              disabled={
                !canRead
                || loadingAcademicYearPlans
              }
            >

              <option value="">
                Select Academic Year
              </option>

              {academicYears.map(
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

          <button
            type="button"
            onClick={() =>
              void handleLoadAcademicYearPlans()
            }
            disabled={
              !canRead
              || loadingAcademicYearPlans
            }
          >
            {
              loadingAcademicYearPlans
                ? "Loading Assessment Plans..."
                : "Load Assessment Plans"
            }
          </button>
        </div>


        {
          academicYearFilterId
          && !loadingAcademicYearPlans
          && academicYearPlans.length === 0
          && (
            <p>
              No Assessment Plans found for the selected Academic Year.
            </p>
          )
        }


        {academicYearPlans.length > 0 && (

          <div>
            <h3>
              Academic Year Assessment Plans
            </h3>

            <ul>

              {academicYearPlans.map(
                plan => (
                  <li key={plan.id ?? plan.planCode}>
                    <strong>
                      {plan.planCode}
                    </strong>
                    {" — "}
                    {plan.planName}
                    {" — "}
                    {plan.planStatus}
                    {" — "}
                    {plan.effectiveFrom}
                  </li>
                )
              )}

            </ul>
          </div>

        )}


        <hr />


        <h2>
          Assessment Plans by Campus
        </h2>

        <p>
          View all Assessment Plans configured for a Campus.
        </p>

        <div>
          <label>
            Campus

            <select
              value={campusFilterId}
              onChange={event =>
                setCampusFilterId(
                  event.target.value
                )
              }
              disabled={
                !canRead
                || loadingCampusPlans
              }
            >

              <option value="">
                Select Campus
              </option>

              {campuses.map(
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

          <button
            type="button"
            onClick={() =>
              void handleLoadCampusPlans()
            }
            disabled={
              !canRead
              || loadingCampusPlans
            }
          >
            {
              loadingCampusPlans
                ? "Loading Assessment Plans..."
                : "Load Assessment Plans"
            }
          </button>
        </div>


        {
          campusFilterId
          && !loadingCampusPlans
          && campusPlans.length === 0
          && (
            <p>
              No Assessment Plans found for the selected Campus.
            </p>
          )
        }


        {campusPlans.length > 0 && (

          <div>
            <h3>
              Campus Assessment Plans
            </h3>

            <ul>

              {campusPlans.map(
                plan => (
                  <li key={plan.id ?? plan.planCode}>
                    <strong>
                      {plan.planCode}
                    </strong>
                    {" — "}
                    {plan.planName}
                    {" — "}
                    {plan.planStatus}
                    {" — "}
                    {plan.effectiveFrom}
                  </li>
                )
              )}

            </ul>
          </div>

        )}


        <hr />


        <h2>
          Assessment Plans by Class Grade
        </h2>

        <p>
          View all Assessment Plans configured for a Class Grade.
        </p>

        <div>
          <label>
            Education Level

            <select
              value={classGradeEducationLevelId}
              onChange={event =>
                void handleClassGradeEducationLevelChange(
                  event.target.value
                )
              }
              disabled={
                !canRead
                || loadingClassGradeFilterOptions
                || loadingClassGradePlans
              }
            >

              <option value="">
                Select Education Level
              </option>

              {educationLevels.map(
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
        </div>


        <div>
          <label>
            Class Grade

            <select
              value={classGradeFilterId}
              onChange={event => {

                setClassGradeFilterId(
                  event.target.value
                );

                setClassGradePlans([]);

              }}
              disabled={
                !canRead
                || !classGradeEducationLevelId
                || loadingClassGradeFilterOptions
                || loadingClassGradePlans
              }
            >

              <option value="">
                {
                  loadingClassGradeFilterOptions
                    ? "Loading Class Grades..."
                    : "Select Class Grade"
                }
              </option>

              {classGradeFilterOptions.map(
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

          <button
            type="button"
            onClick={() =>
              void handleLoadClassGradePlans()
            }
            disabled={
              !canRead
              || !classGradeFilterId
              || loadingClassGradePlans
            }
          >
            {
              loadingClassGradePlans
                ? "Loading Assessment Plans..."
                : "Load Assessment Plans"
            }
          </button>
        </div>


        {
          classGradeFilterId
          && !loadingClassGradePlans
          && classGradePlans.length === 0
          && (
            <p>
              No Assessment Plans found for the selected Class Grade.
            </p>
          )
        }


        {classGradePlans.length > 0 && (

          <div>
            <h3>
              Class Grade Assessment Plans
            </h3>

            <ul>

              {classGradePlans.map(
                plan => (
                  <li key={plan.id ?? plan.planCode}>
                    <strong>
                      {plan.planCode}
                    </strong>
                    {" — "}
                    {plan.planName}
                    {" — "}
                    {plan.planStatus}
                    {" — "}
                    {plan.effectiveFrom}
                  </li>
                )
              )}

            </ul>
          </div>

        )}


        <hr />


        <h2>
          Create Assessment Plan
        </h2>

        <p>
          Define the assessment framework for an
          Academic Year, Campus and Class Grade.
        </p>


        {error && (
          <div role="alert">
            {error}
          </div>
        )}


        {message && (
          <div role="status">
            {message}
          </div>
        )}


        {loadingDependencies ? (

          <p>
            Loading Assessment Plan reference data...
          </p>

        ) : (

          <form
            onSubmit={handleSubmit}
          >

            <div>
              <label>
                Plan Code

                <input
                  type="text"
                  value={form.planCode}
                  onChange={event =>
                    updateField(
                      "planCode",
                      event.target.value
                    )
                  }
                  placeholder="e.g. 2026-T1-P7"
                  disabled={!canCreate || saving}
                />
              </label>
            </div>


            <div>
              <label>
                Plan Name

                <input
                  type="text"
                  value={form.planName}
                  onChange={event =>
                    updateField(
                      "planName",
                      event.target.value
                    )
                  }
                  placeholder="e.g. Term One P7 Assessment Plan"
                  disabled={!canCreate || saving}
                />
              </label>
            </div>


            <div>
              <label>
                Academic Year

                <select
                  value={form.academicYearId}
                  onChange={event =>
                    updateField(
                      "academicYearId",
                      event.target.value
                    )
                  }
                  disabled={!canCreate || saving}
                >

                  <option value="">
                    Select Academic Year
                  </option>

                  {academicYears.map(
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
            </div>


            <div>
              <label>
                Campus

                <select
                  value={form.campusId}
                  onChange={event =>
                    updateField(
                      "campusId",
                      event.target.value
                    )
                  }
                  disabled={!canCreate || saving}
                >

                  <option value="">
                    Select Campus
                  </option>

                  {campuses.map(
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
            </div>


            <div>
              <label>
                Education Level

                <select
                  value={form.educationLevelId}
                  onChange={event =>
                    void handleEducationLevelChange(
                      event.target.value
                    )
                  }
                  disabled={!canCreate || saving}
                >

                  <option value="">
                    Select Education Level
                  </option>

                  {educationLevels.map(
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
            </div>


            <div>
              <label>
                Class Grade

                <select
                  value={form.classGradeId}
                  onChange={event =>
                    updateField(
                      "classGradeId",
                      event.target.value
                    )
                  }
                  disabled={
                    !canCreate
                    || saving
                    || loadingGrades
                    || !form.educationLevelId
                  }
                >

                  <option value="">
                    {
                      loadingGrades
                        ? "Loading Class Grades..."
                        : "Select Class Grade"
                    }
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
            </div>


            <div>
              <label>
                Effective From

                <input
                  type="date"
                  value={form.effectiveFrom}
                  onChange={event =>
                    updateField(
                      "effectiveFrom",
                      event.target.value
                    )
                  }
                  disabled={!canCreate || saving}
                />
              </label>
            </div>


            <div>
              <label>
                Effective To

                <input
                  type="date"
                  value={form.effectiveTo}
                  onChange={event =>
                    updateField(
                      "effectiveTo",
                      event.target.value
                    )
                  }
                  disabled={!canCreate || saving}
                />
              </label>
            </div>


            <div>
              <label>
                Description

                <textarea
                  value={form.description}
                  onChange={event =>
                    updateField(
                      "description",
                      event.target.value
                    )
                  }
                  placeholder="Optional assessment plan description"
                  disabled={!canCreate || saving}
                />
              </label>
            </div>


            <button
              type="submit"
              disabled={
                !canCreate
                || saving
              }
            >
              {
                saving
                  ? "Creating Assessment Plan..."
                  : "Create Assessment Plan"
              }
            </button>

          </form>

        )}

      </div>

    </GTSection>
  );

}
