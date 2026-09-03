import type { ActivityItem } from "../../types/dashboard";


interface Props {

  activities: ActivityItem[];

}


export default function GTActivityFeed({
  activities
}: Props) {

  return (
    <div className="gt-activity-feed">

      <h3>
        Recent Activities
      </h3>


      {activities.map((activity, index) => (

        <div
          key={index}
          className="gt-activity-item"
        >

          <div className="gt-activity-dot">
          </div>


          <div>

            <strong>
              {activity.title}
            </strong>

            <p>
              {activity.description}
            </p>

            <small>
              {activity.time}
            </small>

          </div>

        </div>

      ))}

    </div>
  );
}
