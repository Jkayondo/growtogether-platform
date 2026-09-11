const API_BASE_URL =
  import.meta.env.VITE_API_URL ||
  window.location.origin;


const BOOTSTRAP_TENANT_ID =
  import.meta.env.VITE_TENANT_ID;


const getTenantId = () => {

  const storedUser =
    localStorage.getItem(
      "gt_user"
    );


  if (storedUser) {

    try {

      const user =
        JSON.parse(
          storedUser
        ) as {
          organisationId?: string;
        };


      if (user.organisationId) {

        return user.organisationId;

      }

    }
    catch {

      // Ignore an invalid stored session and
      // fall back to the configured bootstrap tenant.

    }

  }


  return BOOTSTRAP_TENANT_ID;

};



const getAccessToken = () => {

  return localStorage.getItem(
    "gt_access_token"
  );

};



let refreshInProgress:
  Promise<boolean> | null = null;


const clearStoredSession = () => {

  localStorage.removeItem(
    "gt_access_token"
  );

  localStorage.removeItem(
    "gt_refresh_token"
  );

  localStorage.removeItem(
    "gt_user"
  );

};


async function refreshAccessToken():
Promise<boolean> {

  const refreshToken =
    localStorage.getItem(
      "gt_refresh_token"
    );


  if (!refreshToken) {

    return false;

  }


  try {

    const response =
      await fetch(
        `${API_BASE_URL}/api/v1/eiam/auth/refresh`,
        {

          method: "POST",

          headers: {

            "Content-Type":
              "application/json",

            ...(getTenantId()
              ? {
                  "X-Tenant-ID":
                    getTenantId()
                }
              : {})

          },

          body:
            JSON.stringify({
              refreshToken
            })

        }
      );


    if (!response.ok) {

      return false;

    }


    const payload =
      await response.json() as {

        data?: {

          accessToken?: string;

          refreshToken?: string;

        };

      };


    const nextAccessToken =
      payload.data?.accessToken;

    const nextRefreshToken =
      payload.data?.refreshToken;


    if (
      !nextAccessToken
      || !nextRefreshToken
    ) {

      return false;

    }


    localStorage.setItem(
      "gt_access_token",
      nextAccessToken
    );

    localStorage.setItem(
      "gt_refresh_token",
      nextRefreshToken
    );


    return true;

  }
  catch {

    return false;

  }

}


async function ensureRefreshed():
Promise<boolean> {

  if (!refreshInProgress) {

    refreshInProgress =
      refreshAccessToken()
        .finally(
          () => {

            refreshInProgress =
              null;

          }
        );

  }


  return refreshInProgress;

}


async function request<T>(
  endpoint: string,
  options?: RequestInit,
  allowRefresh = true
): Promise<T> {


  const token =
    getAccessToken();


  const response =
    await fetch(
      `${API_BASE_URL}${endpoint}`,
      {

        headers: {

          "Content-Type":
            "application/json",

          ...(getTenantId()
            ? {
                "X-Tenant-ID":
                  getTenantId()
              }
            : {}),

          ...(token
            ? {
                Authorization:
                  `Bearer ${token}`
              }
            : {})

        },

        ...options

      }
    );


  if (!response.ok) {


    if (
      response.status === 401
      && allowRefresh
      && endpoint
        !== "/api/v1/eiam/auth/login"
      && endpoint
        !== "/api/v1/eiam/auth/refresh"
    ) {

      const refreshed =
        await ensureRefreshed();


      if (refreshed) {

        return request<T>(
          endpoint,
          options,
          false
        );

      }


      clearStoredSession();

      window.location.href =
        "/login";

    }


    throw new Error(
      `API Error ${response.status}`
    );

  }


  return response.json();

}



const apiClient = {


  get<T>(
    endpoint: string
  ) {

    return request<T>(
      endpoint,
      {
        method: "GET"
      }
    );

  },



  post<T>(
    endpoint: string,
    body?: unknown
  ) {

    return request<T>(
      endpoint,
      {

        method: "POST",

        ...(body !== undefined
          ? {
              body:
                JSON.stringify(body)
            }
          : {})

      }
    );

  },


  patch<T>(
    endpoint: string,
    body?: unknown
  ) {

    return request<T>(
      endpoint,
      {

        method: "PATCH",

        ...(body !== undefined
          ? {
              body:
                JSON.stringify(body)
            }
          : {})

      }
    );

  }


};



export default apiClient;