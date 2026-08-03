import type { ReactNode } from "react";
import Sidebar from "../components/navigation/Sidebar";
import GTHeader from "../components/common/GTHeader";

interface MainLayoutProps {
  children: ReactNode;
}

export default function MainLayout({ children }: MainLayoutProps) {
  return (
    <div className="gt-layout">
       <GTHeader />

      <div className="gt-body">
        <Sidebar />

        <main className="gt-content">
          {children}
        </main>
      </div>
    </div>
  );
}
