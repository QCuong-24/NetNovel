import { Outlet } from 'react-router-dom';

export function DashboardLayout() {
  return (
    <section className="mx-auto grid w-full max-w-7xl gap-6 px-4 py-6 md:px-6">
      <Outlet />
    </section>
  );
}
