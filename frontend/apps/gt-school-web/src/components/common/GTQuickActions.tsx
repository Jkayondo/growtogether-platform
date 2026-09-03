import LearnerIcon from "../../assets/icons/learner.svg?react";
import TeacherIcon from "../../assets/icons/teacher.svg?react";
import AttendanceIcon from "../../assets/icons/attendance.svg?react";
import FinanceIcon from "../../assets/icons/finance.svg?react";
import CommunicationIcon from "../../assets/icons/communication.svg?react";

import GTQuickActionCard from "./GTQuickActionCard";


export default function GTQuickActions() {

    return (

        <div className="gt-quick-actions-panel">

            <h3>
                Quick Actions
            </h3>


            <div className="gt-quick-actions">


                <GTQuickActionCard
                    icon={LearnerIcon}
                    title="Add Learner"
                />


                <GTQuickActionCard
                    icon={TeacherIcon}
                    title="Add Staff"
                />


                <GTQuickActionCard
                    icon={AttendanceIcon}
                    title="Take Attendance"
                />


                <GTQuickActionCard
                    icon={FinanceIcon}
                    title="Record Payment"
                />


                <GTQuickActionCard
                    icon={CommunicationIcon}
                    title="Send Message"
                />


            </div>

        </div>

    );
}