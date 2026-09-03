import React from "react";

interface GTStatCardProps {
  title: string;
  value: string;
  icon: React.ElementType;
}

export default function GTStatCard({
  title,
  value,
  icon: Icon,
}: GTStatCardProps) {
  return (
    <div className="gt-stat-card">

      <Icon className="gt-stat-icon" />

      <div>
        <h2>{value}</h2>
        <p>{title}</p>
      </div>

    </div>
  );
}