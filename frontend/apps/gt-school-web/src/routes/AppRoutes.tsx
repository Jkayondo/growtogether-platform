import TeacherWorkspace from "../features/teacher/TeacherWorkspace";
import {
  Routes,
  Route
} from "react-router-dom";

import MainLayout from "../layouts/MainLayout";
import Dashboard from "../features/administration/Dashboard";
import LeadershipOverview from "../features/leadership/LeadershipOverview";
import ProtectedRoute from "./ProtectedRoute";
import PermissionRoute from "./PermissionRoute";
import Login from "../features/auth/Login";
import VisitorDashboard from "../features/visitor/VisitorDashboard";
import AcademicYears from "../features/academic/AcademicYears";
import AssessmentPlans from "../features/academic/AssessmentPlans";
import ClassGrades from "../features/academic/ClassGrades";
import CandidateScores from "../features/academic/CandidateScores";
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
          path="leadership"
          element={
            <ProtectedRoute>
              <PermissionRoute
                permission={Permission.LEADERSHIP_OVERVIEW_READ}
              >
                <LeadershipOverview />
              </PermissionRoute>
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


        <Route
          path="academic/assessment-plans"
          element={
            <ProtectedRoute>
              <PermissionRoute
                permission={Permission.ASSESSMENT_READ}
              >
                <AssessmentPlans />
              </PermissionRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="academic/class-grades"
          element={
            <ProtectedRoute>
              <PermissionRoute
                permission={Permission.CURRICULUM_READ}
              >
                <ClassGrades />
              </PermissionRoute>
            </ProtectedRoute>
          }
        />


        <Route
          path="academic/candidate-scores"
          element={
            <ProtectedRoute>

              <PermissionRoute
                permission={Permission.ASSESSMENT_READ}
              >

                <CandidateScores />

              </PermissionRoute>

            </ProtectedRoute>
          }
        />

              </PermissionRoute>

            </ProtectedRoute>
          }
        />

        <Route
          path="teacher/workspace"
          element={
            <ProtectedRoute>
              <PermissionRoute
                permission={Permission.TEACHING_ASSIGNMENT_READ}
              >
                <TeacherWorkspace />
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
