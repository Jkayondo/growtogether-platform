import {
  useEffect,
  useState
} from "react";

import GTSection from "../../components/common/GTSection";

import {
  loadMyActiveTeachingAssignments
} from "../../services/teachingAssignmentService";

import {
  loadSubjects
} from "../../services/subjectService";

import {
  loadClassGrades
} from "../../services/classGradeService";

import type {
  TeachingAssignment
} from "../../types/teachingAssignment";

import type {
  Subject
} from "../../types/subject";

import type {
  ClassGrade
} from "../../types/classGrade";

import { loadEducationLevels } from "../../services/educationLevelService";

import "./TeacherWorkspace.css";


export default function TeacherWorkspace() {


  const [
    assignments,
    setAssignments
  ] =
    useState<TeachingAssignment[]>([]);


  const [
    subjects,
    setSubjects
  ] =
    useState<Subject[]>([]);


  const [
    classGrades,
    setClassGrades
  ] =
    useState<ClassGrade[]>([]);


  const [
    loading,
    setLoading
  ] =
    useState(true);


  const [
    error,
    setError
  ] =
    useState<string | null>(null);



  useEffect(() => {
    let active = true;

    async function load() {
      setLoading(true);
      setError(null);

      try {
        const assignmentData =
          await loadMyActiveTeachingAssignments();

        if (!active) return;
        setAssignments(assignmentData);

        if (assignmentData.length === 0) {
          setSubjects([]);
          setClassGrades([]);
          return;
        }

        async function loadAssignedClasses() {
          const levels = await loadEducationLevels();
          const levelIds = [...new Set(levels.map(level => level.id))];
          const assignedIds = new Set(
            assignmentData.map(assignment => assignment.classGradeId)
          );
          const gradesById = new Map<string, ClassGrade>();
          let failed = false;

          // Limit concurrent requests while covering every education level.
          for (let offset = 0; offset < levelIds.length; offset += 4) {
            if (!active) return { grades: [], incomplete: true };

            const results = await Promise.allSettled(
              levelIds.slice(offset, offset + 4).map(id => loadClassGrades(id))
            );

            for (const result of results) {
              if (result.status === "rejected") {
                failed = true;
                continue;
              }

              for (const grade of result.value) {
                if (assignedIds.has(grade.id)) {
                  gradesById.set(grade.id, grade);
                }
              }
            }
          }

          return {
            grades: [...gradesById.values()],
            incomplete: failed ||
              [...assignedIds].some(id => !gradesById.has(id))
          };
        }

        const [subjectResult, classResult] = await Promise.allSettled([
          loadSubjects(),
          loadAssignedClasses()
        ]);

        if (!active) return;

        const notices: string[] = [];

        if (subjectResult.status === "fulfilled") {
          setSubjects(subjectResult.value);
          const subjectIds = new Set(
            subjectResult.value.map(subject => subject.id)
          );
          if (assignmentData.some(item => !subjectIds.has(item.subjectId))) {
            notices.push("Some subject names are unavailable.");
          }
        } else {
          setSubjects([]);
          notices.push("Subject names could not be loaded.");
        }

        if (classResult.status === "fulfilled") {
          setClassGrades(classResult.value.grades);
          if (classResult.value.incomplete) {
            notices.push("Some class names are unavailable.");
          }
        } else {
          setClassGrades([]);
          notices.push("Class names could not be loaded.");
        }

        if (notices.length > 0) {
          setError(
            notices.join(" ") +
            " Assignments remain visible with reference IDs where needed."
          );
        }
      } catch (cause) {
        if (!active) return;
        setAssignments([]);
        setSubjects([]);
        setClassGrades([]);

        const message = cause instanceof Error ? cause.message : "";
        setError(
          message === "API Error 403"
            ? "Access denied. Check your assignment-read permission and active teacher profile linkage."
            : message === "API Error 401"
              ? "Your session has expired. Please sign in again."
              : "Teaching assignments could not be loaded. Please reload to try again."
        );
      } finally {
        if (active) setLoading(false);
      }
    }

    void load();

    return () => {
      active = false;
    };
  }, []);



  function subjectName(
    id: string
  ) {

    return subjects.find(
      subject =>
        subject.id === id
    )?.subjectName
      ?? id;

  }



  function className(
    id: string
  ) {

    return classGrades.find(
      grade =>
        grade.id === id
    )?.className
      ?? id;

  }



  return (

    <div className="gt-dashboard teacher-workspace">


      <GTSection title="Teacher Workspace">


        <div className="teacher-summary-grid">


          <div className="teacher-card">

            <h3>
              My Teaching Assignments
            </h3>


            {
              loading && (
                <p>
                  Loading assignments...
                </p>
              )
            }


            {
              error && (
                <p role="alert">{error}</p>
              )
            }


            {!loading && !error && assignments.length === 0 && (
              <p role="status">
                No active teaching assignments found.
              </p>
            )}

            {
              assignments.map(
                assignment => (

                  <div
                    key={assignment.id}
                    className="teacher-assignment-card"
                  >

                    <strong>
                      {
                        className(
                          assignment.classGradeId
                        )
                      }
                    </strong>


                    <p>
                      Subject:
                      {" "}
                      {
                        subjectName(
                          assignment.subjectId
                        )
                      }
                    </p>


                    <p>
                      Periods per week:
                      {" "}
                      {assignment.weeklyPeriods}
                    </p>


                    <p>
                      Status:
                      {" "}
                      {assignment.assignmentStatus}
                    </p>


                  </div>

                )
              )
            }


          </div>



          <div className="teacher-card">

            <h3>
              Today's Programme
            </h3>

            <p>
              Lessons, meetings and school activities.
            </p>

          </div>



          <div className="teacher-card">

            <h3>
              Curriculum Progress
            </h3>

            <p>
              Track completed topics and teaching coverage.
            </p>

          </div>


        </div>


      </GTSection>


      <GTSection title="Teaching Tools">


        <div className="teacher-tools-grid">

          <div className="teacher-tool-card">
            Curriculum
          </div>

          <div className="teacher-tool-card">
            Scheme of Work
          </div>

          <div className="teacher-tool-card">
            Lesson Plans
          </div>

          <div className="teacher-tool-card">
            Teaching Notes
          </div>

          <div className="teacher-tool-card">
            Assessments
          </div>

          <div className="teacher-tool-card">
            Exams & Results
          </div>

          <div className="teacher-tool-card">
            Attendance
          </div>

          <div className="teacher-tool-card">
            GT Connect
          </div>

        </div>


      </GTSection>


    </div>

  );

}
