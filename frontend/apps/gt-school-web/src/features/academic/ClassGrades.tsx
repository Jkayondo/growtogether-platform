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
  loadEducationLevels
} from "../../services/educationLevelService";

import {
  disableClassGrade,
  enableClassGrade,
  loadClassGrades,
  saveClassGrade
} from "../../services/classGradeService";

import type {
  EducationLevel
} from "../../types/educationLevel";

import type {
  ClassGrade
} from "../../types/classGrade";


interface ClassGradeForm {

  classCode: string;

  className: string;

  sequenceNumber: string;

  capacity: string;

}


const emptyForm:
  ClassGradeForm = {

    classCode: "",

    className: "",

    sequenceNumber: "",

    capacity: ""

  };


export default function ClassGrades() {


  const {
    hasPermission
  } = useAuth();


  const canReadCurriculum =
    hasPermission(
      Permission.CURRICULUM_READ
    );


  const canCreate =
    hasPermission(
      Permission.CLASS_GRADE_CREATE
    );


  const canManage =
    hasPermission(
      Permission.CLASS_GRADE_MANAGE
    );


  const [educationLevels, setEducationLevels] =
    useState<EducationLevel[]>([]);


  const [
    selectedEducationLevelId,
    setSelectedEducationLevelId
  ] =
    useState("");


  const [classGrades, setClassGrades] =
    useState<ClassGrade[]>([]);


  const [loadingLevels, setLoadingLevels] =
    useState(true);


  const [loadingGrades, setLoadingGrades] =
    useState(false);


  const [saving, setSaving] =
    useState(false);


  const [changingClassGrade, setChangingClassGrade] =
    useState<string | null>(
      null
    );


  const [form, setForm] =
    useState<ClassGradeForm>(
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



  const refreshClassGrades =
    useCallback(
      async (
        educationLevelId: string
      ) => {

        if (!educationLevelId) {

          setClassGrades([]);

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

        }
        catch {

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

    async function loadLevels() {

      if (!canReadCurriculum) {

        setError(
          "Education Level access is required to use Class Grades."
        );

        setLoadingLevels(
          false
        );

        return;

      }


      try {

        const levels =
          await loadEducationLevels();


        setEducationLevels(
          levels
        );


        const firstActive =
          levels.find(
            level =>
              level.status === "ACTIVE"
          )
          ?? levels[0];


        if (firstActive) {

          setSelectedEducationLevelId(
            firstActive.id
          );

        }

      }
      catch {

        setError(
          "Unable to load education levels."
        );

      }
      finally {

        setLoadingLevels(
          false
        );

      }

    }


    void loadLevels();

  }, [canReadCurriculum]);



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



  function updateField(
    field: keyof ClassGradeForm,
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
      !selectedEducationLevelId
    ) {

      setError(
        "Select an Education Level."
      );

      return;

    }


    if (
      !form.classCode.trim()
      || !form.className.trim()
      || !form.sequenceNumber.trim()
    ) {

      setError(
        "Class code, class name and sequence number are required."
      );

      return;

    }


    const sequenceNumber =
      Number(
        form.sequenceNumber
      );


    if (
      !Number.isInteger(
        sequenceNumber
      )
      || sequenceNumber <= 0
    ) {

      setError(
        "Sequence number must be a positive whole number."
      );

      return;

    }


    let capacity:
      number | undefined;


    if (
      form.capacity.trim()
    ) {

      capacity =
        Number(
          form.capacity
        );


      if (
        !Number.isInteger(
          capacity
        )
        || capacity <= 0
      ) {

        setError(
          "Capacity must be a positive whole number."
        );

        return;

      }

    }


    setSaving(
      true
    );


    try {

      await saveClassGrade({

        educationLevelId:
          selectedEducationLevelId,

        classCode:
          form.classCode
            .trim()
            .toUpperCase(),

        className:
          form.className.trim(),

        sequenceNumber,

        capacity

      });


      setForm({
        ...emptyForm
      });


      setMessage(
        "Class grade created successfully."
      );


      await refreshClassGrades(
        selectedEducationLevelId
      );

    }
    catch {

      setError(
        "Unable to create class grade."
      );

    }
    finally {

      setSaving(
        false
      );

    }

  }



  async function changeStatus(
    classGrade: ClassGrade
  ) {

    setMessage(null);
    setError(null);

    setChangingClassGrade(
      classGrade.classCode
    );


    try {

      if (
        classGrade.status === "ACTIVE"
      ) {

        await disableClassGrade(
          classGrade.classCode
        );


        setMessage(
          "Class grade deactivated successfully."
        );

      }
      else {

        await enableClassGrade(
          classGrade.classCode
        );


        setMessage(
          "Class grade activated successfully."
        );

      }


      await refreshClassGrades(
        selectedEducationLevelId
      );

    }
    catch {

      setError(
        "Unable to update class grade status."
      );

    }
    finally {

      setChangingClassGrade(
        null
      );

    }

  }



  return (

    <div className="gt-dashboard">

      <GTSection
        title="Class Grades"
      >

        <div className="gt-card">

          <h3>
            Education Level
          </h3>


          {
            loadingLevels
              ? (

                <p>
                  Loading education levels...
                </p>

              )
              : educationLevels.length === 0
                ? (

                  <p>
                    No education levels are available.
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

                )
          }

        </div>


        {
          canCreate
          && selectedEducationLevelId
          && (

            <form
              className="gt-card"
              onSubmit={submit}
            >

              <h3>
                Create Class Grade
              </h3>


              <input
                className="gt-input"
                type="text"
                placeholder="Class code e.g. P1"
                value={
                  form.classCode
                }
                onChange={
                  event =>
                    updateField(
                      "classCode",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="text"
                placeholder="Class name e.g. Primary One"
                value={
                  form.className
                }
                onChange={
                  event =>
                    updateField(
                      "className",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="number"
                min="1"
                step="1"
                placeholder="Sequence number"
                value={
                  form.sequenceNumber
                }
                onChange={
                  event =>
                    updateField(
                      "sequenceNumber",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="number"
                min="1"
                step="1"
                placeholder="Capacity (optional)"
                value={
                  form.capacity
                }
                onChange={
                  event =>
                    updateField(
                      "capacity",
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
                    : "Create Class Grade"
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
            Existing Class Grades
          </h3>


          {
            !selectedEducationLevelId
              ? (

                <p>
                  Select an Education Level to view Class Grades.
                </p>

              )
              : loadingGrades
                ? (

                  <p>
                    Loading class grades...
                  </p>

                )
                : classGrades.length === 0
                  ? (

                    <p>
                      No class grades have been created for this Education Level.
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
                              Class Grade
                            </th>

                            <th align="left">
                              Sequence
                            </th>

                            <th align="left">
                              Capacity
                            </th>

                            <th align="left">
                              Status
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
                            classGrades.map(
                              classGrade => (

                                <tr
                                  key={
                                    classGrade.id
                                  }
                                >

                                  <td>
                                    {
                                      classGrade.classCode
                                    }
                                  </td>

                                  <td>
                                    {
                                      classGrade.className
                                    }
                                  </td>

                                  <td>
                                    {
                                      classGrade.sequenceNumber
                                    }
                                  </td>

                                  <td>
                                    {
                                      classGrade.capacity
                                      ?? "—"
                                    }
                                  </td>

                                  <td>
                                    {
                                      classGrade.status
                                    }
                                  </td>


                                  {
                                    canManage && (

                                      <td>

                                        <button
                                          className="gt-button"
                                          type="button"
                                          disabled={
                                            changingClassGrade
                                            === classGrade.classCode
                                          }
                                          onClick={
                                            () =>
                                              void changeStatus(
                                                classGrade
                                              )
                                          }
                                        >

                                          {
                                            changingClassGrade
                                            === classGrade.classCode
                                              ? "Updating..."
                                              : classGrade.status === "ACTIVE"
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
