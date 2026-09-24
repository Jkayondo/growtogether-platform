import {
  useCallback,
  useEffect,
  useState
} from "react";

import {
  useAuth
} from "../../auth/authContext";

import {
  Permission
} from "../../auth/permissions";

import {
  disableCurriculumLearningArea,
  disableSubjectCatalogueEntry,
  enableCurriculumLearningArea,
  enableSubjectCatalogueEntry,
  loadCurriculumLearningAreas,
  loadSubjectCatalogue,
  saveCurriculumLearningArea,
  saveSubjectCatalogueEntry
} from "../../services/curriculumService";

import type {
  CurriculumLearningArea,
  SubjectCatalogue
} from "../../types/curriculum";


interface Props {
  curriculumVersionId: string;
}


interface LearningAreaForm {
  learningAreaCode: string;
  learningAreaName: string;
  learningAreaType: string;
  description: string;
  sequenceNumber: string;
}


interface SubjectCatalogueForm {
  subjectCode: string;
  subjectName: string;
  subjectType: string;
  description: string;
  sequenceNumber: string;
}


const emptyLearningAreaForm:
  LearningAreaForm = {
    learningAreaCode: "",
    learningAreaName: "",
    learningAreaType: "",
    description: "",
    sequenceNumber: "1"
  };


const emptySubjectCatalogueForm:
  SubjectCatalogueForm = {
    subjectCode: "",
    subjectName: "",
    subjectType: "",
    description: "",
    sequenceNumber: "1"
  };


export default function CurriculumStructure({
  curriculumVersionId
}: Props) {

  const {
    hasPermission
  } = useAuth();


  const canReadLearningAreas =
    hasPermission(
      Permission.CURRICULUM_LEARNING_AREA_READ
    );


  const canCreateLearningAreas =
    hasPermission(
      Permission.CURRICULUM_LEARNING_AREA_CREATE
    );


  const canManageLearningAreas =
    hasPermission(
      Permission.CURRICULUM_LEARNING_AREA_MANAGE
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
    learningAreas,
    setLearningAreas
  ] =
    useState<CurriculumLearningArea[]>([]);


  const [
    subjectCatalogue,
    setSubjectCatalogue
  ] =
    useState<SubjectCatalogue[]>([]);


  const [
    selectedLearningAreaId,
    setSelectedLearningAreaId
  ] =
    useState("");


  const [
    learningAreaForm,
    setLearningAreaForm
  ] =
    useState<LearningAreaForm>({
      ...emptyLearningAreaForm
    });


  const [
    subjectForm,
    setSubjectForm
  ] =
    useState<SubjectCatalogueForm>({
      ...emptySubjectCatalogueForm
    });


  const [
    loadingLearningAreas,
    setLoadingLearningAreas
  ] =
    useState(false);


  const [
    loadingSubjects,
    setLoadingSubjects
  ] =
    useState(false);


  const [
    savingLearningArea,
    setSavingLearningArea
  ] =
    useState(false);


  const [
    savingSubject,
    setSavingSubject
  ] =
    useState(false);


  const [
    changingLearningArea,
    setChangingLearningArea
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


  const refreshLearningAreas =
    useCallback(
      async () => {

        if (
          !curriculumVersionId
          || !canReadLearningAreas
        ) {

          setLearningAreas([]);
          setSelectedLearningAreaId("");

          return;
        }


        setLoadingLearningAreas(
          true
        );


        try {

          const data =
            await loadCurriculumLearningAreas(
              curriculumVersionId
            );


          setLearningAreas(
            data
          );


          setSelectedLearningAreaId(
            currentId => {

              const current =
                data.find(
                  area =>
                    area.id === currentId
                );


              const next =
                current
                ?? data.find(
                  area =>
                    area.status === "ACTIVE"
                )
                ?? data[0];


              return next?.id ?? "";
            }
          );

        }
        catch {

          setError(
            "Unable to load learning areas."
          );

        }
        finally {

          setLoadingLearningAreas(
            false
          );

        }

      },
      [
        curriculumVersionId,
        canReadLearningAreas
      ]
    );


  const refreshSubjectCatalogue =
    useCallback(
      async () => {

        if (
          !curriculumVersionId
          || !canReadSubjects
        ) {

          setSubjectCatalogue([]);

          return;
        }


        setLoadingSubjects(
          true
        );


        try {

          const data =
            await loadSubjectCatalogue(
              curriculumVersionId
            );


          setSubjectCatalogue(
            data
          );

        }
        catch {

          setError(
            "Unable to load subject catalogue."
          );

        }
        finally {

          setLoadingSubjects(
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

    setMessage(null);
    setError(null);

    void refreshLearningAreas();
    void refreshSubjectCatalogue();

  }, [
    refreshLearningAreas,
    refreshSubjectCatalogue
  ]);


  function updateLearningAreaField(
    field: keyof LearningAreaForm,
    value: string
  ) {

    setLearningAreaForm(
      current => ({
        ...current,
        [field]: value
      })
    );

  }


  function updateSubjectField(
    field: keyof SubjectCatalogueForm,
    value: string
  ) {

    setSubjectForm(
      current => ({
        ...current,
        [field]: value
      })
    );

  }


  async function submitLearningArea(
    event: React.FormEvent
  ) {

    event.preventDefault();

    setMessage(null);
    setError(null);


    if (!curriculumVersionId) {

      setError(
        "Select a curriculum version."
      );

      return;
    }


    if (
      !learningAreaForm.learningAreaCode.trim()
      || !learningAreaForm.learningAreaName.trim()
      || !learningAreaForm.learningAreaType.trim()
    ) {

      setError(
        "Learning area code, name and type are required."
      );

      return;
    }


    const sequenceNumber =
      Number(
        learningAreaForm.sequenceNumber
      );


    if (
      !Number.isInteger(sequenceNumber)
      || sequenceNumber <= 0
    ) {

      setError(
        "Learning area sequence number must be a positive whole number."
      );

      return;
    }


    setSavingLearningArea(
      true
    );


    try {

      const created =
        await saveCurriculumLearningArea({

          curriculumVersionId,

          learningAreaCode:
            learningAreaForm
              .learningAreaCode
              .trim()
              .toUpperCase(),

          learningAreaName:
            learningAreaForm
              .learningAreaName
              .trim(),

          learningAreaType:
            learningAreaForm
              .learningAreaType
              .trim()
              .toUpperCase(),

          description:
            learningAreaForm
              .description
              .trim()
              || undefined,

          sequenceNumber

        });


      setLearningAreaForm({
        ...emptyLearningAreaForm
      });


      setMessage(
        "Learning area created successfully."
      );


      await refreshLearningAreas();


      setSelectedLearningAreaId(
        created.id
      );

    }
    catch {

      setError(
        "Unable to create learning area."
      );

    }
    finally {

      setSavingLearningArea(
        false
      );

    }

  }


  async function changeLearningAreaStatus(
    area: CurriculumLearningArea
  ) {

    setMessage(null);
    setError(null);

    setChangingLearningArea(
      area.learningAreaCode
    );


    try {

      if (
        area.status === "ACTIVE"
      ) {

        await disableCurriculumLearningArea(
          curriculumVersionId,
          area.learningAreaCode
        );


        setMessage(
          "Learning area deactivated successfully."
        );

      }
      else {

        await enableCurriculumLearningArea(
          curriculumVersionId,
          area.learningAreaCode
        );


        setMessage(
          "Learning area activated successfully."
        );

      }


      await refreshLearningAreas();

    }
    catch {

      setError(
        "Unable to update learning area status."
      );

    }
    finally {

      setChangingLearningArea(
        null
      );

    }

  }


  async function submitSubject(
    event: React.FormEvent
  ) {

    event.preventDefault();

    setMessage(null);
    setError(null);


    if (
      !curriculumVersionId
      || !selectedLearningAreaId
    ) {

      setError(
        "Select a curriculum version and learning area."
      );

      return;
    }


    if (
      !subjectForm.subjectCode.trim()
      || !subjectForm.subjectName.trim()
      || !subjectForm.subjectType.trim()
    ) {

      setError(
        "Subject code, name and type are required."
      );

      return;
    }


    const sequenceNumber =
      Number(
        subjectForm.sequenceNumber
      );


    if (
      !Number.isInteger(sequenceNumber)
      || sequenceNumber <= 0
    ) {

      setError(
        "Subject sequence number must be a positive whole number."
      );

      return;
    }


    setSavingSubject(
      true
    );


    try {

      await saveSubjectCatalogueEntry({

        curriculumVersionId,

        learningAreaId:
          selectedLearningAreaId,

        subjectCode:
          subjectForm
            .subjectCode
            .trim()
            .toUpperCase(),

        subjectName:
          subjectForm
            .subjectName
            .trim(),

        subjectType:
          subjectForm
            .subjectType
            .trim()
            .toUpperCase(),

        description:
          subjectForm
            .description
            .trim()
            || undefined,

        sequenceNumber

      });


      setSubjectForm({
        ...emptySubjectCatalogueForm
      });


      setMessage(
        "Subject catalogue entry created successfully."
      );


      await refreshSubjectCatalogue();

    }
    catch {

      setError(
        "Unable to create subject catalogue entry."
      );

    }
    finally {

      setSavingSubject(
        false
      );

    }

  }


  async function changeSubjectStatus(
    subject: SubjectCatalogue
  ) {

    setMessage(null);
    setError(null);

    setChangingSubject(
      subject.subjectCode
    );


    try {

      if (
        subject.status === "ACTIVE"
      ) {

        await disableSubjectCatalogueEntry(
          curriculumVersionId,
          subject.subjectCode
        );


        setMessage(
          "Subject catalogue entry deactivated successfully."
        );

      }
      else {

        await enableSubjectCatalogueEntry(
          curriculumVersionId,
          subject.subjectCode
        );


        setMessage(
          "Subject catalogue entry activated successfully."
        );

      }


      await refreshSubjectCatalogue();

    }
    catch {

      setError(
        "Unable to update subject catalogue status."
      );

    }
    finally {

      setChangingSubject(
        null
      );

    }

  }


  function learningAreaName(
    learningAreaId: string
  ) {

    const area =
      learningAreas.find(
        item =>
          item.id === learningAreaId
      );


    return area
      ? `${area.learningAreaCode} — ${area.learningAreaName}`
      : learningAreaId;

  }


  if (!curriculumVersionId) {

    return (

      <div className="gt-card">

        <h3>
          Curriculum Structure
        </h3>

        <p>
          Select a Curriculum Version to manage Learning Areas and its Subject Catalogue.
        </p>

      </div>

    );

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
          Learning Areas
        </h3>


        {
          !canReadLearningAreas
            ? (
              <p>
                Learning Area read access is required.
              </p>
            )
            : loadingLearningAreas
              ? (
                <p>
                  Loading learning areas...
                </p>
              )
              : learningAreas.length === 0
                ? (
                  <p>
                    No learning areas found.
                  </p>
                )
                : (

                  <>

                    <select
                      className="gt-input"
                      value={
                        selectedLearningAreaId
                      }
                      onChange={
                        event =>
                          setSelectedLearningAreaId(
                            event.target.value
                          )
                      }
                    >

                      {
                        learningAreas.map(
                          area => (

                            <option
                              key={area.id}
                              value={area.id}
                            >
                              {
                                `${area.learningAreaCode} — ${area.learningAreaName} (${area.status})`
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
                            <th>Code</th>
                            <th>Name</th>
                            <th>Type</th>
                            <th>Sequence</th>
                            <th>Status</th>
                            {
                              canManageLearningAreas
                              && (
                                <th>Action</th>
                              )
                            }
                          </tr>
                        </thead>


                        <tbody>

                          {
                            learningAreas.map(
                              area => (

                                <tr
                                  key={area.id}
                                >

                                  <td>
                                    {area.learningAreaCode}
                                  </td>

                                  <td>
                                    {area.learningAreaName}
                                  </td>

                                  <td>
                                    {area.learningAreaType}
                                  </td>

                                  <td>
                                    {area.sequenceNumber}
                                  </td>

                                  <td>
                                    {area.status}
                                  </td>


                                  {
                                    canManageLearningAreas
                                    && (

                                      <td>

                                        <button
                                          className="gt-button"
                                          type="button"
                                          disabled={
                                            changingLearningArea
                                            === area.learningAreaCode
                                          }
                                          onClick={
                                            () =>
                                              void changeLearningAreaStatus(
                                                area
                                              )
                                          }
                                        >
                                          {
                                            changingLearningArea
                                            === area.learningAreaCode
                                              ? "Updating..."
                                              : area.status === "ACTIVE"
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

                  </>

                )
        }

      </div>


      {
        canCreateLearningAreas
        && (

          <form
            className="gt-card"
            onSubmit={
              submitLearningArea
            }
          >

            <h3>
              Create Learning Area
            </h3>


            <input
              className="gt-input"
              placeholder="Code e.g. SCI"
              value={
                learningAreaForm.learningAreaCode
              }
              onChange={
                event =>
                  updateLearningAreaField(
                    "learningAreaCode",
                    event.target.value
                  )
              }
            />


            <input
              className="gt-input"
              placeholder="Learning area name"
              value={
                learningAreaForm.learningAreaName
              }
              onChange={
                event =>
                  updateLearningAreaField(
                    "learningAreaName",
                    event.target.value
                  )
              }
            />


            <input
              className="gt-input"
              placeholder="Type e.g. CORE"
              value={
                learningAreaForm.learningAreaType
              }
              onChange={
                event =>
                  updateLearningAreaField(
                    "learningAreaType",
                    event.target.value
                  )
              }
            />


            <input
              className="gt-input"
              placeholder="Description (optional)"
              value={
                learningAreaForm.description
              }
              onChange={
                event =>
                  updateLearningAreaField(
                    "description",
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
                learningAreaForm.sequenceNumber
              }
              onChange={
                event =>
                  updateLearningAreaField(
                    "sequenceNumber",
                    event.target.value
                  )
              }
            />


            <button
              className="gt-button"
              type="submit"
              disabled={
                savingLearningArea
              }
            >
              {
                savingLearningArea
                  ? "Creating..."
                  : "Create Learning Area"
              }
            </button>

          </form>

        )
      }


      <div className="gt-card">

        <h3>
          Subject Catalogue
        </h3>


        {
          !canReadSubjects
            ? (
              <p>
                Subject read access is required.
              </p>
            )
            : loadingSubjects
              ? (
                <p>
                  Loading subject catalogue...
                </p>
              )
              : subjectCatalogue.length === 0
                ? (
                  <p>
                    No subject catalogue entries found.
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
                          <th>Subject</th>
                          <th>Learning Area</th>
                          <th>Type</th>
                          <th>Sequence</th>
                          <th>Status</th>
                          {
                            canManageSubjects
                            && (
                              <th>Action</th>
                            )
                          }
                        </tr>
                      </thead>


                      <tbody>

                        {
                          subjectCatalogue.map(
                            subject => (

                              <tr
                                key={subject.id}
                              >

                                <td>
                                  {subject.subjectCode}
                                </td>

                                <td>
                                  {subject.subjectName}
                                </td>

                                <td>
                                  {
                                    learningAreaName(
                                      subject.learningAreaId
                                    )
                                  }
                                </td>

                                <td>
                                  {subject.subjectType}
                                </td>

                                <td>
                                  {subject.sequenceNumber}
                                </td>

                                <td>
                                  {subject.status}
                                </td>


                                {
                                  canManageSubjects
                                  && (

                                    <td>

                                      <button
                                        className="gt-button"
                                        type="button"
                                        disabled={
                                          changingSubject
                                          === subject.subjectCode
                                        }
                                        onClick={
                                          () =>
                                            void changeSubjectStatus(
                                              subject
                                            )
                                        }
                                      >
                                        {
                                          changingSubject
                                          === subject.subjectCode
                                            ? "Updating..."
                                            : subject.status === "ACTIVE"
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


      {
        canCreateSubjects
        && canReadLearningAreas
        && selectedLearningAreaId
        && (

          <form
            className="gt-card"
            onSubmit={
              submitSubject
            }
          >

            <h3>
              Create Subject Catalogue Entry
            </h3>


            <select
              className="gt-input"
              value={
                selectedLearningAreaId
              }
              onChange={
                event =>
                  setSelectedLearningAreaId(
                    event.target.value
                  )
              }
            >

              {
                learningAreas.map(
                  area => (

                    <option
                      key={area.id}
                      value={area.id}
                    >
                      {
                        `${area.learningAreaCode} — ${area.learningAreaName}`
                      }
                    </option>

                  )
                )
              }

            </select>


            <input
              className="gt-input"
              placeholder="Subject code e.g. MATH"
              value={
                subjectForm.subjectCode
              }
              onChange={
                event =>
                  updateSubjectField(
                    "subjectCode",
                    event.target.value
                  )
              }
            />


            <input
              className="gt-input"
              placeholder="Subject name"
              value={
                subjectForm.subjectName
              }
              onChange={
                event =>
                  updateSubjectField(
                    "subjectName",
                    event.target.value
                  )
              }
            />


            <input
              className="gt-input"
              placeholder="Subject type e.g. CORE"
              value={
                subjectForm.subjectType
              }
              onChange={
                event =>
                  updateSubjectField(
                    "subjectType",
                    event.target.value
                  )
              }
            />


            <input
              className="gt-input"
              placeholder="Description (optional)"
              value={
                subjectForm.description
              }
              onChange={
                event =>
                  updateSubjectField(
                    "description",
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
                subjectForm.sequenceNumber
              }
              onChange={
                event =>
                  updateSubjectField(
                    "sequenceNumber",
                    event.target.value
                  )
              }
            />


            <button
              className="gt-button"
              type="submit"
              disabled={
                savingSubject
              }
            >
              {
                savingSubject
                  ? "Creating..."
                  : "Create Subject"
              }
            </button>

          </form>

        )
      }

    </>

  );

}
