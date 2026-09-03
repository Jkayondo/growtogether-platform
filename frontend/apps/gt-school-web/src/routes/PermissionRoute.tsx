import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";

import {
  useAuth
} from "../auth/authContext";

import type {
  Permission
} from "../auth/permissions";


interface Props {

  permission: Permission;

  children: ReactNode;

}



export default function PermissionRoute({
  permission,
  children
}: Props) {


  const {
    hasPermission
  } = useAuth();



  if (!hasPermission(permission)) {

    return (
      <Navigate
        to="/"
        replace
      />
    );

  }



  return children;

}
