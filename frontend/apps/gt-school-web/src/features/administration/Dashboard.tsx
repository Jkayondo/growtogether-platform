import LearnerIcon from "../../assets/icons/learner.svg?react";
import TeacherIcon from "../../assets/icons/teacher.svg?react";
import VisitorIcon from "../../assets/icons/visitor.svg?react";

import GTStatCard from "../../components/common/GTStatCard";
import GTAttendancePanel from "../../components/common/GTAttendancePanel";
import GTActivityFeed from "../../components/common/GTActivityFeed";
import GTSection from "../../components/common/GTSection";
import GTPaymentOverview from "../../components/common/GTPaymentOverview";
import GTNotificationPanel from "../../components/common/GTNotificationPanel";
import GTQuickActions from "../../components/common/GTQuickActions";
import { fetchDashboardData } from "../../services/dashboardService";
import type { DashboardData } from "../../types/dashboard";
import { useEffect, useState } from "react";



export default function Dashboard() {

  const [dashboard, setDashboard] = useState<DashboardData | null>(null);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState<string | null>(null);


  useEffect(() => {

    fetchDashboardData()

      .then((data) => {

        setDashboard(data);

      })

      .catch((err) => {

        setError(err.message);

      })

      .finally(() => {

        setLoading(false);

      });

  }, []);


  if (loading) {

    return <div>Loading dashboard...</div>;

  }


  if (error || !dashboard) {

    return <div>Unable to load dashboard</div>;

  }


  return (

    <div className="gt-dashboard">


      {/* ROW 1 */}
      <GTSection title="School Snapshot">

        <div className="gt-dashboard-summary-grid">

          <GTStatCard
            icon={LearnerIcon}
            title="Total Learners"
            value={dashboard.snapshot.totalLearners.toLocaleString()}
          />


          <GTStatCard
            icon={VisitorIcon}
            title="Visitors Today"
            value={dashboard.snapshot.visitorsToday.toLocaleString()}
          />


          <GTStatCard
            icon={TeacherIcon}
            title="Staff"
            value={dashboard.snapshot.totalStaff.toLocaleString()}
          />

        </div>

      </GTSection>



      {/* ROW 2 */}

      <div className="gt-dashboard-analysis-grid">

        <GTAttendancePanel
          attendance={dashboard.attendance}
        />

        <GTPaymentOverview
          payment={dashboard.payment}
        />

      </div>

      {/* ROW 3 */}

      <div className="gt-dashboard-bottom-grid">

        <GTQuickActions />

        <GTActivityFeed
          activities={dashboard.activities}
        />

        <GTNotificationPanel
          notifications={dashboard.notifications}
        />

      </div>


    </div>


  );

}
