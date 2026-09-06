import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import apiClient from "./apiClient";

describe("apiClient session recovery", () => {

  beforeEach(() => {

    localStorage.clear();

    localStorage.setItem(
      "gt_access_token",
      "expired-access-token"
    );

    localStorage.setItem(
      "gt_refresh_token",
      "refresh-token-1"
    );

    localStorage.setItem(
      "gt_user",
      JSON.stringify({
        organisationId: "tenant-1",
      })
    );

  });


  afterEach(() => {

    vi.unstubAllGlobals();

    localStorage.clear();

  });


  it(
    "refreshes rotated credentials and retries a protected request once after 401",
    async () => {

      const fetchMock =
        vi.fn();


      fetchMock.mockResolvedValueOnce(
        new Response(
          JSON.stringify({}),
          {
            status: 401,
            headers: {
              "Content-Type":
                "application/json",
            },
          }
        )
      );


      fetchMock.mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            data: {
              accessToken:
                "rotated-access-token",
              refreshToken:
                "rotated-refresh-token",
            },
          }),
          {
            status: 200,
            headers: {
              "Content-Type":
                "application/json",
            },
          }
        )
      );


      fetchMock.mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            data: {
              value: "recovered",
            },
          }),
          {
            status: 200,
            headers: {
              "Content-Type":
                "application/json",
            },
          }
        )
      );


      vi.stubGlobal(
        "fetch",
        fetchMock
      );


      const result =
        await apiClient.get<{
          data: {
            value: string;
          };
        }>(
          "/api/v1/connect/spaces"
        );


      expect(result).toEqual({
        data: {
          value: "recovered",
        },
      });


      expect(fetchMock)
        .toHaveBeenCalledTimes(3);


      const [
        firstUrl,
        firstInit,
      ] =
        fetchMock.mock.calls[0];


      expect(
        String(firstUrl)
      ).toContain(
        "/api/v1/connect/spaces"
      );


      expect(
        (
          firstInit.headers as
            Record<string, string>
        ).Authorization
      ).toBe(
        "Bearer expired-access-token"
      );


      const [
        refreshUrl,
        refreshInit,
      ] =
        fetchMock.mock.calls[1];


      expect(
        String(refreshUrl)
      ).toContain(
        "/api/v1/eiam/auth/refresh"
      );


      expect(
        refreshInit.method
      ).toBe("POST");


      expect(
        (
          refreshInit.headers as
            Record<string, string>
        )["X-Tenant-ID"]
      ).toBe("tenant-1");


      expect(
        JSON.parse(
          String(refreshInit.body)
        )
      ).toEqual({
        refreshToken:
          "refresh-token-1",
      });


      const [
        retryUrl,
        retryInit,
      ] =
        fetchMock.mock.calls[2];


      expect(
        String(retryUrl)
      ).toContain(
        "/api/v1/connect/spaces"
      );


      expect(
        (
          retryInit.headers as
            Record<string, string>
        ).Authorization
      ).toBe(
        "Bearer rotated-access-token"
      );


      expect(
        localStorage.getItem(
          "gt_access_token"
        )
      ).toBe(
        "rotated-access-token"
      );


      expect(
        localStorage.getItem(
          "gt_refresh_token"
        )
      ).toBe(
        "rotated-refresh-token"
      );

    }
  );

});
