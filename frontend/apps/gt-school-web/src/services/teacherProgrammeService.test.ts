import {
  beforeEach,
  describe,
  expect,
  it,
  vi
} from "vitest";

const api = vi.hoisted(() => ({
  programme: vi.fn()
}));

vi.mock("./teacherProgrammeApi", () => ({
  getMyTodayProgramme:
    api.programme
}));

import {
  loadMyTodayProgramme
} from "./teacherProgrammeService";

describe("teacherProgrammeService", () => {

  beforeEach(() => {
    api.programme.mockReset();
  });

  it(
    "returns a valid direct Today Programme response",
    async () => {

      const programme = {
        date: "2026-09-19",
        zone: "Africa/Kampala",
        lessons: [],
        calendarEvents: []
      };

      api.programme.mockResolvedValue(
        programme
      );

      await expect(
        loadMyTodayProgramme()
      ).resolves.toEqual(
        programme
      );
    }
  );

  it.each([
    [
      "missing date",
      {
        zone: "Africa/Kampala",
        lessons: [],
        calendarEvents: []
      }
    ],
    [
      "missing zone",
      {
        date: "2026-09-19",
        lessons: [],
        calendarEvents: []
      }
    ],
    [
      "invalid lessons",
      {
        date: "2026-09-19",
        zone: "Africa/Kampala",
        lessons: null,
        calendarEvents: []
      }
    ],
    [
      "invalid calendar events",
      {
        date: "2026-09-19",
        zone: "Africa/Kampala",
        lessons: [],
        calendarEvents: null
      }
    ]
  ])(
    "rejects an invalid response: %s",
    async (_label, programme) => {

      api.programme.mockResolvedValue(
        programme
      );

      await expect(
        loadMyTodayProgramme()
      ).rejects.toThrow(
        "Invalid teacher programme response."
      );
    }
  );
});
