const menuItems = [
  "Dashboard",
  "Learners",
  "Teachers",
  "Classes",
  "Attendance",
  "Reports",
  "Communication",
];

export default function Sidebar() {
  return (
    <aside className="gt-sidebar">
      <nav>
        {menuItems.map((item) => (
          <div key={item} className="gt-menu-item">
            {item}
          </div>
        ))}
      </nav>
    </aside>
  );
}
