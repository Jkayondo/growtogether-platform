import TeacherWorkspace from "../features/teacher/TeacherWorkspace";
import TeacherAiWorkspace from "../features/teacher-ai-workspace/TeacherAiWorkspace";
import ParentWorkspace from "../features/parent/ParentWorkspace";
import LearnerWorkspace from "../features/learner/LearnerWorkspace";
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
import TeachingAssignments from "../features/academic/TeachingAssignments";
import Curricula from "../features/academic/Curricula";
import ClassOfferings from "../features/academic/ClassOfferings";
import CandidateScores from "../features/academic/CandidateScores";
import ConnectDashboard from "../features/connect/ConnectDashboard";
import Learners from "../features/community/Learners";
import Admissions from "../features/community/Admissions";
import StudentEnrollments from "../features/community/StudentEnrollments";
import TeacherProfiles from "../features/academic/TeacherProfiles";

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
          path="community/admissions"
          element={
            <ProtectedRoute>
              <PermissionRoute
                permission={Permission.MANAGE_LEARNERS}
              >
                <Admissions />
              </PermissionRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="community/learners"
          element={
            <ProtectedRoute>
              <PermissionRoute
                permission={Permission.MANAGE_LEARNERS}
              >
                <Learners />
              </PermissionRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="community/enrollments"
          element={
            <ProtectedRoute>
              <PermissionRoute
                permission={Permission.MANAGE_LEARNERS}
              >
                <StudentEnrollments />
              </PermissionRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="community/staff"
          element={
            <ProtectedRoute>
              <PermissionRoute
                permission={Permission.MANAGE_USERS}
              >
                <TeacherProfiles />
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
          path="academic/curricula"
          element={
            <ProtectedRoute>
              <PermissionRoute
                permission={Permission.CURRICULUM_READ}
              >
                <Curricula />
              </PermissionRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="academic/class-offerings"
          element={
            <ProtectedRoute>
              <PermissionRoute
                permission={Permission.CURRICULUM_READ}
              >
                <ClassOfferings />
              </PermissionRoute>
            </ProtectedRoute>
          }
        />

        <Route
          path="academic/teaching-assignments"
          element={
            <ProtectedRoute>
              <PermissionRoute
                permission={Permission.TEACHING_ASSIGNMENT_READ}
              >
                <TeachingAssignments />
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
          path="teacher/ai"
          element={
            <ProtectedRoute>
              <PermissionRoute
                permission={Permission.TEACHING_ASSIGNMENT_READ}
              >
                <TeacherAiWorkspace />
              </PermissionRoute>
            </ProtectedRoute>
          }
        />



        <Route
          path="learner/workspace"
          element={
            <ProtectedRoute>
              <LearnerWorkspace />
            </ProtectedRoute>
          }
        />

        <Route
          path="parent/workspace"
          element={
            <ProtectedRoute>
              <ParentWorkspace />
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
