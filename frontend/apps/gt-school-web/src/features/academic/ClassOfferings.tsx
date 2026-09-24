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
  disableClassOffering,
  enableClassOffering,
  loadClassOfferings,
  saveClassOffering
} from "../../services/classOfferingService";

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
  ClassOffering
} from "../../types/classOffering";


interface ClassOfferingForm {

  offeringCode: string;

  plannedCapacity: string;

  minimumEnrollment: string;

  maximumEnrollment: string;

  enrollmentOpenDate: string;

  enrollmentCloseDate: string;

}


const emptyForm:
  ClassOfferingForm = {

    offeringCode: "",

    plannedCapacity: "",

    minimumEnrollment: "",

    maximumEnrollment: "",

    enrollmentOpenDate: "",

    enrollmentCloseDate: ""

  };


export default function ClassOfferings() {

  const {
    hasPermission
  } = useAuth();


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


  const canCreate =
    hasPermission(
      Permission.CLASS_OFFERING_CREATE
    );


  const canManage =
    hasPermission(
      Permission.CLASS_OFFERING_MANAGE
    );


  const [academicYears, setAcademicYears] =
    useState<AcademicYear[]>([]);


  const [
    selectedAcademicYearId,
    setSelectedAcademicYearId
  ] =
    useState("");


  const [campuses, setCampuses] =
    useState<Campus[]>([]);


  const [
    selectedCampusId,
    setSelectedCampusId
  ] =
    useState("");


  const [
    educationLevels,
    setEducationLevels
  ] =
    useState<EducationLevel[]>([]);


  const [
    selectedEducationLevelId,
    setSelectedEducationLevelId
  ] =
    useState("");


  const [classGrades, setClassGrades] =
    useState<ClassGrade[]>([]);


  const [
    selectedClassGradeId,
    setSelectedClassGradeId
  ] =
    useState("");


  const [
    classOfferings,
    setClassOfferings
  ] =
    useState<ClassOffering[]>([]);


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
    loadingOfferings,
    setLoadingOfferings
  ] =
    useState(false);


  const [saving, setSaving] =
    useState(false);


  const [
    changingOffering,
    setChangingOffering
  ] =
    useState<string | null>(
      null
    );


  const [form, setForm] =
    useState<ClassOfferingForm>(
      emptyForm
    );


  const [message, setMessage] =
    useState<string | null>(
      null
    );


  const [error, setError] =
    useState<string | null>(
      null
    );


  const refreshClassOfferings =
    useCallback(
      async (
        academicYearId: string
      ) => {

        if (!academicYearId) {

          setClassOfferings([]);

          return;

        }


        setLoadingOfferings(
          true
        );

        setError(
          null
        );


        try {

          const data =
            await loadClassOfferings(
              academicYearId
            );


          setClassOfferings(
            data
          );

        }
        catch {

          setError(
            "Unable to load class offerings."
          );

        }
        finally {

          setLoadingOfferings(
            false
          );

        }

      },
      []
    );


  const refreshClassGrades =
    useCallback(
      async (
        educationLevelId: string
      ) => {

        if (!educationLevelId) {

          setClassGrades([]);

          setSelectedClassGradeId("");

          return;

        }


        setLoadingGrades(
          true
        );

        setError(
          null
        );


        try {

          const data =
            await loadClassGrades(
              educationLevelId
            );


          setClassGrades(
            data
          );


          const firstActive =
            data.find(
              item =>
                item.status === "ACTIVE"
            )
            ?? data[0];


          setSelectedClassGradeId(
            firstActive?.id ?? ""
          );

        }
        catch {

          setClassGrades([]);

          setSelectedClassGradeId("");

          setError(
            "Unable to load class grades."
          );

        }
        finally {

          setLoadingGrades(
            false
          );

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
          "Academic Year, Campus, Education Level and Class Grade access is required to use Class Offerings."
        );

        setLoadingDependencies(
          false
        );

        return;

      }


      try {

        const [
          years,
          campusData,
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
          campusData
        );

        setEducationLevels(
          levels
        );


        const preferredYear =
          years.find(
            year =>
              year.currentYear
          )
          ?? years.find(
            year =>
              year.status === "ACTIVE"
          )
          ?? years[0];


        const preferredCampus =
          campusData.find(
            campus =>
              campus.status === "ACTIVE"
          )
          ?? campusData[0];


        const preferredLevel =
          levels.find(
            level =>
              level.status === "ACTIVE"
          )
          ?? levels[0];


        setSelectedAcademicYearId(
          preferredYear?.id ?? ""
        );

        setSelectedCampusId(
          preferredCampus?.id ?? ""
        );

        setSelectedEducationLevelId(
          preferredLevel?.id ?? ""
        );

      }
      catch {

        setError(
          "Unable to load Class Offering dependencies."
        );

      }
      finally {

        setLoadingDependencies(
          false
        );

      }

    }


    void loadDependencies();

  }, [
    canReadAcademicYears,
    canReadCampuses,
    canReadCurriculum,
    canReadClassGrades
  ]);


  useEffect(() => {

    if (
      selectedEducationLevelId
    ) {

      void refreshClassGrades(
        selectedEducationLevelId
      );

    }

  }, [
    selectedEducationLevelId,
    refreshClassGrades
  ]);


  useEffect(() => {

    if (
      selectedAcademicYearId
    ) {

      void refreshClassOfferings(
        selectedAcademicYearId
      );

    }

  }, [
    selectedAcademicYearId,
    refreshClassOfferings
  ]);


  function updateField(
    field: keyof ClassOfferingForm,
    value: string
  ) {

    setForm(
      current => ({
        ...current,
        [field]: value
      })
    );

  }


  function optionalInteger(
    value: string,
    label: string,
    allowZero: boolean
  ): number | undefined {

    if (!value.trim()) {

      return undefined;

    }


    const number =
      Number(
        value
      );


    if (
      !Number.isInteger(number)
      || (
        allowZero
          ? number < 0
          : number <= 0
      )
    ) {

      throw new Error(
        allowZero
          ? `${label} must be zero or a positive whole number.`
          : `${label} must be a positive whole number.`
      );

    }


    return number;

  }


  async function submit(
    event: React.FormEvent
  ) {

    event.preventDefault();

    setMessage(null);

    setError(null);


    if (
      !selectedAcademicYearId
      || !selectedCampusId
      || !selectedClassGradeId
    ) {

      setError(
        "Academic Year, Campus and Class Grade are required."
      );

      return;

    }


    if (
      !form.offeringCode.trim()
    ) {

      setError(
        "Offering code is required."
      );

      return;

    }


    let plannedCapacity:
      number | undefined;

    let minimumEnrollment:
      number | undefined;

    let maximumEnrollment:
      number | undefined;


    try {

      plannedCapacity =
        optionalInteger(
          form.plannedCapacity,
          "Planned capacity",
          false
        );

      minimumEnrollment =
        optionalInteger(
          form.minimumEnrollment,
          "Minimum enrollment",
          true
        );

      maximumEnrollment =
        optionalInteger(
          form.maximumEnrollment,
          "Maximum enrollment",
          false
        );

    }
    catch (validationError) {

      setError(
        validationError instanceof Error
          ? validationError.message
          : "Invalid capacity values."
      );

      return;

    }


    if (
      minimumEnrollment !== undefined
      && maximumEnrollment !== undefined
      && maximumEnrollment
        < minimumEnrollment
    ) {

      setError(
        "Maximum enrollment cannot be less than minimum enrollment."
      );

      return;

    }


    if (
      form.enrollmentOpenDate
      && form.enrollmentCloseDate
      && form.enrollmentCloseDate
        < form.enrollmentOpenDate
    ) {

      setError(
        "Enrollment close date cannot be before the open date."
      );

      return;

    }


    setSaving(
      true
    );


    try {

      await saveClassOffering({

        offeringCode:
          form.offeringCode
            .trim()
            .toUpperCase(),

        academicYearId:
          selectedAcademicYearId,

        campusId:
          selectedCampusId,

        classGradeId:
          selectedClassGradeId,

        plannedCapacity,

        minimumEnrollment,

        maximumEnrollment,

        enrollmentOpenDate:
          form.enrollmentOpenDate
          || undefined,

        enrollmentCloseDate:
          form.enrollmentCloseDate
          || undefined

      });


      setForm({
        ...emptyForm
      });


      setMessage(
        "Class offering created successfully."
      );


      await refreshClassOfferings(
        selectedAcademicYearId
      );

    }
    catch {

      setError(
        "Unable to create class offering."
      );

    }
    finally {

      setSaving(
        false
      );

    }

  }


  async function changeStatus(
    offering: ClassOffering
  ) {

    setMessage(null);

    setError(null);

    setChangingOffering(
      offering.offeringCode
    );


    try {

      if (
        offering.offeringStatus
        === "ACTIVE"
      ) {

        await disableClassOffering(
          offering.offeringCode
        );


        setMessage(
          "Class offering deactivated successfully."
        );

      }
      else {

        await enableClassOffering(
          offering.offeringCode
        );


        setMessage(
          "Class offering activated successfully."
        );

      }


      await refreshClassOfferings(
        selectedAcademicYearId
      );

    }
    catch {

      setError(
        "Unable to update class offering status."
      );

    }
    finally {

      setChangingOffering(
        null
      );

    }

  }


  function campusName(
    campusId: string
  ) {

    const campus =
      campuses.find(
        item =>
          item.id === campusId
      );


    return campus
      ? `${campus.campusCode} — ${campus.campusName}`
      : campusId;

  }


  function classGradeName(
    classGradeId: string
  ) {

    const classGrade =
      classGrades.find(
        item =>
          item.id === classGradeId
      );


    return classGrade
      ? `${classGrade.classCode} — ${classGrade.className}`
      : classGradeId;

  }


  return (

    <div className="gt-dashboard">

      <GTSection
        title="Class Offerings"
      >

        <div className="gt-card">

          <h3>
            Academic Structure
          </h3>


          {
            loadingDependencies
              ? (

                <p>
                  Loading academic structure...
                </p>

              )
              : (

                <>

                  <select
                    className="gt-input"
                    value={
                      selectedAcademicYearId
                    }
                    onChange={
                      event =>
                        setSelectedAcademicYearId(
                          event.target.value
                        )
                    }
                  >

                    <option value="">
                      Select Academic Year
                    </option>

                    {
                      academicYears.map(
                        year => (

                          <option
                            key={year.id}
                            value={year.id}
                          >

                            {
                              year.academicYearName
                            }

                            {
                              year.status !== "ACTIVE"
                                ? ` (${year.status})`
                                : ""
                            }

                          </option>

                        )
                      )
                    }

                  </select>


                  <select
                    className="gt-input"
                    value={
                      selectedCampusId
                    }
                    onChange={
                      event =>
                        setSelectedCampusId(
                          event.target.value
                        )
                    }
                  >

                    <option value="">
                      Select Campus
                    </option>

                    {
                      campuses.map(
                        campus => (

                          <option
                            key={campus.id}
                            value={campus.id}
                          >

                            {
                              campus.campusName
                            }

                            {
                              campus.status !== "ACTIVE"
                                ? ` (${campus.status})`
                                : ""
                            }

                          </option>

                        )
                      )
                    }

                  </select>


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

                    <option value="">
                      Select Education Level
                    </option>

                    {
                      educationLevels.map(
                        level => (

                          <option
                            key={level.id}
                            value={level.id}
                          >

                            {
                              level.levelName
                            }

                            {
                              level.status !== "ACTIVE"
                                ? ` (${level.status})`
                                : ""
                            }

                          </option>

                        )
                      )
                    }

                  </select>


                  <select
                    className="gt-input"
                    value={
                      selectedClassGradeId
                    }
                    disabled={
                      loadingGrades
                      || !selectedEducationLevelId
                    }
                    onChange={
                      event =>
                        setSelectedClassGradeId(
                          event.target.value
                        )
                    }
                  >

                    <option value="">
                      {
                        loadingGrades
                          ? "Loading Class Grades..."
                          : "Select Class Grade"
                      }
                    </option>

                    {
                      classGrades.map(
                        classGrade => (

                          <option
                            key={classGrade.id}
                            value={classGrade.id}
                          >

                            {
                              `${classGrade.classCode} — ${classGrade.className}`
                            }

                            {
                              classGrade.status !== "ACTIVE"
                                ? ` (${classGrade.status})`
                                : ""
                            }

                          </option>

                        )
                      )
                    }

                  </select>

                </>

              )
          }

        </div>


        {
          canCreate
          && selectedAcademicYearId
          && selectedCampusId
          && selectedClassGradeId
          && (

            <form
              className="gt-card"
              onSubmit={submit}
            >

              <h3>
                Create Class Offering
              </h3>


              <input
                className="gt-input"
                type="text"
                placeholder="Offering code e.g. BABY-2027-MAIN"
                value={
                  form.offeringCode
                }
                onChange={
                  event =>
                    updateField(
                      "offeringCode",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="number"
                min="1"
                step="1"
                placeholder="Planned capacity (optional)"
                value={
                  form.plannedCapacity
                }
                onChange={
                  event =>
                    updateField(
                      "plannedCapacity",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="number"
                min="0"
                step="1"
                placeholder="Minimum enrollment (optional)"
                value={
                  form.minimumEnrollment
                }
                onChange={
                  event =>
                    updateField(
                      "minimumEnrollment",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="number"
                min="1"
                step="1"
                placeholder="Maximum enrollment (optional)"
                value={
                  form.maximumEnrollment
                }
                onChange={
                  event =>
                    updateField(
                      "maximumEnrollment",
                      event.target.value
                    )
                }
              />


              <label>
                Enrollment Open Date
              </label>

              <input
                className="gt-input"
                type="date"
                value={
                  form.enrollmentOpenDate
                }
                onChange={
                  event =>
                    updateField(
                      "enrollmentOpenDate",
                      event.target.value
                    )
                }
              />


              <label>
                Enrollment Close Date
              </label>

              <input
                className="gt-input"
                type="date"
                value={
                  form.enrollmentCloseDate
                }
                onChange={
                  event =>
                    updateField(
                      "enrollmentCloseDate",
                      event.target.value
                    )
                }
              />


              <button
                className="gt-button"
                type="submit"
                disabled={saving}
              >

                {
                  saving
                    ? "Creating..."
                    : "Create Class Offering"
                }

              </button>

            </form>

          )
        }


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
            Existing Class Offerings
          </h3>


          {
            !selectedAcademicYearId
              ? (

                <p>
                  Select an Academic Year to view Class Offerings.
                </p>

              )
              : loadingOfferings
                ? (

                  <p>
                    Loading class offerings...
                  </p>

                )
                : classOfferings.length === 0
                  ? (

                    <p>
                      No Class Offerings have been created for this Academic Year.
                    </p>

                  )
                  : (

                    <div
                      style={{
                        overflowX: "auto"
                      }}
                    >

                      <table
                        style={{
                          width: "100%",
                          borderCollapse: "collapse"
                        }}
                      >

                        <thead>

                          <tr>

                            <th align="left">
                              Code
                            </th>

                            <th align="left">
                              Campus
                            </th>

                            <th align="left">
                              Class Grade
                            </th>

                            <th align="left">
                              Capacity
                            </th>

                            <th align="left">
                              Enrollment
                            </th>

                            <th align="left">
                              Offering Status
                            </th>

                            {
                              canManage && (

                                <th align="left">
                                  Action
                                </th>

                              )
                            }

                          </tr>

                        </thead>


                        <tbody>

                          {
                            classOfferings.map(
                              offering => (

                                <tr
                                  key={offering.id}
                                >

                                  <td>
                                    {
                                      offering.offeringCode
                                    }
                                  </td>

                                  <td>
                                    {
                                      campusName(
                                        offering.campusId
                                      )
                                    }
                                  </td>

                                  <td>
                                    {
                                      classGradeName(
                                        offering.classGradeId
                                      )
                                    }
                                  </td>

                                  <td>
                                    {
                                      offering.plannedCapacity
                                      ?? "—"
                                    }
                                  </td>

                                  <td>
                                    {
                                      `${
                                        offering.minimumEnrollment
                                        ?? "—"
                                      } / ${
                                        offering.maximumEnrollment
                                        ?? "—"
                                      }`
                                    }
                                  </td>

                                  <td>
                                    {
                                      offering.offeringStatus
                                    }
                                  </td>


                                  {
                                    canManage && (

                                      <td>

                                        <button
                                          className="gt-button"
                                          type="button"
                                          disabled={
                                            changingOffering
                                            === offering.offeringCode
                                          }
                                          onClick={
                                            () =>
                                              void changeStatus(
                                                offering
                                              )
                                          }
                                        >

                                          {
                                            changingOffering
                                            === offering.offeringCode
                                              ? "Updating..."
                                              : offering.offeringStatus
                                                === "ACTIVE"
                                                ? "Deactivate"
                                                : "Activate"
                                          }

                                        </button>

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

                  )
          }

        </div>

      </GTSection>

    </div>

  );

}
