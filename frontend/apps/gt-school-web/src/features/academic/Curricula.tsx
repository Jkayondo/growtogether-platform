import {
  useCallback,
  useEffect,
  useState
} from "react";

import GTSection
  from "../../components/common/GTSection";

import CurriculumStructure
  from "./CurriculumStructure";

import CurriculumMappings
  from "./CurriculumMappings";

import {
  useAuth
} from "../../auth/authContext";

import {
  Permission
} from "../../auth/permissions";

import {
  approveVersion,
  enableCurriculum,
  enableCurriculumVersion,
  loadCurricula,
  loadCurriculumVersions,
  saveCurriculum,
  saveCurriculumVersion
} from "../../services/curriculumService";

import type {
  Curriculum,
  CurriculumVersion
} from "../../types/curriculum";


interface CurriculumForm {
  curriculumCode: string;
  curriculumName: string;
  curriculumType: string;
}


interface CurriculumVersionForm {
  versionCode: string;
  versionName: string;
  effectiveFrom: string;
}


const emptyCurriculumForm:
  CurriculumForm = {
    curriculumCode: "",
    curriculumName: "",
    curriculumType: ""
  };


const emptyVersionForm:
  CurriculumVersionForm = {
    versionCode: "",
    versionName: "",
    effectiveFrom: ""
  };


export default function Curricula() {

  const {
    hasPermission
  } = useAuth();


  const canRead =
    hasPermission(
      Permission.CURRICULUM_READ
    );


  const canCreate =
    hasPermission(
      Permission.CURRICULUM_CREATE
    );


  const canManage =
    hasPermission(
      Permission.CURRICULUM_MANAGE
    );


  const canReadVersions =
    hasPermission(
      Permission.CURRICULUM_VERSION_READ
    );


  const canCreateVersions =
    hasPermission(
      Permission.CURRICULUM_VERSION_CREATE
    );


  const canManageVersions =
    hasPermission(
      Permission.CURRICULUM_VERSION_MANAGE
    );


  const [
    curricula,
    setCurricula
  ] =
    useState<Curriculum[]>([]);


  const [
    selectedCurriculumId,
    setSelectedCurriculumId
  ] =
    useState("");


  const [
    versions,
    setVersions
  ] =
    useState<CurriculumVersion[]>([]);


  const [
    selectedVersionId,
    setSelectedVersionId
  ] =
    useState("");


  const [
    curriculumForm,
    setCurriculumForm
  ] =
    useState<CurriculumForm>({
      ...emptyCurriculumForm
    });


  const [
    versionForm,
    setVersionForm
  ] =
    useState<CurriculumVersionForm>({
      ...emptyVersionForm
    });


  const [
    approvalReferences,
    setApprovalReferences
  ] =
    useState<Record<string, string>>({});


  const [
    loadingCurricula,
    setLoadingCurricula
  ] =
    useState(true);


  const [
    loadingVersions,
    setLoadingVersions
  ] =
    useState(false);


  const [
    savingCurriculum,
    setSavingCurriculum
  ] =
    useState(false);


  const [
    savingVersion,
    setSavingVersion
  ] =
    useState(false);


  const [
    changingCurriculum,
    setChangingCurriculum
  ] =
    useState<string | null>(
      null
    );


  const [
    changingVersion,
    setChangingVersion
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


  const selectedCurriculum =
    curricula.find(
      curriculum =>
        curriculum.id
        === selectedCurriculumId
    );


  const refreshCurricula =
    useCallback(
      async (
        preferredCurriculumId?: string
      ) => {

        setLoadingCurricula(
          true
        );

        setError(
          null
        );


        try {

          const data =
            await loadCurricula();


          setCurricula(
            data
          );


          const preferred =
            preferredCurriculumId
              ? data.find(
                  curriculum =>
                    curriculum.id
                    === preferredCurriculumId
                )
              : undefined;


          const current =
            selectedCurriculumId
              ? data.find(
                  curriculum =>
                    curriculum.id
                    === selectedCurriculumId
                )
              : undefined;


          const next =
            preferred
            ?? current
            ?? data.find(
              curriculum =>
                curriculum.curriculumStatus
                === "ACTIVE"
            )
            ?? data[0];


          setSelectedCurriculumId(
            next?.id ?? ""
          );

        }
        catch {

          setError(
            "Unable to load curricula."
          );

        }
        finally {

          setLoadingCurricula(
            false
          );

        }

      },
      [
        selectedCurriculumId
      ]
    );


  const refreshVersions =
    useCallback(
      async (
        curriculumId: string
      ) => {

        if (
          !curriculumId
          || !canReadVersions
        ) {

          setVersions([]);
          setSelectedVersionId("");

          return;

        }


        setLoadingVersions(
          true
        );

        setError(
          null
        );


        try {

          const data =
            await loadCurriculumVersions(
              curriculumId
            );


          setVersions(
            data
          );


          setSelectedVersionId(
            currentId => {

              const current =
                data.find(
                  version =>
                    version.id === currentId
                );


              const next =
                current
                ?? data.find(
                  version =>
                    version.versionStatus === "ACTIVE"
                )
                ?? data[0];


              return next?.id ?? "";
            }
          );

        }
        catch {

          setError(
            "Unable to load curriculum versions."
          );

        }
        finally {

          setLoadingVersions(
            false
          );

        }

      },
      [
        canReadVersions
      ]
    );


  useEffect(() => {

    if (!canRead) {

      setError(
        "Curriculum read access is required."
      );

      setLoadingCurricula(
        false
      );

      return;

    }


    void refreshCurricula();

  }, [
    canRead
  ]);


  useEffect(() => {

    if (
      selectedCurriculumId
    ) {

      void refreshVersions(
        selectedCurriculumId
      );

    }
    else {

      setVersions([]);

    }

  }, [
    selectedCurriculumId,
    refreshVersions
  ]);


  function updateCurriculumField(
    field: keyof CurriculumForm,
    value: string
  ) {

    setCurriculumForm(
      current => ({
        ...current,
        [field]: value
      })
    );

  }


  function updateVersionField(
    field: keyof CurriculumVersionForm,
    value: string
  ) {

    setVersionForm(
      current => ({
        ...current,
        [field]: value
      })
    );

  }


  async function submitCurriculum(
    event: React.FormEvent
  ) {

    event.preventDefault();

    setMessage(null);
    setError(null);


    if (
      !curriculumForm.curriculumCode.trim()
      || !curriculumForm.curriculumName.trim()
      || !curriculumForm.curriculumType.trim()
    ) {

      setError(
        "Curriculum code, name and type are required."
      );

      return;

    }


    setSavingCurriculum(
      true
    );


    try {

      const created =
        await saveCurriculum({

          curriculumCode:
            curriculumForm
              .curriculumCode
              .trim()
              .toUpperCase(),

          curriculumName:
            curriculumForm
              .curriculumName
              .trim(),

          curriculumType:
            curriculumForm
              .curriculumType
              .trim()
              .toUpperCase()

        });


      setCurriculumForm({
        ...emptyCurriculumForm
      });


      setMessage(
        "Curriculum created successfully."
      );


      await refreshCurricula(
        created.id
      );

    }
    catch {

      setError(
        "Unable to create curriculum."
      );

    }
    finally {

      setSavingCurriculum(
        false
      );

    }

  }


  async function activateSelectedCurriculum() {

    if (!selectedCurriculum) {
      return;
    }


    setMessage(null);
    setError(null);

    setChangingCurriculum(
      selectedCurriculum.curriculumCode
    );


    try {

      await enableCurriculum(
        selectedCurriculum.curriculumCode
      );


      setMessage(
        "Curriculum activated successfully."
      );


      await refreshCurricula(
        selectedCurriculum.id
      );

    }
    catch {

      setError(
        "Unable to activate curriculum."
      );

    }
    finally {

      setChangingCurriculum(
        null
      );

    }

  }


  async function submitVersion(
    event: React.FormEvent
  ) {

    event.preventDefault();

    setMessage(null);
    setError(null);


    if (!selectedCurriculumId) {

      setError(
        "Select a curriculum."
      );

      return;

    }


    if (
      !versionForm.versionCode.trim()
      || !versionForm.versionName.trim()
      || !versionForm.effectiveFrom
    ) {

      setError(
        "Version code, version name and effective date are required."
      );

      return;

    }


    setSavingVersion(
      true
    );


    try {

      await saveCurriculumVersion({

        curriculumId:
          selectedCurriculumId,

        versionCode:
          versionForm
            .versionCode
            .trim()
            .toUpperCase(),

        versionName:
          versionForm
            .versionName
            .trim(),

        effectiveFrom:
          versionForm.effectiveFrom

      });


      setVersionForm({
        ...emptyVersionForm
      });


      setMessage(
        "Curriculum version created successfully."
      );


      await refreshVersions(
        selectedCurriculumId
      );

    }
    catch {

      setError(
        "Unable to create curriculum version."
      );

    }
    finally {

      setSavingVersion(
        false
      );

    }

  }


  async function approveCurriculumVersion(
    version: CurriculumVersion
  ) {

    if (!selectedCurriculumId) {
      return;
    }


    const approvalReference =
      (
        approvalReferences[
          version.versionCode
        ]
        ?? ""
      ).trim();


    if (!approvalReference) {

      setError(
        "Enter an approval reference before approving the version."
      );

      return;

    }


    setMessage(null);
    setError(null);

    setChangingVersion(
      version.versionCode
    );


    try {

      await approveVersion({

        curriculumId:
          selectedCurriculumId,

        versionCode:
          version.versionCode,

        approvalReference

      });


      setApprovalReferences(
        current => ({
          ...current,
          [version.versionCode]: ""
        })
      );


      setMessage(
        "Curriculum version approved successfully."
      );


      await refreshVersions(
        selectedCurriculumId
      );

    }
    catch {

      setError(
        "Unable to approve curriculum version."
      );

    }
    finally {

      setChangingVersion(
        null
      );

    }

  }


  async function activateVersion(
    version: CurriculumVersion
  ) {

    if (!selectedCurriculumId) {
      return;
    }


    setMessage(null);
    setError(null);

    setChangingVersion(
      version.versionCode
    );


    try {

      await enableCurriculumVersion(
        selectedCurriculumId,
        version.versionCode
      );


      setMessage(
        "Curriculum version activated successfully."
      );


      await refreshVersions(
        selectedCurriculumId
      );

    }
    catch {

      setError(
        "Unable to activate curriculum version."
      );

    }
    finally {

      setChangingVersion(
        null
      );

    }

  }


  return (

    <div className="gt-dashboard">

      <GTSection
        title="Curricula"
      >

        <div className="gt-card">

          <h3>
            Curriculum
          </h3>


          {
            loadingCurricula
              ? (
                <p>
                  Loading curricula...
                </p>
              )
              : curricula.length === 0
                ? (
                  <p>
                    No curricula are available.
                  </p>
                )
                : (

                  <>

                    <select
                      className="gt-input"
                      value={
                        selectedCurriculumId
                      }
                      onChange={
                        event =>
                          setSelectedCurriculumId(
                            event.target.value
                          )
                      }
                    >

                      {
                        curricula.map(
                          curriculum => (

                            <option
                              key={curriculum.id}
                              value={curriculum.id}
                            >
                              {
                                `${curriculum.curriculumCode} — ${curriculum.curriculumName} (${curriculum.curriculumStatus})`
                              }
                            </option>

                          )
                        )
                      }

                    </select>


                    {
                      selectedCurriculum
                      && canManage
                      && selectedCurriculum
                        .curriculumStatus
                        !== "ACTIVE"
                      && (

                        <button
                          className="gt-button"
                          type="button"
                          disabled={
                            changingCurriculum
                            === selectedCurriculum
                              .curriculumCode
                          }
                          onClick={
                            () =>
                              void activateSelectedCurriculum()
                          }
                        >
                          {
                            changingCurriculum
                            === selectedCurriculum
                              .curriculumCode
                              ? "Activating..."
                              : "Activate Curriculum"
                          }
                        </button>

                      )
                    }

                  </>

                )
          }

        </div>


        {
          canCreate && (

            <form
              className="gt-card"
              onSubmit={
                submitCurriculum
              }
            >

              <h3>
                Create Curriculum
              </h3>


              <input
                className="gt-input"
                placeholder="Curriculum code e.g. UG-NCDC"
                value={
                  curriculumForm
                    .curriculumCode
                }
                onChange={
                  event =>
                    updateCurriculumField(
                      "curriculumCode",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                placeholder="Curriculum name"
                value={
                  curriculumForm
                    .curriculumName
                }
                onChange={
                  event =>
                    updateCurriculumField(
                      "curriculumName",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                placeholder="Curriculum type e.g. NATIONAL"
                value={
                  curriculumForm
                    .curriculumType
                }
                onChange={
                  event =>
                    updateCurriculumField(
                      "curriculumType",
                      event.target.value
                    )
                }
              />


              <button
                className="gt-button"
                type="submit"
                disabled={
                  savingCurriculum
                }
              >
                {
                  savingCurriculum
                    ? "Creating..."
                    : "Create Curriculum"
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
            Curriculum Versions
          </h3>


          {
            canReadVersions
            && versions.length > 0
            && (

              <select
                className="gt-input"
                value={
                  selectedVersionId
                }
                onChange={
                  event =>
                    setSelectedVersionId(
                      event.target.value
                    )
                }
              >

                {
                  versions.map(
                    version => (

                      <option
                        key={version.id}
                        value={version.id}
                      >
                        {
                          `${version.versionCode} — ${version.versionName ?? "Unnamed Version"} (${version.versionStatus})`
                        }
                      </option>

                    )
                  )
                }

              </select>

            )
          }


          {
            !selectedCurriculumId
              ? (
                <p>
                  Select a curriculum to view its versions.
                </p>
              )
              : !canReadVersions
                ? (
                  <p>
                    Curriculum Version read access is required.
                  </p>
                )
                : loadingVersions
                  ? (
                    <p>
                      Loading curriculum versions...
                    </p>
                  )
                  : versions.length === 0
                    ? (
                      <p>
                        No curriculum versions found.
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
                              <th>Code</th>
                              <th>Name</th>
                              <th>Effective From</th>
                              <th>Status</th>
                              {
                                canManageVersions
                                && (
                                  <th>Action</th>
                                )
                              }
                            </tr>
                          </thead>


                          <tbody>

                            {
                              versions.map(
                                version => (

                                  <tr
                                    key={version.id}
                                  >

                                    <td>
                                      {version.versionCode}
                                    </td>

                                    <td>
                                      {
                                        version.versionName
                                        ?? "—"
                                      }
                                    </td>

                                    <td>
                                      {version.effectiveFrom}
                                    </td>

                                    <td>
                                      {version.versionStatus}
                                    </td>


                                    {
                                      canManageVersions
                                      && (

                                        <td>

                                          {
                                            (
                                              version.versionStatus
                                              === "DRAFT"
                                              || version.versionStatus
                                              === "UNDER_REVIEW"
                                            )
                                            && (

                                              <>

                                                <input
                                                  className="gt-input"
                                                  placeholder="Approval reference"
                                                  value={
                                                    approvalReferences[
                                                      version.versionCode
                                                    ]
                                                    ?? ""
                                                  }
                                                  onChange={
                                                    event =>
                                                      setApprovalReferences(
                                                        current => ({
                                                          ...current,
                                                          [version.versionCode]:
                                                            event.target.value
                                                        })
                                                      )
                                                  }
                                                />

                                                <button
                                                  className="gt-button"
                                                  type="button"
                                                  disabled={
                                                    changingVersion
                                                    === version.versionCode
                                                  }
                                                  onClick={
                                                    () =>
                                                      void approveCurriculumVersion(
                                                        version
                                                      )
                                                  }
                                                >
                                                  {
                                                    changingVersion
                                                    === version.versionCode
                                                      ? "Approving..."
                                                      : "Approve"
                                                  }
                                                </button>

                                              </>

                                            )
                                          }


                                          {
                                            version.versionStatus
                                            === "APPROVED"
                                            && (

                                              <button
                                                className="gt-button"
                                                type="button"
                                                disabled={
                                                  changingVersion
                                                  === version.versionCode
                                                }
                                                onClick={
                                                  () =>
                                                    void activateVersion(
                                                      version
                                                    )
                                                }
                                              >
                                                {
                                                  changingVersion
                                                  === version.versionCode
                                                    ? "Activating..."
                                                    : "Activate"
                                                }
                                              </button>

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

                    )
          }

        </div>


        {
          canCreateVersions
          && selectedCurriculumId
          && (

            <form
              className="gt-card"
              onSubmit={
                submitVersion
              }
            >

              <h3>
                Create Curriculum Version
              </h3>


              <input
                className="gt-input"
                placeholder="Version code e.g. 2027-V1"
                value={
                  versionForm.versionCode
                }
                onChange={
                  event =>
                    updateVersionField(
                      "versionCode",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                placeholder="Version name"
                value={
                  versionForm.versionName
                }
                onChange={
                  event =>
                    updateVersionField(
                      "versionName",
                      event.target.value
                    )
                }
              />


              <input
                className="gt-input"
                type="date"
                value={
                  versionForm.effectiveFrom
                }
                onChange={
                  event =>
                    updateVersionField(
                      "effectiveFrom",
                      event.target.value
                    )
                }
              />


              <button
                className="gt-button"
                type="submit"
                disabled={
                  savingVersion
                }
              >
                {
                  savingVersion
                    ? "Creating..."
                    : "Create Curriculum Version"
                }
              </button>

            </form>

          )
        }


        <CurriculumStructure
          curriculumVersionId={
            selectedVersionId
          }
        />


        <CurriculumMappings
          curriculumVersionId={
            selectedVersionId
          }
        />

      </GTSection>

    </div>

  );

}
