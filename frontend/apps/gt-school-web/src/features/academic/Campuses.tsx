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
  disableCampus,
  enableCampus,
  loadCampuses,
  saveCampus
} from "../../services/campusService";

import type {
  Campus,
  CreateCampusRequest
} from "../../types/campus";


const emptyForm:
  CreateCampusRequest = {

    campusCode: "",

    campusName: "",

    addressLine: "",

    district: "",

    city: "",

    countryCode: "",

    phoneNumber: "",

    email: "",

    mainCampus: false

  };


export default function Campuses() {


  const {
    hasPermission
  } = useAuth();


  const canCreate =
    hasPermission(
      Permission.CAMPUS_CREATE
    );


  const canManage =
    hasPermission(
      Permission.CAMPUS_MANAGE
    );


  const [campuses, setCampuses] =
    useState<Campus[]>([]);


  const [loading, setLoading] =
    useState(true);


  const [error, setError] =
    useState<string | null>(null);


  const [message, setMessage] =
    useState<string | null>(null);


  const [form, setForm] =
    useState<CreateCampusRequest>(
      emptyForm
    );


  const [saving, setSaving] =
    useState(false);


  const [changingCampus, setChangingCampus] =
    useState<string | null>(null);



  const refresh =
    useCallback(
      async () => {

        setError(null);

        try {

          const data =
            await loadCampuses();


          setCampuses(
            data
          );

        }
        catch {

          setError(
            "Unable to load campuses."
          );

        }
        finally {

          setLoading(
            false
          );

        }

      },
      []
    );



  useEffect(() => {

    void refresh();

  }, [refresh]);



  function updateField(
    field: keyof CreateCampusRequest,
    value: string | boolean
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
      !form.campusCode.trim()
      || !form.campusName.trim()
    ) {

      setError(
        "Campus code and campus name are required."
      );

      return;

    }


    if (
      form.countryCode
      && form.countryCode.trim().length !== 2
    ) {

      setError(
        "Country code must contain two letters."
      );

      return;

    }


    setSaving(
      true
    );


    try {

      await saveCampus({
        campusCode:
          form.campusCode.trim(),

        campusName:
          form.campusName.trim(),

        addressLine:
          form.addressLine?.trim(),

        district:
          form.district?.trim(),

        city:
          form.city?.trim(),

        countryCode:
          form.countryCode
            ?.trim()
            .toUpperCase(),

        phoneNumber:
          form.phoneNumber?.trim(),

        email:
          form.email?.trim(),

        mainCampus:
          form.mainCampus
      });


      setForm({
        ...emptyForm
      });


      setMessage(
        "Campus created successfully."
      );


      await refresh();

    }
    catch {

      setError(
        "Unable to create campus."
      );

    }
    finally {

      setSaving(
        false
      );

    }

  }



  async function changeStatus(
    campus: Campus
  ) {

    setMessage(null);
    setError(null);

    setChangingCampus(
      campus.campusCode
    );


    try {

      if (
        campus.status === "ACTIVE"
      ) {

        await disableCampus(
          campus.campusCode
        );


        setMessage(
          "Campus deactivated successfully."
        );

      }
      else {

        await enableCampus(
          campus.campusCode
        );


        setMessage(
          "Campus activated successfully."
        );

      }


      await refresh();

    }
    catch {

      setError(
        "Unable to update campus status."
      );

    }
    finally {

      setChangingCampus(
        null
      );

    }

  }



  return (

    <div className="gt-dashboard">

      <GTSection
        title="Campuses"
      >

        {
          canCreate && (

            <form
              className="gt-card"
              onSubmit={submit}
            >

              <h3>
                Create Campus
              </h3>


              <input
                className="gt-input"
                type="text"
                placeholder="Campus code e.g. MAIN"
                value={
                  form.campusCode
                }
                onChange={
                  event =>
                    updateField(
                      "campusCode",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="text"
                placeholder="Campus name"
                value={
                  form.campusName
                }
                onChange={
                  event =>
                    updateField(
                      "campusName",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="text"
                placeholder="Address"
                value={
                  form.addressLine
                }
                onChange={
                  event =>
                    updateField(
                      "addressLine",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="text"
                placeholder="District"
                value={
                  form.district
                }
                onChange={
                  event =>
                    updateField(
                      "district",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="text"
                placeholder="City"
                value={
                  form.city
                }
                onChange={
                  event =>
                    updateField(
                      "city",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="text"
                placeholder="Country code e.g. UG"
                maxLength={2}
                value={
                  form.countryCode
                }
                onChange={
                  event =>
                    updateField(
                      "countryCode",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="tel"
                placeholder="Phone number"
                value={
                  form.phoneNumber
                }
                onChange={
                  event =>
                    updateField(
                      "phoneNumber",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="email"
                placeholder="Email"
                value={
                  form.email
                }
                onChange={
                  event =>
                    updateField(
                      "email",
                      event.target.value
                    )
                }
              />


              <label>

                <input
                  type="checkbox"
                  checked={
                    form.mainCampus
                  }
                  onChange={
                    event =>
                      updateField(
                        "mainCampus",
                        event.target.checked
                      )
                  }
                />

                {" "}
                Main Campus

              </label>


              <button
                className="gt-button"
                type="submit"
                disabled={saving}
              >

                {
                  saving
                    ? "Creating..."
                    : "Create Campus"
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
            Existing Campuses
          </h3>


          {
            loading
              ? (

                <p>
                  Loading campuses...
                </p>

              )
              : campuses.length === 0
                ? (

                  <p>
                    No campuses have been created yet.
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
                            District
                          </th>

                          <th align="left">
                            City
                          </th>

                          <th align="left">
                            Country
                          </th>

                          <th align="left">
                            Main
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
                          campuses.map(
                            campus => (

                              <tr
                                key={campus.id}
                              >

                                <td>
                                  {
                                    campus.campusCode
                                  }
                                </td>

                                <td>
                                  {
                                    campus.campusName
                                  }
                                </td>

                                <td>
                                  {
                                    campus.district
                                      || "—"
                                  }
                                </td>

                                <td>
                                  {
                                    campus.city
                                      || "—"
                                  }
                                </td>

                                <td>
                                  {
                                    campus.countryCode
                                      || "—"
                                  }
                                </td>

                                <td>
                                  {
                                    campus.mainCampus
                                      ? "Yes"
                                      : "No"
                                  }
                                </td>

                                <td>
                                  {
                                    campus.status
                                  }
                                </td>


                                {
                                  canManage && (

                                    <td>

                                      <button
                                        className="gt-button"
                                        type="button"
                                        disabled={
                                          changingCampus
                                          === campus.campusCode
                                        }
                                        onClick={
                                          () =>
                                            void changeStatus(
                                              campus
                                            )
                                        }
                                      >

                                        {
                                          changingCampus
                                          === campus.campusCode
                                            ? "Updating..."
                                            : campus.status === "ACTIVE"
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
