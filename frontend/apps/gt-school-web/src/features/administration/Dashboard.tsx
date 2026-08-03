import GTCard from "../../components/common/GTCard";
import GTSection from "../../components/common/GTSection";
import GTButton from "../../components/common/GTButton";

export default function Dashboard() {
  return (
    <div>

      <GTSection title="Quick Actions">

        <GTCard title="Learners">
          Manage learner records
          <br />
          <GTButton>
            Open
          </GTButton>
        </GTCard>


        <GTCard title="Teachers">
          Manage teacher information
          <br />
          <GTButton>
            Open
          </GTButton>
        </GTCard>


        <GTCard title="Classes">
          Manage classes and subjects
          <br />
          <GTButton>
            Open
          </GTButton>
        </GTCard>


      </GTSection>


      <GTSection title="Recent Activities">

        <p>
          No activities available yet.
        </p>

      </GTSection>


      <GTSection title="Notifications">

        <p>
          No notifications available yet.
        </p>

      </GTSection>


    </div>
  );
}
