import { Outlet } from "react-router-dom";
import Sidebar from "../components/navigation/Sidebar";
import GTHeader from "../components/common/GTHeader";

export default function MainLayout() {

  return (

    <div className="gt-body">

      <Sidebar />

      <main className="gt-content">

        <GTHeader />

        <Outlet />

      </main>

    </div>

  );

}