import BoyIcon from "../../assets/icons/navigation/boy.svg?react";
import GirlIcon from "../../assets/icons/navigation/girl.svg?react";
import type { AttendanceSummary } from "../../types/dashboard";

import GTPieChart from "../charts/GTPieChart";

interface Props {
  attendance: AttendanceSummary;
}


export default function GTAttendancePanel({
  attendance
}: Props) {

  const attendanceData = [
    {
      name: "Present",
      value: attendance.present,
      color: "#8FC73E",
    },
    {
      name: "Absent",
      value: attendance.absent,
      color: "#E85D5D",
    },
  ];


  return (

    <div className="gt-attendance-panel">

      <h3>
        Today's Attendance
      </h3>


      <div className="gt-attendance-content">


        <div className="gt-attendance-chart">

          <GTPieChart
            data={attendanceData}
            centerText={`${attendance.attendanceRate}%`}
          />

        </div>



        <div className="gt-chart-summary">


          <div className="attendance-stat-row">
            <span>
              <span className="gt-dot present"></span>
              Present:
            </span>

            <strong>
              {attendance.present.toLocaleString()}
            </strong>
          </div>



          <div className="attendance-stat-row">
            <span>
              <span className="gt-dot absent"></span>
              Absent:
            </span>

            <strong>
              {attendance.absent.toLocaleString()}
            </strong>
          </div>



          <div className="attendance-stat-row">
            <span>
              <span className="gt-dot late"></span>
              Late Arrivals:
            </span>

            <strong>
              {attendance.lateArrivals.toLocaleString()}
            </strong>
          </div>



          <div className="attendance-gender-summary">

            <div className="gender-item">

              <BoyIcon />

              <span>
                Boys:
              </span>

              <strong>
                {attendance.boys.toLocaleString()}
              </strong>

            </div>



            <div className="gender-item">

              <GirlIcon />

              <span>
                Girls:
              </span>

              <strong>
                {attendance.girls.toLocaleString()}
              </strong>

            </div>

          </div>


        </div>


      </div>


    </div>

  );

}
