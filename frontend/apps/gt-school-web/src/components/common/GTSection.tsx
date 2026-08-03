import type { ReactNode } from "react";

interface GTSectionProps {
  title: string;
  children: ReactNode;
}

export default function GTSection({ title, children }: GTSectionProps) {
  return (
    <section className="gt-section">
      <h2>{title}</h2>

      <div>
        {children}
      </div>
    </section>
  );
}
