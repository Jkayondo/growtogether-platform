import type { NotificationItem } from "../../types/dashboard";


interface Props {

  notifications: NotificationItem[];

}


export default function GTNotificationPanel({
  notifications
}: Props) {

  return (
    <div className="gt-notification-panel">

      <h3>
        Notifications
      </h3>


      {notifications.map((notification, index) => (

        <div
          key={index}
          className={`gt-notification-item ${notification.type}`}
        >

          <strong>
            {notification.title}
          </strong>

          <p>
            {notification.message}
          </p>

        </div>

      ))}

    </div>
  );
}
