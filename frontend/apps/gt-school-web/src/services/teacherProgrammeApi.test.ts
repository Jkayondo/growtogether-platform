import {
  beforeEach,
  describe,
  expect,
  it,
  vi
} from "vitest";

const client = vi.hoisted(() => ({
  get: vi.fn()
}));

vi.mock("./apiClient", () => ({
  default: client
}));

import {
  getMyTodayProgramme
} from "./teacherProgrammeApi";

describe("teacherProgrammeApi", () => {

  beforeEach(() => {
    client.get.mockReset();
  });

  it(
    "loads the authenticated teacher programme without caller-selected identity",
    async () => {

      const programme = {
        date: "2026-09-19",
        zone: "Africa/Kampala",
        lessons: [],
        calendarEvents: []
      };

      client.get.mockResolvedValue(
        programme
      );

      await expect(
        getMyTodayProgramme()
      ).resolves.toEqual(
        programme
      );

      expect(
        client.get
      ).toHaveBeenCalledTimes(1);

      expect(
        client.get
      ).toHaveBeenCalledWith(
        "/api/v1/school/teacher/programme/today"
      );
    }
  );
});
