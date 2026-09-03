import {
  createContext,
  useContext,
  useState
} from "react";

import type {
  AuthUser
} from "./authTypes";

import {
  getCurrentUser,
  clearSession
} from "./authService";

import {
  Permission
} from "./permissions";

interface AuthContextType {

  user: AuthUser | null;

  isAuthenticated: boolean;

  login: (user: AuthUser) => void;

  logout: () => void;

  refreshUser: () => void;

  hasPermission:
  (permission: Permission) => boolean;

}



const AuthContext =
  createContext<AuthContextType | undefined>(
    undefined
  );



export function AuthProvider({
  children
}: {
  children: React.ReactNode;
}) {


  const [user, setUser] =
    useState<AuthUser | null>(
      () => getCurrentUser()
    );




  const login = (
    user: AuthUser
  ) => {

    setUser(user);

  };




  const refreshUser = () => {

    const currentUser =
      getCurrentUser();

    setUser(currentUser);

  };




  const logout = () => {

    clearSession();

    setUser(null);

  };




  const hasPermission = (
    permission: Permission
  ) => {


    if (!user) {

      return false;

    }


    return user.permissions?.includes(
      permission
    ) ?? false;

  };




  return (

    <AuthContext.Provider

      value={{

        user,

        isAuthenticated:
          user !== null,

        login,

        logout,

        refreshUser,

        hasPermission

      }}

    >

      {children}

    </AuthContext.Provider>

  );

}




export function useAuth() {

  const context =
    useContext(AuthContext);


  if (!context) {

    throw new Error(
      "useAuth must be used inside AuthProvider"
    );

  }


  return context;

}