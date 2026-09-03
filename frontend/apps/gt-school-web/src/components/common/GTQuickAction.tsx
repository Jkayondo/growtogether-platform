import React from "react";

interface GTQuickActionProps {
  icon: React.ElementType;
  title: string;
  description?: string;
}


export default function GTQuickAction({
  icon: Icon,
  title,
  description,
}: GTQuickActionProps) {

  return (
    <div className="gt-quick-action">

      <Icon className="gt-quick-action-icon" />

      <div>
        <strong>
          {title}
        </strong>

        {description && (
          <small>
            {description}
          </small>
        )}

      </div>

    </div>
  );
}
