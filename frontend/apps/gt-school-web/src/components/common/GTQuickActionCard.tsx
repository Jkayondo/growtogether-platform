import React from "react";

interface GTQuickActionCardProps {

  title: string;
  icon: React.ElementType;

}


export default function GTQuickActionCard({

  title,
  icon: Icon,

}: GTQuickActionCardProps) {


  return (

    <div className="gt-quick-action-card">


      <Icon className="gt-quick-action-icon" />


      <span>
        {title}
      </span>


    </div>

  );

}
