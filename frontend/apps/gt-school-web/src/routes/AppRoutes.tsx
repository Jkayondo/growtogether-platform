import {
  Routes,
  Route
} from "react-router-dom";

import MainLayout from "../layouts/MainLayout";
import Dashboard from "../features/administration/Dashboard";
import ProtectedRoute from "./ProtectedRoute";
import PermissionRoute from "./PermissionRoute";
import Login from "../features/auth/Login";
import VisitorDashboard from "../features/visitor/VisitorDashboard";
import AcademicYears from "../features/academic/AcademicYears";
import ConnectDashboard from "../features/connect/ConnectDashboard";

import {
  Permission
} from "../auth/permissions";

export default function AppRoutes() {

  return (

    <Routes>

      <Route
        path="/login"
        element={<Login />}
      />


      <Route
        path="/"
        element={<MainLayout />}
      >

        <Route
          index
          element={
            <ProtectedRoute>

              <Dashboard />

            </ProtectedRoute>
          }
        />

        <Route
          path="visitors"
          element={
            <ProtectedRoute>

              <PermissionRoute
                permission={Permission.MANAGE_VISITORS}
              >

                <VisitorDashboard />

              </PermissionRoute>

            </ProtectedRoute>
          }
        />


        <Route
          path="academic/years"
          element={
            <ProtectedRoute>

              <PermissionRoute
                permission={Permission.ACADEMIC_YEAR_READ}
              >

                <AcademicYears />

              </PermissionRoute>

            </ProtectedRoute>
          }
        />

        <Route
          path="connect"
          element={
            <ProtectedRoute>

              <ConnectDashboard />

            </ProtectedRoute>
          }
        />


      </Route>

    </Routes>

  );

}
