import type { ReactNode } from "react";

interface GTCardProps {
  title: string;
  children: ReactNode;
}

export default function GTCard({ title, children }: GTCardProps) {
  return (
    <div className="gt-card">
      <h3>{title}</h3>

      <div>
        {children}
      </div>
    </div>
  );
}
