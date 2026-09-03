import type { AuthSession } from "./authTypes";


const TOKEN_KEY = "gt_access_token";


export const saveSession = (
  session: AuthSession
) => {

  localStorage.setItem(
    TOKEN_KEY,
    session.accessToken
  );

  localStorage.setItem(
    "gt_user",
    JSON.stringify(session.user)
  );

};


export const getToken = () => {

  return localStorage.getItem(
    TOKEN_KEY
  );

};


export const getCurrentUser = () => {

  const user =
    localStorage.getItem("gt_user");


  return user
    ? JSON.parse(user)
    : null;

};


export const clearSession = () => {

  localStorage.removeItem(
    TOKEN_KEY
  );

  localStorage.removeItem(
    "gt_user"
  );

};
