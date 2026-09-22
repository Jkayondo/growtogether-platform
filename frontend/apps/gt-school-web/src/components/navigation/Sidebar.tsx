import { useState } from "react";
import { useNavigate } from "react-router-dom";
import DashboardIcon from "../../assets/icons/navigation/dashboard.svg?react";
import CommunityIcon from "../../assets/icons/navigation/school-community.svg?react";
import AcademicIcon from "../../assets/icons/navigation/academic.svg?react";
import CalendarIcon from "../../assets/icons/navigation/planning-events.svg?react";
import AttendanceIcon from "../../assets/icons/navigation/attendance.svg?react";
import FinanceIcon from "../../assets/icons/navigation/finance.svg?react";
import OperationsIcon from "../../assets/icons/navigation/operations.svg?react";
import CommunicationIcon from "../../assets/icons/navigation/communication.svg?react";
import ReportsIcon from "../../assets/icons/navigation/reports.svg?react";
import VisitorIcon from "../../assets/icons/visitor.svg?react";

import {
  Permission
} from "../../auth/permissions";

import {
  useAuth
} from "../../auth/authContext";

const menuItems = [
  {
    title: "Dashboard",
    icon: DashboardIcon,
  },
  {
    title: "Visitors",
    icon: VisitorIcon,
    permission: Permission.MANAGE_VISITORS,
  },

  {
    title: "School Community",
    icon: CommunityIcon,
    permission: Permission.MANAGE_LEARNERS,
    children: [
      "Learners",
      "Parents / Guardians",
      "Staff",
    ],
  },
  {
    title: "Academic",
    icon: AcademicIcon,
    permission: Permission.ACADEMIC_YEAR_READ,
    children: [
      "Academic Years",
      "Classes",
      "Subjects",
      "Timetable",
      "Assessments",
      "Assessment Plans",
      "Candidate Scores",
      "Results",
    ],
  },
  {
    title: "Planning & Events",
    icon: CalendarIcon,
    permission: Permission.MANAGE_LEARNERS,
    children: [
      "School Calendar",
      "Meetings",
      "Ceremonies",
      "Activities",
    ],
  },
  {
    title: "Attendance",
    icon: AttendanceIcon,
    permission: Permission.MANAGE_ATTENDANCE,
  },
  {
    title: "Finance",
    icon: FinanceIcon,
    permission: Permission.MANAGE_FEES,
    children: [
      "Fees",
      "Payments",
      "Student Wallets",
      "Marketplace",
    ],
  },
  {
    title: "Operations",
    icon: OperationsIcon,
    children: [
      "Food & Dining",
      "Transport",
      "Boarding",
      "Inventory",
      "Facilities",
    ],
  },
  {
    title: "Communication",
    icon: CommunicationIcon,
    children: [
      "GT Connect",
    ],
  },
  {
    title: "Reports",
    icon: ReportsIcon,
    permission: Permission.VIEW_REPORTS,
  },
];

export default function Sidebar() {

  const navigate =
    useNavigate();


  const [expanded, setExpanded] =
    useState<string | null>(null);


  const {
    hasPermission
  } = useAuth();


  return (
    <aside className="gt-sidebar">

      <nav>

        {menuItems
          .filter(
            (item) =>
              !item.permission ||
              hasPermission(item.permission)
          )
          .map((item) => {

            const Icon = item.icon;

            const hasChildren =
              item.children;

            const isOpen =
              expanded === item.title;


            return (
              <div key={item.title}>

                <div
                  className={`gt-menu-item ${item.title === "Dashboard" ? "active" : ""
                    }`}
                  onClick={() => {

                    if (hasChildren) {

                      setExpanded(
                        isOpen ? null : item.title
                      );

                    }

                    else if (item.title === "Visitors") {

                      window.location.href = "/visitors";

                    }

                  }}
                >
                  <Icon className="gt-sidebar-icon" />

                  <span>{item.title}</span>

                  {
                    hasChildren && (
                      <span className="gt-menu-arrow">
                        {isOpen ? "⌄" : "›"}
                      </span>
                    )
                  }
                </div>


                {
                  hasChildren && isOpen && (
                    <div className="gt-submenu">
                      {item.children.map((child) => (
                        <div
                          key={child}
                          className="gt-submenu-item"
                          onClick={() => {

                            if (
                              child === "Academic Years"
                            ) {

                              navigate(
                                "/academic/years"
                              );

                            }
                              else if (
                                child === "Assessment Plans"
                              ) {

                                navigate(
                                  "/academic/assessment-plans"
                                );

                              }

                              else if (
                                child === "Candidate Scores"
                              ) {

                                navigate(
                                  "/academic/candidate-scores"
                                );

                              }



                            else if (
                              child === "GT Connect"
                            ) {

                              navigate(
                                "/connect"
                              );

                            }

                          }}
                        >
                          {child}
                        </div>
                      ))}
                    </div>
                  )
                }

              </div>
            );
          })}
      </nav>
    </aside >
  );
}
