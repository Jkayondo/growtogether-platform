const API_BASE_URL =
  import.meta.env.VITE_API_URL ||
  "http://localhost:8080";


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



async function request<T>(
  endpoint: string,
  options?: RequestInit
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


    if (response.status === 401) {

      localStorage.removeItem(
        "gt_access_token"
      );

      localStorage.removeItem(
        "gt_user"
      );

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
    body: unknown
  ) {

    return request<T>(
      endpoint,
      {

        method: "POST",

        body:
          JSON.stringify(body)

      }
    );

  }


};



export default apiClient;