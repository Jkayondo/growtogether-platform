import {
  useState
} from "react";

import type {
  FormEvent
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
  loadCandidateScore,
  loadCandidateScoreByMarkSheetAndStudent,
  markScoreAbsent,
  recordCandidateScore,
  saveCandidateScore
} from "../../services/candidateScoreService";

import type {
  CandidateScore,
  CreateCandidateScoreRequest
} from "../../types/candidateScore";


interface CandidateScoreForm {
  markSheetId: string;
  examinationCandidateId: string;
  candidatePaperRegistrationId: string;
  studentId: string;
  studentEnrollmentId: string;
}


const emptyForm: CandidateScoreForm = {
  markSheetId: "",
  examinationCandidateId: "",
  candidatePaperRegistrationId: "",
  studentId: "",
  studentEnrollmentId: ""
};


const gridStyle = {
  display: "grid",
  gridTemplateColumns:
    "repeat(auto-fit, minmax(220px, 1fr))",
  gap: "1rem"
};


const fieldStyle = {
  display: "grid",
  gap: "0.4rem"
};


const sectionStyle = {
  display: "grid",
  gap: "1rem",
  marginBottom: "1.5rem"
};


const detailsStyle = {
  display: "grid",
  gridTemplateColumns:
    "repeat(auto-fit, minmax(220px, 1fr))",
  gap: "0.8rem"
};


function displayValue(
  value: unknown
) {

  if (
    value === null
    || value === undefined
    || value === ""
  ) {

    return "—";

  }

  if (typeof value === "boolean") {

    return value
      ? "Yes"
      : "No";

  }

  return String(value);
}


function CandidateScoreDetails({
  candidateScore
}: {
  candidateScore: CandidateScore;
}) {

  return (

    <div style={detailsStyle}>

      <div>
        <strong>ID</strong>
        <div>
          {displayValue(candidateScore.id)}
        </div>
      </div>

      <div>
        <strong>Status</strong>
        <div>
          {displayValue(
            candidateScore.scoreStatus
          )}
        </div>
      </div>

      <div>
        <strong>Mark Sheet</strong>
        <div>
          {displayValue(
            candidateScore.markSheetId
          )}
        </div>
      </div>

      <div>
        <strong>Student</strong>
        <div>
          {displayValue(
            candidateScore.studentId
          )}
        </div>
      </div>

      <div>
        <strong>Enrollment</strong>
        <div>
          {displayValue(
            candidateScore.studentEnrollmentId
          )}
        </div>
      </div>

      <div>
        <strong>Examination Candidate</strong>
        <div>
          {displayValue(
            candidateScore.examinationCandidateId
          )}
        </div>
      </div>

      <div>
        <strong>Candidate Paper Registration</strong>
        <div>
          {displayValue(
            candidateScore.candidatePaperRegistrationId
          )}
        </div>
      </div>

      <div>
        <strong>Raw Score</strong>
        <div>
          {displayValue(
            candidateScore.rawScore
          )}
        </div>
      </div>

      <div>
        <strong>Final Score</strong>
        <div>
          {displayValue(
            candidateScore.finalScore
          )}
        </div>
      </div>

      <div>
        <strong>Absent</strong>
        <div>
          {displayValue(
            candidateScore.absent
          )}
        </div>
      </div>

      <div>
        <strong>Source</strong>
        <div>
          {displayValue(
            candidateScore.sourceType
          )}
        </div>
      </div>

      <div>
        <strong>Entered At</strong>
        <div>
          {displayValue(
            candidateScore.enteredAt
          )}
        </div>
      </div>

      <div>
        <strong>Entered By</strong>
        <div>
          {displayValue(
            candidateScore.enteredBy
          )}
        </div>
      </div>

      <div>
        <strong>Record Status</strong>
        <div>
          {displayValue(
            candidateScore.status
          )}
        </div>
      </div>

    </div>
  );
}


export default function CandidateScores() {


  const {
    hasPermission
  } = useAuth();


  const canRead =
    hasPermission(
      Permission.ASSESSMENT_READ
    );

  const canCreate =
    hasPermission(
      Permission.ASSESSMENT_CREATE
    );

  const canManage =
    hasPermission(
      Permission.ASSESSMENT_MANAGE
    );


  const [
    form,
    setForm
  ] =
    useState<CandidateScoreForm>(
      emptyForm
    );


  const [
    candidateScoreId,
    setCandidateScoreId
  ] =
    useState("");


  const [
    lookupMarkSheetId,
    setLookupMarkSheetId
  ] =
    useState("");


  const [
    lookupStudentId,
    setLookupStudentId
  ] =
    useState("");


  const [
    scoreValue,
    setScoreValue
  ] =
    useState("");


  const [
    selectedCandidateScore,
    setSelectedCandidateScore
  ] =
    useState<CandidateScore | null>(
      null
    );


  const [
    loading,
    setLoading
  ] =
    useState(false);


  const [
    creating,
    setCreating
  ] =
    useState(false);


  const [
    changingScore,
    setChangingScore
  ] =
    useState(false);


  const [
    message,
    setMessage
  ] =
    useState("");


  const [
    error,
    setError
  ] =
    useState("");


  const clearFeedback = () => {

    setMessage("");
    setError("");

  };


  const reportError = (
    caught: unknown
  ) => {

    setError(
      caught instanceof Error
        ? caught.message
        : "Candidate Score operation failed."
    );

  };


  const updateField = (
    field: keyof CandidateScoreForm,
    value: string
  ) => {

    setForm(
      current => ({
        ...current,
        [field]: value
      })
    );

  };


  const selectCandidateScore = (
    candidateScore: CandidateScore
  ) => {

    setSelectedCandidateScore(
      candidateScore
    );

    setCandidateScoreId(
      candidateScore.id ?? ""
    );

    setLookupMarkSheetId(
      candidateScore.markSheetId
    );

    setLookupStudentId(
      candidateScore.studentId
    );

  };


  const handleCreate = async (
    event: FormEvent
  ) => {

    event.preventDefault();

    clearFeedback();


    if (
      !form.markSheetId.trim()
      || !form.studentId.trim()
      || !form.studentEnrollmentId.trim()
    ) {

      setError(
        "Mark Sheet, Student and Student Enrollment IDs are required."
      );

      return;

    }


    const request:
      CreateCandidateScoreRequest = {

        markSheetId:
          form.markSheetId.trim(),

        examinationCandidateId:
          form.examinationCandidateId.trim()
            || null,

        candidatePaperRegistrationId:
          form.candidatePaperRegistrationId.trim()
            || null,

        studentId:
          form.studentId.trim(),

        studentEnrollmentId:
          form.studentEnrollmentId.trim()

      };


    try {

      setCreating(true);

      const created =
        await saveCandidateScore(
          request
        );

      selectCandidateScore(
        created
      );

      setMessage(
        "Candidate Score record created."
      );

    }
    catch (caught) {

      reportError(
        caught
      );

    }
    finally {

      setCreating(false);

    }

  };


  const handleLookupById = async () => {

    clearFeedback();

    const id =
      candidateScoreId.trim();


    if (!id) {

      setError(
        "Candidate Score ID is required."
      );

      return;

    }


    try {

      setLoading(true);

      const found =
        await loadCandidateScore(
          id
        );

      selectCandidateScore(
        found
      );

      setMessage(
        "Candidate Score retrieved."
      );

    }
    catch (caught) {

      reportError(
        caught
      );

    }
    finally {

      setLoading(false);

    }

  };


  const handleLookupByStudent = async () => {

    clearFeedback();

    const markSheetId =
      lookupMarkSheetId.trim();

    const studentId =
      lookupStudentId.trim();


    if (
      !markSheetId
      || !studentId
    ) {

      setError(
        "Mark Sheet ID and Student ID are required."
      );

      return;

    }


    try {

      setLoading(true);

      const found =
        await loadCandidateScoreByMarkSheetAndStudent(
          markSheetId,
          studentId
        );

      selectCandidateScore(
        found
      );

      setMessage(
        "Candidate Score retrieved."
      );

    }
    catch (caught) {

      reportError(
        caught
      );

    }
    finally {

      setLoading(false);

    }

  };


  const handleEnterScore = async () => {

    clearFeedback();


    if (!selectedCandidateScore?.id) {

      setError(
        "Retrieve or create a Candidate Score first."
      );

      return;

    }


    if (!scoreValue.trim()) {

      setError(
        "Score is required."
      );

      return;

    }


    const numericScore =
      Number(
        scoreValue
      );


    if (
      !Number.isFinite(
        numericScore
      )
      || numericScore < 0
    ) {

      setError(
        "Score must be zero or greater."
      );

      return;

    }


    try {

      setChangingScore(true);

      const updated =
        await recordCandidateScore(
          selectedCandidateScore.id,
          numericScore
        );

      selectCandidateScore(
        updated
      );

      setMessage(
        "Candidate score entered."
      );

    }
    catch (caught) {

      reportError(
        caught
      );

    }
    finally {

      setChangingScore(false);

    }

  };


  const handleAbsent = async () => {

    clearFeedback();


    if (!selectedCandidateScore?.id) {

      setError(
        "Retrieve or create a Candidate Score first."
      );

      return;

    }


    try {

      setChangingScore(true);

      const updated =
        await markScoreAbsent(
          selectedCandidateScore.id
        );

      selectCandidateScore(
        updated
      );

      setScoreValue("");

      setMessage(
        "Candidate marked absent."
      );

    }
    catch (caught) {

      reportError(
        caught
      );

    }
    finally {

      setChangingScore(false);

    }

  };


  const scoreEntryMutable =
    selectedCandidateScore !== null
    && (
      selectedCandidateScore.scoreStatus
        === "DRAFT"
      || selectedCandidateScore.scoreStatus
        === "ENTERED"
    );


  return (

    <GTSection title="Candidate Scores">

      <div style={sectionStyle}>

        <div>

          <h2>Candidate Score Entry</h2>

          <p>
            Register learners on an open Mark Sheet,
            retrieve their Candidate Score records,
            enter marks, or record absence.
          </p>

          <p>
            Score entry is accepted only while the
            related Mark Sheet is OPEN.
          </p>

        </div>


        {message && (

          <div role="status">
            {message}
          </div>

        )}


        {error && (

          <div role="alert">
            {error}
          </div>

        )}

      </div>


      <div style={sectionStyle}>

        <h2>Retrieve Candidate Score</h2>


        <div style={gridStyle}>

          <label style={fieldStyle}>

            Candidate Score ID

            <input
              value={candidateScoreId}
              onChange={
                event =>
                  setCandidateScoreId(
                    event.target.value
                  )
              }
              placeholder="Candidate Score UUID"
            />

          </label>


          <label style={fieldStyle}>

            Mark Sheet ID

            <input
              value={lookupMarkSheetId}
              onChange={
                event =>
                  setLookupMarkSheetId(
                    event.target.value
                  )
              }
              placeholder="Mark Sheet UUID"
            />

          </label>


          <label style={fieldStyle}>

            Student ID

            <input
              value={lookupStudentId}
              onChange={
                event =>
                  setLookupStudentId(
                    event.target.value
                  )
              }
              placeholder="Student UUID"
            />

          </label>

        </div>


        <div>

          <button
            type="button"
            disabled={
              loading
              || !canRead
            }
            onClick={
              handleLookupById
            }
          >
            {loading
              ? "Loading..."
              : "Retrieve by ID"}
          </button>

          {" "}

          <button
            type="button"
            disabled={
              loading
              || !canRead
            }
            onClick={
              handleLookupByStudent
            }
          >
            {loading
              ? "Loading..."
              : "Retrieve by Mark Sheet + Student"}
          </button>

        </div>

      </div>


      {selectedCandidateScore && (

        <div style={sectionStyle}>

          <h2>Candidate Score Details</h2>

          <CandidateScoreDetails
            candidateScore={
              selectedCandidateScore
            }
          />


          <div style={gridStyle}>

            <label style={fieldStyle}>

              Score

              <input
                type="number"
                min="0"
                step="0.01"
                value={scoreValue}
                onChange={
                  event =>
                    setScoreValue(
                      event.target.value
                    )
                }
                placeholder="Enter mark"
              />

            </label>

          </div>


          <div>

            <button
              type="button"
              disabled={
                changingScore
                || !canManage
                || !scoreEntryMutable
              }
              onClick={
                handleEnterScore
              }
            >
              {changingScore
                ? "Saving..."
                : "Enter Score"}
            </button>

            {" "}

            <button
              type="button"
              disabled={
                changingScore
                || !canManage
                || !scoreEntryMutable
              }
              onClick={
                handleAbsent
              }
            >
              Mark Absent
            </button>

          </div>

        </div>

      )}


      <div style={sectionStyle}>

        <div>

          <h2>Create Candidate Score Record</h2>

          <p>
            Register an eligible learner for manual
            score entry on an OPEN Mark Sheet.
          </p>

        </div>


        <form
          onSubmit={
            handleCreate
          }
          style={sectionStyle}
        >

          <div style={gridStyle}>

            <label style={fieldStyle}>

              Mark Sheet ID

              <input
                value={form.markSheetId}
                onChange={
                  event =>
                    updateField(
                      "markSheetId",
                      event.target.value
                    )
                }
                placeholder="Mark Sheet UUID"
                required
              />

            </label>


            <label style={fieldStyle}>

              Student ID

              <input
                value={form.studentId}
                onChange={
                  event =>
                    updateField(
                      "studentId",
                      event.target.value
                    )
                }
                placeholder="Student UUID"
                required
              />

            </label>


            <label style={fieldStyle}>

              Student Enrollment ID

              <input
                value={
                  form.studentEnrollmentId
                }
                onChange={
                  event =>
                    updateField(
                      "studentEnrollmentId",
                      event.target.value
                    )
                }
                placeholder="Student Enrollment UUID"
                required
              />

            </label>


            <label style={fieldStyle}>

              Examination Candidate ID

              <input
                value={
                  form.examinationCandidateId
                }
                onChange={
                  event =>
                    updateField(
                      "examinationCandidateId",
                      event.target.value
                    )
                }
                placeholder="Optional Examination Candidate UUID"
              />

            </label>


            <label style={fieldStyle}>

              Candidate Paper Registration ID

              <input
                value={
                  form.candidatePaperRegistrationId
                }
                onChange={
                  event =>
                    updateField(
                      "candidatePaperRegistrationId",
                      event.target.value
                    )
                }
                placeholder="Optional Candidate Paper Registration UUID"
              />

            </label>

          </div>


          <div>

            <button
              type="submit"
              disabled={
                creating
                || !canCreate
              }
            >
              {creating
                ? "Creating Candidate Score..."
                : "Create Candidate Score"}
            </button>

          </div>

        </form>

      </div>

    </GTSection>
  );
}
