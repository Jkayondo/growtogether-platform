export const API_CONFIG = {

  baseUrl:
    import.meta.env.VITE_API_URL ||
    "http://localhost:8080",

  endpoints: {

    dashboard:
      "/api/v1/dashboard"

  }

};
