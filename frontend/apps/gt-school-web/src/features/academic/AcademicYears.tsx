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
  loadAcademicYears,
  saveAcademicYear
} from "../../services/academicYearService";

import type {
  AcademicYear,
  CreateAcademicYearRequest
} from "../../types/academicYear";


const emptyForm:
  CreateAcademicYearRequest = {

    academicYearCode: "",

    academicYearName: "",

    startDate: "",

    endDate: ""

  };


export default function AcademicYears() {


  const {
    hasPermission
  } = useAuth();


  const canCreate =
    hasPermission(
      Permission.ACADEMIC_YEAR_CREATE
    );


  const [years, setYears] =
    useState<AcademicYear[]>([]);


  const [loading, setLoading] =
    useState(true);


  const [error, setError] =
    useState<string | null>(null);


  const [form, setForm] =
    useState<CreateAcademicYearRequest>(
      emptyForm
    );


  const [saving, setSaving] =
    useState(false);


  const [message, setMessage] =
    useState<string | null>(null);



  const refresh =
    useCallback(
      async () => {

        setError(null);

        try {

          const data =
            await loadAcademicYears();


          setYears(data);

        }
        catch {

          setError(
            "Unable to load academic years."
          );

        }
        finally {

          setLoading(false);

        }

      },
      []
    );



  useEffect(() => {

    void refresh();

  }, [refresh]);



  function updateField(
    field: keyof CreateAcademicYearRequest,
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
      !form.academicYearCode.trim() ||
      !form.academicYearName.trim() ||
      !form.startDate ||
      !form.endDate
    ) {

      setError(
        "Complete all Academic Year fields."
      );

      return;

    }


    if (
      form.endDate <=
      form.startDate
    ) {

      setError(
        "Academic year end date must be after start date."
      );

      return;

    }


    setSaving(true);


    try {

      await saveAcademicYear({
        academicYearCode:
          form.academicYearCode.trim(),

        academicYearName:
          form.academicYearName.trim(),

        startDate:
          form.startDate,

        endDate:
          form.endDate
      });


      setForm(
        emptyForm
      );


      setMessage(
        "Academic year created successfully."
      );


      await refresh();

    }
    catch {

      setError(
        "Unable to create academic year."
      );

    }
    finally {

      setSaving(false);

    }

  }



  return (

    <div className="gt-dashboard">

      <GTSection
        title="Academic Years"
      >

        {
          canCreate && (

            <form
              className="gt-card"
              onSubmit={submit}
            >

              <h3>
                Create Academic Year
              </h3>


              <input
                className="gt-input"
                type="text"
                placeholder="Year code e.g. 2027"
                value={
                  form.academicYearCode
                }
                onChange={
                  event =>
                    updateField(
                      "academicYearCode",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="text"
                placeholder="Academic year name"
                value={
                  form.academicYearName
                }
                onChange={
                  event =>
                    updateField(
                      "academicYearName",
                      event.target.value
                    )
                }
              />


              <label>
                Start date
              </label>

              <input
                className="gt-input"
                type="date"
                value={
                  form.startDate
                }
                onChange={
                  event =>
                    updateField(
                      "startDate",
                      event.target.value
                    )
                }
              />


              <label>
                End date
              </label>

              <input
                className="gt-input"
                type="date"
                value={
                  form.endDate
                }
                onChange={
                  event =>
                    updateField(
                      "endDate",
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
                    : "Create Academic Year"
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
            Existing Academic Years
          </h3>


          {
            loading
              ? (

                <p>
                  Loading academic years...
                </p>

              )
              : years.length === 0
                ? (

                  <p>
                    No academic years have been created yet.
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
                            Academic Year
                          </th>

                          <th align="left">
                            Start
                          </th>

                          <th align="left">
                            End
                          </th>

                          <th align="left">
                            Status
                          </th>

                          <th align="left">
                            Current
                          </th>

                        </tr>

                      </thead>


                      <tbody>

                        {
                          years.map(
                            year => (

                              <tr
                                key={year.id}
                              >

                                <td>
                                  {
                                    year.academicYearCode
                                  }
                                </td>

                                <td>
                                  {
                                    year.academicYearName
                                  }
                                </td>

                                <td>
                                  {
                                    year.startDate
                                  }
                                </td>

                                <td>
                                  {
                                    year.endDate
                                  }
                                </td>

                                <td>
                                  {
                                    year.status
                                  }
                                </td>

                                <td>
                                  {
                                    year.currentYear
                                      ? "Yes"
                                      : "No"
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

      </GTSection>

    </div>

  );

}
