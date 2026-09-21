import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi
} from "vitest";
import {
  cleanup,
  fireEvent,
  render,
  screen,
  waitFor
} from "@testing-library/react";

import CandidateScores from "./CandidateScores";

const api = vi.hoisted(() => ({
  hasPermission: vi.fn<(permission: string) => boolean>(),
  save: vi.fn(),
  load: vi.fn(),
  loadByStudent: vi.fn(),
  record: vi.fn(),
  absent: vi.fn()
}));

vi.mock("../../auth/authContext", () => ({
  useAuth: () => ({
    hasPermission: api.hasPermission
  })
}));

vi.mock("../../services/candidateScoreService", () => ({
  saveCandidateScore: api.save,
  loadCandidateScore: api.load,
  loadCandidateScoreByMarkSheetAndStudent:
    api.loadByStudent,
  recordCandidateScore: api.record,
  markScoreAbsent: api.absent
}));

const draftScore = {
  id: "score-1",
  tenantId: "tenant-1",
  markSheetId: "mark-sheet-1",
  examinationCandidateId: null,
  candidatePaperRegistrationId: null,
  studentId: "student-1",
  studentEnrollmentId: "enrollment-1",
  score: null,
  scoreStatus: "DRAFT"
};

const enteredScore = {
  ...draftScore,
  score: 72,
  scoreStatus: "ENTERED"
};

const absentScore = {
  ...draftScore,
  score: null,
  scoreStatus: "ABSENT"
};

beforeEach(() => {
  for (const mock of Object.values(api)) {
    mock.mockReset();
  }

  api.hasPermission.mockReturnValue(true);

  api.save.mockResolvedValue(draftScore);
  api.load.mockResolvedValue(draftScore);
  api.loadByStudent.mockResolvedValue(draftScore);
  api.record.mockResolvedValue(enteredScore);
  api.absent.mockResolvedValue(absentScore);
});

afterEach(cleanup);

describe("Candidate Scores", () => {
  it("enforces read, create and manage permissions independently", async () => {
    api.hasPermission.mockImplementation(
      permission =>
        permission ===
        "school.academic.assessment.read"
    );

    render(<CandidateScores />);

    expect(
      (
        screen.getByRole("button", {
          name: "Retrieve by ID"
        }) as HTMLButtonElement
      ).disabled
    ).toBe(false);

    expect(
      (
        screen.getByRole("button", {
          name: "Retrieve by Mark Sheet + Student"
        }) as HTMLButtonElement
      ).disabled
    ).toBe(false);

    expect(
      (
        screen.getByRole("button", {
          name: "Create Candidate Score"
        }) as HTMLButtonElement
      ).disabled
    ).toBe(true);

    expect(api.hasPermission).toHaveBeenCalledWith(
      "school.academic.assessment.read"
    );

    expect(api.hasPermission).toHaveBeenCalledWith(
      "school.academic.assessment.create"
    );

    expect(api.hasPermission).toHaveBeenCalledWith(
      "school.academic.assessment.manage"
    );
  });

  it("retrieves a candidate score by ID", async () => {
    render(<CandidateScores />);

    fireEvent.change(
      screen.getByPlaceholderText(
        "Candidate Score UUID"
      ),
      {
        target: {
          value: "score-1"
        }
      }
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Retrieve by ID"
      })
    );

    await waitFor(() => {
      expect(api.load).toHaveBeenCalledWith(
        "score-1"
      );
    });

    expect(
      await screen.findByText(
        "Candidate Score retrieved."
      )
    ).toBeTruthy();

    expect(
      screen.getByText("Candidate Score Details")
    ).toBeTruthy();
  });

  it("creates a candidate score record", async () => {
    render(<CandidateScores />);

    const markSheetInputs =
      screen.getAllByPlaceholderText(
        "Mark Sheet UUID"
      );

    fireEvent.change(
      markSheetInputs[1],
      {
        target: {
          value: "mark-sheet-1"
        }
      }
    );

    const studentInputs =
      screen.getAllByPlaceholderText(
        "Student UUID"
      );

    fireEvent.change(
      studentInputs[1],
      {
        target: {
          value: "student-1"
        }
      }
    );

    fireEvent.change(
      screen.getByPlaceholderText(
        "Student Enrollment UUID"
      ),
      {
        target: {
          value: "enrollment-1"
        }
      }
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Create Candidate Score"
      })
    );

    await waitFor(() => {
      expect(api.save).toHaveBeenCalledWith({
        markSheetId: "mark-sheet-1",
        examinationCandidateId: null,
        candidatePaperRegistrationId: null,
        studentId: "student-1",
        studentEnrollmentId: "enrollment-1"
      });
    });

    expect(
      await screen.findByText(
        "Candidate Score record created."
      )
    ).toBeTruthy();
  });

  it("enters a score for a mutable candidate score", async () => {
    render(<CandidateScores />);

    fireEvent.change(
      screen.getByPlaceholderText(
        "Candidate Score UUID"
      ),
      {
        target: {
          value: "score-1"
        }
      }
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Retrieve by ID"
      })
    );

    await screen.findByText(
      "Candidate Score Details"
    );

    fireEvent.change(
      screen.getByPlaceholderText(
        "Enter mark"
      ),
      {
        target: {
          value: "72"
        }
      }
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Enter Score"
      })
    );

    await waitFor(() => {
      expect(api.record).toHaveBeenCalledWith(
        "score-1",
        72
      );
    });

    expect(
      await screen.findByText(
        "Candidate score entered."
      )
    ).toBeTruthy();
  });

  it("marks a mutable candidate score absent", async () => {
    render(<CandidateScores />);

    fireEvent.change(
      screen.getByPlaceholderText(
        "Candidate Score UUID"
      ),
      {
        target: {
          value: "score-1"
        }
      }
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Retrieve by ID"
      })
    );

    await screen.findByText(
      "Candidate Score Details"
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Mark Absent"
      })
    );

    await waitFor(() => {
      expect(api.absent).toHaveBeenCalledWith(
        "score-1"
      );
    });

    expect(
      await screen.findByText(
        "Candidate marked absent."
      )
    ).toBeTruthy();
  });
});
