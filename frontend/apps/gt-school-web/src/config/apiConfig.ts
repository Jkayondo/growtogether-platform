export const API_CONFIG = {

  baseUrl:
    import.meta.env.VITE_API_URL ||
    window.location.origin,

  endpoints: {

    dashboard:
      "/api/v1/dashboard"

  }

};
